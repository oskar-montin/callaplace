package com.callaplace.call_a_place;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, LocationListener, GoogleApiClient.OnConnectionFailedListener, Response.Listener<String>, Response.ErrorListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnPoiClickListener, PlaceSelectionListener, GoogleMap.OnCameraIdleListener, CallHistoryAdapter.OnCallSelectedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int TAB_FAVOURITES = 0;
    private static final int TAB_HISTORY = 1;

    // todo: remove test data
    private static String DEFAULT_FAVOURITES = "[" +
            "{loc:{lat:57.690,lon:11.977}}," +
            "{loc:{lat:57.690,lon:11.972}}]";
    private static String DEFAULT_CALLHISTORY = "[" +
            "{type:'INCOMING',loc:{lat:57.689,lon:11.974},time:'2016-05-14T10:10:10.010Z'}," +
            "{type:'OUTGOING',loc:{lat:57.686,lon:11.967},time:'2016-05-14T12:12:10.012Z'}," +
            "{type:'MISSED',loc:{lat:57.688,lon:11.979},time:'2016-05-14T15:15:10.015Z'}]";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLanUtil.Serializer())
            .registerTypeAdapter(LatLng.class, new LatLanUtil.Deserializer())
            .registerTypeAdapter(Location.class, new LocationUtil.Serializer())
            .registerTypeAdapter(CallType.class, new CallType.Serializer())
            .registerTypeAdapter(CallType.class, new CallType.Deserializer())
            .registerTypeAdapter(Date.class, new ISO8601.Serializer())
            .registerTypeAdapter(Date.class, new ISO8601.Deserializer())
            .create();

    private RequestQueue mRequestQueue;
    private Geocoder mGeocoder;

    private GoogleMap mMap;

    private String mDeviceId;

    private Collection<Favorite> mFavorites;
    private List<RecentCall> mCallHistory;

    // Views
    private TabLayout mTabs;
    private FloatingActionButton mFab;

    private PlaceAutocompleteFragment mAutocompleteFragment;

    private BottomSheetBehavior mMarkerDetailsBottomSheetBehavior;
    private BottomSheetBehavior mCallHistoryBottomSheetBehavior;

    private TextView mMarkerDetailsTitle;
    private TextView mMarkerDetailsLocation;

    private Button mMarkerDetailsSaveButton;

    private RecyclerView mCallHistoryList;

    private Map<Favorite, Marker> mFavoriteMarkers = new HashMap<>();
    private Map<RecentCall, Marker> mCallHistoryMarkers = new HashMap<>();
    private Marker mCurrentMaker;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onNewIntent(Intent intent) {
        // todo: launch call activity
        NotificationManager nMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancel(intent.getIntExtra(EventService.CancelNotification.EXTRA_ID, -1));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);
        mGeocoder = new Geocoder(this);

        mDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Send token to server
        String token = FirebaseInstanceId.getInstance().getToken();
        mRequestQueue.add(new InstanceIDService.RegistrationToken(this, mDeviceId, token));

        mTabs = (TabLayout) findViewById(R.id.tabLayout);
        mTabs.setOnTabSelectedListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.search_suggestions);
        mAutocompleteFragment.setOnPlaceSelectedListener(this);
        mAutocompleteFragment.setHint("Search places");

        // Initialize map
        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final LinearLayout markerDetailsBottomSheet = (LinearLayout) findViewById(R.id.markerDetailsBottomSheet);
        final LinearLayout callHistoryBottomSheet = (LinearLayout) findViewById(R.id.callHistoryBottomSheet);
        mMarkerDetailsBottomSheetBehavior = BottomSheetBehavior.from(markerDetailsBottomSheet);
        mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCallHistoryBottomSheetBehavior = BottomSheetBehavior.from(callHistoryBottomSheet);
        mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mMarkerDetailsBottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        boolean hidden = newState == STATE_HIDDEN;
                        mFab.setVisibility(hidden ? View.INVISIBLE : View.VISIBLE);
                        if (hidden) {
                            removeMarker();
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });

        mMarkerDetailsTitle = (TextView) markerDetailsBottomSheet.findViewById(R.id.titleTextView);
        mMarkerDetailsLocation = (TextView) markerDetailsBottomSheet.findViewById(R.id.locationTextView);

        mMarkerDetailsSaveButton = (Button) markerDetailsBottomSheet.findViewById(R.id.saveButton);

        mCallHistoryList = (RecyclerView) callHistoryBottomSheet.findViewById(R.id.callHistoryList);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(AppIndex.API)
                .addConnectionCallbacks(this)
                .build();
        // todo: permissions
        //ActivityCompat.requestPermissions(this, new String[]{
        // Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void loadSavedData() {
        mFavorites = gson.fromJson(DEFAULT_FAVOURITES, new TypeToken<Collection<Favorite>>() {
        }.getType());
        for (Favorite fav : mFavorites) {
            MarkerOptions opt = new MarkerOptions()
                    .position(fav.loc)
                    .anchor(.5f, .5f)
                    .visible(false)
                    .icon(getBitmapDescriptor(R.drawable.ic_star_border_gold_64dp));
            Marker marker = mMap.addMarker(opt);
            marker.setTag(TAB_FAVOURITES);
            mFavoriteMarkers.put(fav, marker);
        }
        mCallHistory = gson.fromJson(DEFAULT_CALLHISTORY, new TypeToken<ArrayList<RecentCall>>() {
        }.getType());
        Collections.sort(mCallHistory);
        for (RecentCall call : mCallHistory) {
            MarkerOptions opt = new MarkerOptions()
                    .position(call.loc)
                    .anchor(.5f, .5f)
                    .visible(false)
                    .icon(getBitmapDescriptor(call.type.getIcon()))
                    .title(getMarkerAddress(call.loc));
            Marker marker = mMap.addMarker(opt);
            marker.setTag(TAB_HISTORY);
            mCallHistoryMarkers.put(call, marker);
        }
        final CallHistoryAdapter adapter = new CallHistoryAdapter(mCallHistory, mGeocoder);
        mCallHistoryList.setAdapter(adapter);
        adapter.setOnCallSelectedListener(this);
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
        for (Marker mark : mFavoriteMarkers.values()) {
            mark.setVisible(false);
        }
        for (Marker mark : mCallHistoryMarkers.values()) {
            mark.setVisible(true);
        }
        // Select first call
        if (mCurrentMaker == null && !mCallHistory.isEmpty()) {
            Marker marker = mCallHistoryMarkers.get(mCallHistory.get(0));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            marker.showInfoWindow();
            mCallHistoryBottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    private void showFavourites() {
        for (Marker marker : mCallHistoryMarkers.values()) {
            marker.setVisible(false);
        }
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Marker marker : mFavoriteMarkers.values()) {
            marker.setVisible(true);
            boundsBuilder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 256));
        mCallHistoryBottomSheetBehavior.setState(STATE_HIDDEN);
    }

    private void placeMarker(LatLng latLng, int bottomSheetState) {
        placeMarker(latLng, bottomSheetState, null);
    }

    private void placeMarker(LatLng latLng, int bottomSheetState, String title) {
        if (mCurrentMaker != null) {
            mCurrentMaker.setPosition(latLng);
        } else {
            mCurrentMaker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
        }
        mCallHistoryBottomSheetBehavior.setState(STATE_HIDDEN);
        mMarkerDetailsBottomSheetBehavior.setState(bottomSheetState);
        updateMarkerData(latLng, title);
    }

    private void updateMarkerData(LatLng latLng, String title) {
        mMarkerDetailsTitle.setText(title != null ? title : getMarkerAddress(latLng));
        mMarkerDetailsLocation.setText(String.format("%.3f, %.3f", latLng.latitude, latLng.longitude));
        boolean isFavourite = isFavourite(latLng);
        mMarkerDetailsSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, isFavourite
                ? R.drawable.ic_star_white_24dp
                : R.drawable.ic_star_border_white_24dp, 0, 0);
        mMarkerDetailsSaveButton.setText(isFavourite ? "SAVED" : "SAVE");
    }

    private String getMarkerAddress(LatLng latLng) {
        try {
            Address address = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            return address.getThoroughfare() + " " + address.getFeatureName();
        } catch (IOException | IndexOutOfBoundsException e) {
            return "Custom location";
        }
    }

    private boolean isFavourite(LatLng latLng) {
        return mFavoriteMarkers.containsKey(latLng);
    }

    private void removeMarker() {
        if (mCurrentMaker != null) {
            mCurrentMaker.remove();
            mCurrentMaker = null;
            mMarkerDetailsBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        if (mTabs.getSelectedTabPosition() == TAB_HISTORY) {
            mCallHistoryBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        placeMarker(latLng, STATE_COLLAPSED);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        placeMarker(latLng, STATE_EXPANDED);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(mCurrentMaker)) {
            removeMarker();
        } else if ((int) marker.getTag() == TAB_FAVOURITES) {
            placeMarker(marker.getPosition(), STATE_EXPANDED);
        } else if ((int) marker.getTag() == TAB_HISTORY) {
            removeMarker();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            marker.showInfoWindow();
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDrag(Marker marker) {
        updateMarkerData(marker.getPosition(), "");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        updateMarkerData(marker.getPosition(), null);
    }

    @Override
    public void onSelected(RecentCall call) {
        Marker marker = mCallHistoryMarkers.get(call);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
        marker.showInfoWindow();
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        placeMarker(pointOfInterest.latLng, STATE_COLLAPSED, pointOfInterest.name);
    }

    @Override
    public void onCameraIdle() {
        mAutocompleteFragment.setBoundsBias(mMap.getProjection().getVisibleRegion().latLngBounds);
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i("TAG", "Place: " + place.getName());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17));
        placeMarker(place.getLatLng(), STATE_COLLAPSED, place.getName().toString());
    }

    @Override
    public void onError(Status status) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // todo: permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // req
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        loadSavedData();

        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnPoiClickListener(this);
        mMap.setOnCameraIdleListener(this);
    }

    @Override
    public void onMapLoaded() {
        onTabSelected(mTabs.getSelectedTabPosition());
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        onTabSelected(tab.getPosition());
    }

    private void onTabSelected(int position) {
        switch (position) {
            case TAB_FAVOURITES:
                showFavourites();
                return;
            case TAB_HISTORY:
                showCallHistory();
                return;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab.getPosition());
    }

    @Override
    public void onStart() {
        super.onStart();
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
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Register for location updates
            LocationRequest request = LocationRequest.create()
                    .setInterval(300000)
                    .setSmallestDisplacement(15);
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
            // Initialize map to last known position
            Location loc = LocationServices.FusedLocationApi.getLastLocation(client);
            if (loc != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(loc.getLatitude(), loc.getLongitude()), 17));
                onLocationChanged(loc);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mRequestQueue.add(new LocationUpdate(this, gson, mDeviceId, location));
        }
    }

    public void saveButtonToggle(View view) {
        boolean isFavourite = isFavourite(mCurrentMaker.getPosition());
        mMarkerDetailsSaveButton.setText(isFavourite ? "SAVED" : "SAVE");
    }

    public void callButtonClick(View view) {
        // Send call command to server
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
        if (mCurrentMaker != null) {
            mRequestQueue.add(new CallRequest(this, mDeviceId, mCurrentMaker.getPosition(), this, this));
            /*
            try {
                mRequestQueue.add(new CallRequest(this, mDeviceId, mCurrentMaker.getPosition(), this, this));
            } catch (IOException e) {}
            */
        }
    }

    @Override
    public void onResponse(String remoteDevice) {
        Toast.makeText(this, "todo: call " + remoteDevice, Toast.LENGTH_LONG).show();

        /*
        String id = response.optString("_id");
        if (id != null && id != mDeviceId) {
            SharedPreferences prefs = getPreferences(0);
            if (prefs.edit().putString("user", id).commit()){
                mDeviceId = id;
                Toast.makeText(this, "ID: " + id, Toast.LENGTH_LONG).show();
            }
        }
        */
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "No one there", Toast.LENGTH_SHORT).show();
    }

    private static class LocationUpdate extends GsonRequest<LocationUpdate.Body, Void> {
        private static final String LOCATION = "location";

        public LocationUpdate(Context context, Gson gson, String id, Location loc) {
            super(Method.POST, Url.get(context, LOCATION), gson, new Body(id, loc), Void.TYPE);
        }
        static class Body {
            private final String id;
            private final Location loc;
            Body(String id, Location loc) {
                this.id = id;
                this.loc = loc;
            }
        }
    }

    private static class CallRequest extends StringRequest {
        private static final String CALL = "call";

        public CallRequest(Context context, String id, LatLng loc, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Method.GET, Url.get(context, CALL) + createQuery(id, loc), listener, errorListener);
        }

        private static String createQuery(String id, LatLng loc) {
            return String.format("?lat=%f&lon=%f&exclude=%s", loc.latitude, loc.longitude, id);
        }
    }
}