package com.callaplace.call_a_place;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        setupBottomSheet();
        setupTabIconTints();

        final TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setOnTabSelectedListener(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        TextView titleTextView = (TextView) bottomSheet.findViewById(R.id.titleTextView);
        TextView locationTextView = (TextView) bottomSheet.findViewById(R.id.locationTextView);
        titleTextView.setText("INSERT TITLE HERE");
        locationTextView.setText("INSERT LOCATION HERE");

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
}
