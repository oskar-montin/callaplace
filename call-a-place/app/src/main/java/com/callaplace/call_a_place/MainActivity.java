package com.callaplace.call_a_place;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static String DEFAULT_FAVOURITES = "[" +
            "{'loc':[47,20]}," +
            "{'loc':[57,13],'title':'My favourite'}]";
    private static String DEFAULT_CALLHISTORY = "[" +
            "{'type':'INCOMING','loc':[19,6]}," +
            "{'type':'OUTGOING','loc':[50,15],'title':'My friend'}," +
            "{'type':'MISSED','loc':[42,11]}]";

    /*private static LatLng[] FAVOURITES = new LatLng[]{
            new LatLng(47, 20), new LatLng(57, 13)
    };
    private static LatLng[] HISTORY = new LatLng[]{
            new LatLng(19, 6), new LatLng(50, 15),
    };*/


    private enum CallType { INCOMING, OUTGOING, MISSED };

    private GoogleMap mMap;

    private TabLayout mTabs;
    private FloatingActionButton mFab;

    private BottomSheetBehavior mBorromSheetBehavior;
    private TextView mBorromSheetTitle;
    private TextView mBorromSheetLocation;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private List<Marker> mFavourites = new ArrayList<Marker>();
    private List<Marker> mCallHistory = new ArrayList<Marker>();

    private Marker mCurrentMaker;


    // Backend team
    Location mLastLocation;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize map
        final SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTabs = (TabLayout) findViewById(R.id.tabLayout);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        final LinearLayout bottomSheet = (LinearLayout) findViewById(R.id.bottomSheet);
        mBorromSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBorromSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mBorromSheetTitle = (TextView) bottomSheet.findViewById(R.id.titleTextView);
        mBorromSheetLocation = (TextView) bottomSheet.findViewById(R.id.locationTextView);

        mTabs.setOnTabSelectedListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API)
                .build();

        //ActivityCompat.requestPermissions(this, new String[]{
        // Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void loadSavedData() throws JSONException {
        if (mMap == null)
            throw new IllegalStateException("Map not initialized");
        SharedPreferences prefs = getPreferences(0);

        boolean showFav = mTabs.getSelectedTabPosition() == 0;

        // Favorites
        JSONArray favorites = new JSONArray(prefs.getString("favorites", DEFAULT_FAVOURITES));
        for (int i = 0; i < favorites.length(); i++){
            JSONObject favorite = favorites.getJSONObject(i);
            JSONArray loc = favorite.getJSONArray("loc");
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(R.drawable.ic_star_border_gold_64dp))
                    .anchor(.5f, .5f)
                    .visible(showFav);
            if (favorite.has("title"))
                opt.title(favorite.getString("title"));
            mFavourites.add(mMap.addMarker(opt));
        }

        // Favorites
        JSONArray callHistory = new JSONArray(prefs.getString("callHistory", DEFAULT_CALLHISTORY));
        for (int i = 0; i < callHistory.length(); i++){
            JSONObject call = callHistory.getJSONObject(i);
            JSONArray loc = call.getJSONArray("loc");
            int iconResource = 0;
            switch (CallType.valueOf(call.getString("type"))){
                case INCOMING:
                    iconResource = R.drawable.ic_call_received_green_64dp;
                    break;
                case OUTGOING:
                    iconResource = R.drawable.ic_call_made_green_64dp;
                    break;
                case MISSED:
                    iconResource = R.drawable.ic_call_missed_red_64dp;
                    break;
            };
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(iconResource))
                    .anchor(0.5f, 0.5f)
                    .visible(!showFav);
            if (call.has("title"))
                opt.title(call.getString("title"));
            mCallHistory.add(mMap.addMarker(opt));
        }
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        Drawable vectorDrawable = getDrawable(id);
        vectorDrawable.setBounds(0, 0, 64, 64);
        Bitmap bm = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    private void showCallHistory() {
        if (mCallHistory.isEmpty()) return;

        for (Marker mark : mFavourites)
            mark.setVisible(false);
        for (Marker mark : mCallHistory)
            mark.setVisible(true);
    }

    private void showFavourites() {
        if (mFavourites.isEmpty()) return;

        for (Marker mark : mCallHistory)
            mark.setVisible(false);
        for (Marker mark : mFavourites)
            mark.setVisible(true);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mCurrentMaker != null){
            // Remove marker
            mCurrentMaker.remove();
            mCurrentMaker = null;
            // Hide bottom sheet
            mBorromSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mCurrentMaker != null){
            mCurrentMaker.setPosition(latLng);
        }
        else {
            mCurrentMaker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true));
        }
        updateCurrentLocation(latLng);
    }

    private void updateCurrentLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String title;
        try {
            Address address = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1).get(0);
            title = address.getFeatureName();
            if (title == null) title = address.getLocality();
            if (title == null) title = address.getCountryName();
            if (title == null) title = address.toString();
        } catch (IOException | IndexOutOfBoundsException e) {
            title = "Custom location";
        }
        mBorromSheetTitle.setText(title);
        mBorromSheetLocation.setText(latLng.latitude + "," + latLng.longitude);
        mBorromSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == mCurrentMaker){
            mBorromSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else {
            // Place marker on the other marker's position
            onMapLongClick(marker.getPosition());
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        updateCurrentLocation(marker.getPosition());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //mMap.getUiSettings().setMapToolbarEnabled();

        // TODO: permissions
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        // Load data)
        try {
            loadSavedData();
        } catch (JSONException e) {
            // TODO: Handle exception
            e.printStackTrace();
        }
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Register for location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    client, LocationRequest.create().setInterval(300000), this);

            // Get latest known location
            Location loc = LocationServices.FusedLocationApi.getLastLocation(client);
            if (loc != null) {
                onLocationChanged(loc);

                // call AsynTask to perform network operation on separate thread
                //new HttpAsyncTask().execute("http://angseus.ninja:3000/");
                //etResponse.setText(String.valueOf(longitude) + " " + String.valueOf(latitude));
                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO: Update my location to server
        User me = null;
        new UpdateLocation().execute(me);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        StringBuilder res = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            res.append(line);
        inputStream.close();
        return res.toString();
    }

    private class User {
        private int id;
        private LatLng loc;

        public User(int id, LatLng loc) {
            this.id = id;
            this.loc = loc;
        }
        public int getId() {
            return id;
        }
        public LatLng getLocation() {
            return loc;
        }
    }

    private class UpdateLocation extends AsyncTask<User, Void, Boolean> {
        @Override
        protected Boolean doInBackground(User... params) {
            try {
                String url = "https://xxx:NNNN/location";
                HttpPost req = new HttpPost(url);

                User self = params[0];
                LatLng loc = self.getLocation();

                JSONObject body = new JSONObject();
                body.put("user", self.getId());
                body.put("lat", loc.latitude);
                body.put("lon", loc.longitude);

                req.setEntity(new StringEntity(body.toString(), "UTF8"));
                req.setHeader("Content-type", "application/json");

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse res = httpClient.execute(req);
            } catch (IOException | JSONException e) {
                // TODO: Handle error
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private class GetLocations extends AsyncTask<LatLng, Void, User[]> {
        @Override
        protected User[] doInBackground(LatLng... params) {
            try {
                LatLng loc = params[0];
                String url = "https://xxx:NNNN/location?lat=" + loc.latitude + "&lon=" + loc.longitude;
                HttpGet req = new HttpGet(url);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse res = httpClient.execute(req);

                JSONArray data = new JSONArray(convertInputStreamToString(res.getEntity().getContent()));
                User[] users = new User[data.length()];
                for (int i = 0; i < users.length; i++){
                    JSONObject item = data.getJSONObject(i);
                    User user = new User(item.getInt("id"), new LatLng(
                            item.getDouble("id"),item.getDouble("id")));
                    users[i] = user;
                }
                return users;
            } catch (IOException | JSONException e) {
                // TODO: Handle error
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User[] users) {
            // TODO: Initialize call to a user/all users
        }
    }


    // OLD



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

            //TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //String MAC = tMgr.getLine1Number();

            try {
                //object.put("phonenumber", MAC);
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
