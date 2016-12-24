package com.callaplace.call_a_place;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LocationReceiver extends BroadcastReceiver {

    private static final String TAG = "CAP/LocationReceiver";
    private static final String PREFS_FILE = "location";
    private static final String PREF_AVAILABLE = "available";
    private static final Gson sGson = new GsonBuilder()
            .registerTypeAdapter(Location.class, new LocationUtil.Serializer())
            .create();

    public static PendingIntent createRequestIntent(Context context) {
        final Intent intent = new Intent(context, LocationReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public static LocationRequest createDefaultRequest() {
        return LocationRequest.create()
                //.setInterval(5000);
                .setInterval(300000)
                .setFastestInterval(10000);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        boolean isAvailable = prefs.getBoolean(PREF_AVAILABLE, false);
        if (LocationAvailability.hasLocationAvailability(intent)) {
            isAvailable = LocationAvailability.extractLocationAvailability(intent).isLocationAvailable();
            prefs.edit().putBoolean(PREF_AVAILABLE, isAvailable).apply();
            Log.d(TAG, "LocationAvailability changed: " + Boolean.toString(isAvailable));
        }
        if (!isAvailable || !LocationResult.hasResult(intent)) {
            return;
        }
        final Location loc = LocationResult.extractResult(intent).getLastLocation();
        if (loc == null) {
            return;
        }
        final RequestQueue queue = Volley.newRequestQueue(context);
        final String id = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        queue.add(new LocationUpdate(context, id, loc).thenStopQueue(queue));
    }

    private static class LocationUpdate extends ReceiverRequest<LocationUpdate.Body> {
        private static final String LOCATION = "location";

        public LocationUpdate(Context context, String id, Location loc) {
            super(Method.POST, Url.get(context, LOCATION), sGson, new LocationUpdate.Body(id, loc));
            Log.d(TAG, "Sending location update: " + sGson.toJson(loc));
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
}
