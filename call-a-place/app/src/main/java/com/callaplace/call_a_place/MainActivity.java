package com.callaplace.call_a_place;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static String DEFAULT_FAVOURITES = "[" +
            "{'loc':[57.690,11.977]}," +
            "{'loc':[57.690,11.972]}]";
    private static String DEFAULT_CALLHISTORY = "[" +
            "{'type':'INCOMING','loc':[57.689,11.974],'time':'2016-05-14T10:10:10.010Z'}," +
            "{'type':'OUTGOING','loc':[57.686,11.967],'time':'2016-05-14T12:12:10.012Z'}," +
            "{'type':'MISSED','loc':[57.688,11.979],'time':'2016-05-14T15:15:10.015Z'}]";

    enum CallType { INCOMING, OUTGOING, MISSED };

    private String mUserId;
    private JSONArray mFavourites;
    private JSONArray mCallHistory;

    private GoogleMap mMap;

    // Views

    private TabLayout mTabs;
    private FloatingActionButton mCallFab;
    private FloatingActionButton mMyLocationFab;
    private SearchView mSearchView;

    private BottomSheetBehavior mMarkerDetailsBottomSheetBehavior;
    private BottomSheetBehavior mCallHistoryBottomSheetBehavior;
    private RecyclerView mCallHistoryList;

    private TextView mMarkerDetailsTitle;
    private TextView mMarkerDetailsLocation;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Map<JSONObject, Marker> mFavouriteMarkers = new HashMap<>();
    private Map<JSONObject, Marker> mCallHistoryMarkers = new HashMap<>();
    private Marker mCurrentMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get (opt) id from preferences
        SharedPreferences prefs = getPreferences(0);
        mUserId = prefs.getString("user", null);

        // TODO: Get voip user settings etc. from prefs

        // Initialize map
        final SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTabs = (TabLayout) findViewById(R.id.tabLayout);
        mCallFab = (FloatingActionButton) findViewById(R.id.callFab);
        mMyLocationFab = (FloatingActionButton) findViewById(R.id.myLocationFab);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        mTabs.setOnTabSelectedListener(this);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Pattern locTest = Pattern.compile("(\\d+\\.?\\d*),(\\d+\\.?\\d*)");
                Matcher m = locTest.matcher(query);
                if (m.matches()) {
                    LatLng loc = new LatLng(Double.parseDouble(m.group(1)),
                            Double.parseDouble(m.group(2)));
                    onMapLongClick(loc);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        final LinearLayout markDetBottomSheet = (LinearLayout) findViewById(R.id.markDetBottomSheet);
        final LinearLayout histBottomSheet = (LinearLayout) findViewById(R.id.histBottomSheet);
        mMarkerDetailsBottomSheetBehavior = BottomSheetBehavior.from(markDetBottomSheet);
        mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCallHistoryBottomSheetBehavior = BottomSheetBehavior.from(histBottomSheet);
        mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mMarkerDetailsTitle = (TextView) markDetBottomSheet.findViewById(R.id.titleTextView);
        mMarkerDetailsLocation = (TextView) markDetBottomSheet.findViewById(R.id.locationTextView);

        mCallHistoryList = (RecyclerView) histBottomSheet.findViewById(R.id.callHistoryList);
        mCallHistoryList.setLayoutManager(new LinearLayoutManager(this));

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

        int currTab = mTabs.getSelectedTabPosition();

        // Favourites
        mFavourites = new JSONArray(prefs.getString("favorites", DEFAULT_FAVOURITES));
        for (int i = 0; i < mFavourites.length(); i++) {
            JSONObject favourite = mFavourites.getJSONObject(i);
            JSONArray loc = favourite.getJSONArray("loc");
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(R.drawable.ic_star_border_gold_64dp))
                    .anchor(.5f, .5f)
                    .visible(currTab == 0);
            mFavouriteMarkers.put(favourite, mMap.addMarker(opt));
        }

        // Call history
        mCallHistory = new JSONArray(prefs.getString("history", DEFAULT_CALLHISTORY));
        for (int i = 0; i < mCallHistory.length(); i++) {
            JSONObject call = mCallHistory.getJSONObject(i);
            JSONArray loc = call.getJSONArray("loc");
            int iconResource = 0;
            switch (CallType.valueOf(call.getString("type"))) {
                case INCOMING:
                    iconResource = R.drawable.ic_call_received_green_64dp;
                    break;
                case OUTGOING:
                    iconResource = R.drawable.ic_call_made_green_64dp;
                    break;
                case MISSED:
                    iconResource = R.drawable.ic_call_missed_red_64dp;
                    break;
            }
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(iconResource))
                    .anchor(0.5f, 0.5f)
                    .title(call.getString("time"))
                    .visible(currTab == 1);
            mCallHistoryMarkers.put(call, mMap.addMarker(opt));
        }

        // Create adapter for call history
        mCallHistoryList.setAdapter(new CallHistoryAdapter(mCallHistory));
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
        if (mCallHistoryMarkers.isEmpty()) return;
        for (Marker mark : mFavouriteMarkers.values())
            mark.setVisible(false);
        for (Marker mark : mCallHistoryMarkers.values())
            mark.setVisible(true);
        // Select first call
        if (mCurrentMaker == null) {
            try {
                Marker m = mCallHistoryMarkers.get(mCallHistory.getJSONObject(0));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 17));
                m.showInfoWindow();
                mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mCallFab.setVisibility(View.GONE);
                mMyLocationFab.setVisibility(View.GONE);
            } catch (JSONException e) {
            }
        }
    }

    private void showFavourites() {
        if (mFavouriteMarkers.isEmpty()) return;
        for (Marker m : mCallHistoryMarkers.values())
            m.setVisible(false);
        for (Marker m : mFavouriteMarkers.values())
            m.setVisible(true);
        mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCallFab.setVisibility(View.GONE);
        mMyLocationFab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mCurrentMaker != null) {
            mCurrentMaker.remove();
            mCurrentMaker = null;
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mCallFab.setVisibility(View.GONE);
        }
        if (mTabs.getSelectedTabPosition() == 1) {
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            mMyLocationFab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mCurrentMaker != null) {
            mCurrentMaker.setPosition(latLng);
        } else {
            mCurrentMaker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true));
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mCallFab.setVisibility(View.VISIBLE);
            mMyLocationFab.setVisibility(View.GONE);
        }
        mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        updateMarkerData(latLng);
    }

    private void updateMarkerData(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String title;
        try {
            Address address = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1).get(0);
            title = address.getThoroughfare() + " " + address.getFeatureName();
        } catch (IOException | IndexOutOfBoundsException e) {
            title = "Custom location";
        }
        mMarkerDetailsTitle.setText(title);
        mMarkerDetailsLocation.setText(latLng.latitude + "," + latLng.longitude);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == mCurrentMaker) {
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mCallFab.setVisibility(View.VISIBLE);
        } else {
            // Place marker on the other marker's position
            onMapLongClick(marker.getPosition());
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mMarkerDetailsTitle.setText(null);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng latLng = marker.getPosition();
        mMarkerDetailsLocation.setText(latLng.latitude + "," + latLng.longitude);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        updateMarkerData(marker.getPosition());
    }


    public void focusMyLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(client);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(), location.getLongitude()), 17));
    }

    // Event callbacks

    public void callHistorySelected(View view) {
        Marker marker = mCallHistoryMarkers.get(view.getTag());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
        marker.showInfoWindow();
    }

    public void callHistoryRedial(View view) {
        Marker marker = mCallHistoryMarkers.get(view.getTag());
        LatLng loc = marker.getPosition();
        Toast.makeText(getBaseContext(), "Redial (h) " + loc.longitude + ", " + loc.latitude, Toast.LENGTH_LONG).show();
    }

    public void callLocation(View view) {
        LatLng loc = mCurrentMaker.getPosition();
        Toast.makeText(getBaseContext(), "Call " + loc.longitude + ", " + loc.latitude, Toast.LENGTH_LONG).show();
    }

    public void saveFavourite(View view) {
        LatLng loc = mCurrentMaker.getPosition();
        Toast.makeText(getBaseContext(), "Save " + loc.longitude + ", " + loc.latitude, Toast.LENGTH_LONG).show();
    }

    public void shareLocation(View view) {
        LatLng loc = mCurrentMaker.getPosition();
        Toast.makeText(getBaseContext(), "Share " + loc.longitude + ", " + loc.latitude, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //mMap.getUiSettings().setMapToolbarEnabled();

        // TODO: permissions
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        else mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        try {
            loadSavedData();
        } catch (JSONException e) {
            // TODO: Handle error
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
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Register for location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(client,
                    LocationRequest.create().setInterval(300000).setSmallestDisplacement(15), this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update my location to server
        new UpdateLocation().execute(location);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line; StringBuilder res = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            res.append(line);
        inputStream.close();
        return res.toString();
    }

    private class UpdateLocation extends AsyncTask<Location, Void, String> {
        @Override
        protected String doInBackground(Location... params) {
            try {
                String url = "http://angseus.ninja:3000/locations";
                HttpPost req = new HttpPost(url);

                Location loc = params[0];

                JSONObject body = new JSONObject();
                body.put("user", mUserId);
                body.put("lat", loc.getLatitude());
                body.put("lon", loc.getLongitude());

                req.setEntity(new StringEntity(body.toString(), "UTF8"));
                req.setHeader("Content-type", "application/json");

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse res = httpClient.execute(req);

                if (res.getStatusLine().getStatusCode() != 200) {
                    return null;
                }

                JSONObject data = new JSONObject(convertInputStreamToString(
                        res.getEntity().getContent()));
                return data.getString("_id");
            } catch (IOException | JSONException e) {
                // TODO: Handle error
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String id) {
            if (id != mUserId){
                SharedPreferences prefs = getPreferences(0);
                if (prefs.edit().putString("user", id).commit()){
                    mUserId = id;
                }
            }
        }
    }

    private class User {
        private String id;
        private LatLng loc;

        public User(String id, LatLng loc) {
            this.id = id;
            this.loc = loc;
        }
        public String getId() {
            return id;
        }
        public LatLng getLocation() {
            return loc;
        }
    }

    private class GetLocations extends AsyncTask<LatLng, Void, User[]> {
        private LatLng mTarget;
        @Override
        protected User[] doInBackground(LatLng... params) {
            mTarget = params[0];
            try {
                String url = "http://angseus.ninja:3000/locations?lat=" +
                        mTarget.latitude + "&lon=" + mTarget.longitude;
                HttpGet req = new HttpGet(url);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse res = httpClient.execute(req);

                if (res.getStatusLine().getStatusCode() != 200) {
                    return null;
                }

                JSONArray data = new JSONArray(convertInputStreamToString(
                        res.getEntity().getContent()));
                User[] users = new User[data.length()];
                for (int i = 0; i < users.length; i++){
                    JSONObject obj = data.getJSONObject(i);
                    users[i] = new User(obj.getString("_id"), new LatLng(
                            obj.getDouble("lat"), obj.getDouble("lon")));
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
            if (users == null || users.length == 0){
                // TODO: Handle error
                return;
            }
            Arrays.sort(users, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    LatLng lloc = lhs.getLocation(), rloc = rhs.getLocation();
                    float[] lres = new float[1], rres = new float[1];
                    Location.distanceBetween(lloc.latitude, lloc.longitude,
                            mTarget.latitude, mTarget.longitude, lres);
                    Location.distanceBetween(rloc.latitude, rloc.longitude,
                            mTarget.latitude, mTarget.longitude, rres);
                    if (lres[0] == rres[0]) return 0;
                    return lres[0] > rres[0] ? 1 : -1;
                }
            });
            User closest = users[0];

            // TODO: Initialize voip call to a user/all users
        }
    }
}