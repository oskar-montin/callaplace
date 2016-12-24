package com.callaplace.call_a_place;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class AutoStart extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks {

    private PendingResult mAsyncResult;
    private GoogleApiClient mClient;
    private PendingIntent mRequestIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CAP/AutoStart", "boot received");
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != 0 &&
                ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != 0) {
            return;
        }
        mAsyncResult = goAsync();
        mClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mRequestIntent = LocationReceiver.createRequestIntent(context);
        mClient.connect();
        // Send registration token
        final String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            final RequestQueue queue = Volley.newRequestQueue(context);
            final String id = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            queue.add(new InstanceIDService.RegistrationToken(context, id, token).thenStopQueue(queue));
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("CAP/AutoStart", "requestLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mClient, LocationReceiver.createDefaultRequest(), mRequestIntent);
        mAsyncResult.finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mAsyncResult.finish();
    }
}
