package com.callaplace.call_a_place;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    private FloatingActionButton mFab;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    private static LatLng[] FAVOURITES = new LatLng[]{
            new LatLng(47, 20), new LatLng(57, 13)
    };
    private static LatLng[] HISTORY = new LatLng[]{
            new LatLng(19, 6), new LatLng(50, 15),
    };

    private List<Marker> mFavourites = new ArrayList<Marker>();
    private List<Marker> mCallHistory = new ArrayList<Marker>();
    private Marker lastMarker;


    // Backend team
    Location mLastLocation;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longitude = lastMarker.getPosition().longitude;
                latitude = lastMarker.getPosition().latitude;
                new getLocations().execute("http://angseus.ninja:3000");
            }
        });

        setupBottomSheet();
        setupTabIconTints();

        final TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setOnTabSelectedListener(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // check if you are connected or not
        if (isConnected()) {
            //tvIsConnected.setBackgroundColor(0xFF00CC00);
            //tvIsConnected.setText("You are conncted");
        } else {
            //tvIsConnected.setText("You are NOT conncted");
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API)
                .build();

        //ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void showCallHistory() {
        for (Marker mark : mFavourites) {
            mark.setVisible(false);
        }

        if (mCallHistory.isEmpty()) {
            for (LatLng pos : HISTORY) {
                mCallHistory.add(mMap.addMarker(new MarkerOptions().position(pos)));
            }
        } else {
            for (Marker mark : mCallHistory) {
                mark.setVisible(true);
            }
        }
    }

    private void showFavourites() {
        for (Marker mark : mCallHistory) {
            mark.setVisible(false);
        }

        if (mFavourites.isEmpty()) {
            for (LatLng pos : FAVOURITES) {
                mFavourites.add(mMap.addMarker(new MarkerOptions().position(pos)));
            }
        } else {
            for (Marker mark : mFavourites) {
                mark.setVisible(true);
            }
        }
    }

    private void setupTabIconTints() {
        final TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.getTabAt(i).getIcon().setTint(
                    getResources().getColor(R.color.colorWhite));
        }
    }

    private void setupBottomSheet() {
        LinearLayout bottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        // Initial behavior
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (lastMarker == null) {
            lastMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        }
        else lastMarker.setPosition(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LinearLayout bottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        // Update views
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> address = null;
        LatLng loc = null;
        try {
            loc = marker.getPosition();
            address = geocoder.getFromLocation(loc.latitude, loc.longitude, 1);
        }catch (Exception jagBryrMigInte) {
            return false;
        }

        TextView titleTextView = (TextView) bottomSheet.findViewById(R.id.titleTextView);
        TextView locationTextView = (TextView) bottomSheet.findViewById(R.id.locationTextView);
        titleTextView.setText(address.get(0).getCountryName());
        locationTextView.setText(loc.latitude + ", " + loc.longitude);

        // Set bottom sheet state
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // TODO: permissions
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Initialize current tab
        final TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        onTabSelected(tabs.getTabAt(tabs.getSelectedTabPosition()));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                showFavourites();
                break;
            case 1:
                showCallHistory();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.callaplace.call_a_place/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.callaplace.call_a_place/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if (mLastLocation != null) {
                longitude = mLastLocation.getLongitude();
                latitude = mLastLocation.getLatitude();

                // call AsynTask to perform network operation on separate thread
                new HttpAsyncTask().execute("http://angseus.ninja:3000/");
                //etResponse.setText(String.valueOf(longitude) + " " + String.valueOf(latitude));
                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /* TO BE USED TO UPDATE LOCATIONS ?*/
    /* Class My Location Listener */
    /*public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            String Text = "My current location is: Latitud = " + loc.getLatitude() + " Longitud = " + loc.getLongitude();
            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();

            etResponse.setText(String.valueOf(longitude));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }*/

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class getLocations extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            InputStream inputStream = null;
            String result = "";
            try {
                HttpGet httpGet = new HttpGet("http://angseus.ninja:3000/getlocations?longitude=" + String.valueOf(longitude) + "&latitude=" + String.valueOf(latitude));
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httpGet);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    // receive response as inputStream
                    inputStream = response.getEntity().getContent();
                    // convert inputstream to string
                    if (inputStream != null) {
                        result = convertInputStreamToString(inputStream);
                    }
                    else {
                        result = "Did not work!";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(result);
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray json = new JSONArray(result);
                //etResponse.setText(json.toString(1));

                JSONObject doc = (JSONObject) json.get(0);
                JSONArray loc = (JSONArray) doc.get("loc");

                Toast.makeText(getBaseContext(),
                        "TODO: Make a call to " + doc.getString("id") + " at pos(" + loc.getDouble(1) + "," + loc.getDouble(0) + ")", Toast.LENGTH_LONG);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class updateLocation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            JSONObject object = new JSONObject();

            //WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //WifiInfo info = manager.getConnectionInfo();
            //String MAC = info.getMacAddress();

            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String MAC = tMgr.getLine1Number();

            try {
                object.put("phonenumber", MAC);
                object.put("longitude", longitude);
                object.put("latitude", latitude);

            } catch (Exception ex) {

            }
            try {
                String message = object.toString();
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost("http://angseus.ninja:3000/updatelocation");

                httpPost.setEntity(new StringEntity(message, "UTF8"));
                httpPost.setHeader("Content-type", "application/json");
                HttpResponse resp = httpclient.execute(httpPost);
                if (resp != null) {

                }
                Log.d("Status line", "" + resp.getStatusLine().getStatusCode());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            new getLocations().execute("http://angseus.ninja:3000/");
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            new updateLocation().execute("http://angseus.ninja:3000/");
        }
    }
}
