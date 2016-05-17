package com.callaplace.call_a_place;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener
        GoogleMap.OnMarkerDragListener,
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static String DEFAULT_FAVOURITES = "[" +
            "{'loc':[47,20]}," +
            "{'loc':[57,13]}]";
    private static String DEFAULT_CALLHISTORY = "[" +
            "{'type':'INCOMING','loc':[19,6],'time':'2016-05-14T10:10:10.010Z'}," +
            "{'type':'OUTGOING','loc':[50,15],'time':'2016-05-14T12:12:10.012Z'}," +
            "{'type':'MISSED','loc':[42,11],'time':'2016-05-14T15:15:10.015Z'}]";

    enum CallType {INCOMING, OUTGOING, MISSED};


    private String mUserId;

    private GoogleMap mMap;

    private FloatingActionButton mFab;
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

    private List<Marker> mFavouriteMarkers = new ArrayList<Marker>();
    private Map<JSONObject, Marker> mCallHistoryMarkers = new HashMap<>();
    private Marker mCurrentMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Pattern locTest = Pattern.compile("(\\d+\\.?\\d*),(\\d+\\.?\\d*)");
                Matcher m = locTest.matcher(query);
                if (m.matches()){
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

        mMarkerDetailsTitle = (TextView)  markDetBottomSheet.findViewById(R.id.titleTextView);
        mMarkerDetailsLocation = (TextView) markDetBottomSheet.findViewById(R.id.locationTextView);

        mCallHistoryList = (RecyclerView) histBottomSheet.findViewById(R.id.callHistoryList);
        mCallHistoryList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void loadSavedData() throws JSONException {
        if (mMap == null)
            throw new IllegalStateException("Map not initialized");
        SharedPreferences prefs = getPreferences(0);

        int currTab = mTabs.getSelectedTabPosition();

        // Favorites
        JSONArray favorites = new JSONArray(prefs.getString("favorites", DEFAULT_FAVOURITES));
        for (int i = 0; i < favorites.length(); i++) {
            JSONObject favorite = favorites.getJSONObject(i);
            JSONArray loc = favorite.getJSONArray("loc");
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(R.drawable.ic_star_border_gold_64dp))
                    .anchor(.5f, .5f)
                    .visible(currTab == 0);
            mFavouriteMarkers.add(mMap.addMarker(opt));
        }

        // Call history
        JSONArray history = new JSONArray(prefs.getString("history", DEFAULT_CALLHISTORY));
        for (int i = 0; i < history.length(); i++) {
            JSONObject call = history.getJSONObject(i);
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
            ;
            MarkerOptions opt = new MarkerOptions()
                    .position(new LatLng(loc.getDouble(0), loc.getDouble(1)))
                    .icon(getBitmapDescriptor(iconResource))
                    .anchor(0.5f, 0.5f)
                    .visible(currTab == 1);
            mCallHistoryMarkers.put(call, mMap.addMarker(opt));
        }

        if (mCurrentMaker == null && currTab == 1){
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        final CallHistoryAdapter adapter = new CallHistoryAdapter(history);
        mCallHistoryList.setAdapter(adapter);
        adapter.setOnCallSelectedListener(new CallHistoryAdapter.OnCallSelectedListener(){
            @Override
            public void onSelected(JSONObject call) {
                Marker m = mCallHistoryMarkers.get(call);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 17));
            }
        });
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
        for (Marker mark : mFavouriteMarkers)
            mark.setVisible(false);
        for (Marker mark : mCallHistoryMarkers.values())
            mark.setVisible(true);
        if (mCurrentMaker == null){
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void showFavourites() {
        if (mFavouriteMarkers.isEmpty()) return;
        for (Marker mark : mCallHistoryMarkers.values())
            mark.setVisible(false);
        for (Marker mark : mFavouriteMarkers)
            mark.setVisible(true);
        mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mCurrentMaker != null) {
            // Remove marker
            mCurrentMaker.remove();
            mCurrentMaker = null;
            // Hide bottom sheet
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        if (mTabs.getSelectedTabPosition() == 1){
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        if (mFavourites.isEmpty()) {
            for (LatLng pos : FAVOURITES) {
                mFavourites.add(mMap.addMarker(new MarkerOptions().position(pos)));
            }
        } else {
            mCurrentMaker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true));
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
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
        } else {
            // Place marker on the other marker's position
            onMapLongClick(marker.getPosition());
        }
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Register for location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    client, LocationRequest.create().setInterval(300000)
                            .setSmallestDisplacement(15), this);

            // Get latest known location
            Location loc = LocationServices.FusedLocationApi.getLastLocation(client);
            if (loc != null) {
                onLocationChanged(loc);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
                String url = "https://xxx/location";
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
                return data.getString("user");
            } catch (IOException | JSONException e) {
                // TODO: Handle error
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String userId) {
            if (userId != mUserId){
                SharedPreferences prefs = getPreferences(0);
                if (prefs.edit().putString("user", userId).commit()){
                    mUserId = userId;
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
                String url = "https://xxx/location?lat=" +
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
                    users[i] = new User(obj.getString("user"), new LatLng(
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