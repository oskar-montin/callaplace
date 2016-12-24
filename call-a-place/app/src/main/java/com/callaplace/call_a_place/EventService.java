package com.callaplace.call_a_place;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventService extends FirebaseMessagingService {

    private static final String TAG = "CAP/EventService";
    private static final String STATIC_MAP_URL = "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=14&size=%dx%d&markers=%s&key=%s";
    private static final String STATIC_MAP_API = "AIzaSyDPgh1HS_la8ZFlUD0Ymk1YqtX8qrZQGSc";
    private static final Gson sGson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLanUtil.Deserializer())
            .create();

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("loc")) {
            final LatLng latLng = sGson.fromJson(remoteMessage.getData().get("loc"), LatLng.class);
            final String loc = String.format(Locale.getDefault(), "%f,%f", latLng.latitude, latLng.longitude);
            final int width = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
            final int height = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_height);
            String mapIconUrl = String.format(Locale.getDefault(), STATIC_MAP_URL,
                    loc, width, height, loc, STATIC_MAP_API);
            // Send image request
            final RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(new ImageRequest(mapIconUrl,
                    new SendNotification(this, remoteMessage),
                    0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Send notification without image
                    new SendNotification(EventService.this, remoteMessage).onResponse(null);
                }
            }));
        } else {
            // Send notification without image
            new SendNotification(this, remoteMessage).onResponse(null);
        }
    }

    private static class SendNotification implements Response.Listener<Bitmap> {
        private static int sNotificationId = 0;

        private final Context mContext;
        private final RemoteMessage mMessage;

        SendNotification(Context context, RemoteMessage message) {
            mContext = context;
            mMessage = message;
        }

        @Override
        public void onResponse(Bitmap mapImage) {
            final int notificationId = sNotificationId++;

            String caller = mMessage.getData().get("caller");

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(CancelNotification.EXTRA_ID, notificationId);
            PendingIntent piAnswer = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent dismiss = new Intent(mContext, CancelNotification.class);
            dismiss.putExtra(CancelNotification.EXTRA_ID, notificationId);
            dismiss.putExtra(CancelNotification.CALLER, caller);
            PendingIntent piDismiss = PendingIntent.getBroadcast(
                    mContext, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Notification notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_action_answer)
                    .setLargeIcon(mapImage)
                    .setContentTitle("Incoming call to your location")
                    .setSound(defaultSoundUri, new AudioAttributes.Builder().setUsage(
                            AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setFullScreenIntent(piAnswer, false)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .addAction(new Notification.Action.Builder(
                            R.drawable.ic_action_answer, "Answer", piAnswer).build())
                    .addAction(new Notification.Action.Builder(
                            R.drawable.ic_action_cancel, "Dismiss", piDismiss).build())
                    .build();
            notification.flags |= Notification.FLAG_INSISTENT;
            notification.flags |= Notification.FLAG_LOCAL_ONLY;

            final NotificationManager nMgr = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            nMgr.notify(notificationId, notification);
        }
    }

    public static class CancelNotification extends BroadcastReceiver {
        public static final String EXTRA_ID = "android.intent.extra.notification.ID";
        public static final String CALLER = "android.intent.extra.notification.CALLER";

        @Override
        public void onReceive(Context context, Intent intent) {
            final NotificationManager nMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            nMgr.cancel(intent.getIntExtra(EXTRA_ID, -1));

            final String caller = intent.getStringExtra(CALLER);
            final RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(new DismissCallRequest(context, caller).thenStopQueue(queue));
        };
    }

    public static class DismissCallRequest extends ReceiverRequest<JsonObject> {
        private static final String CALL = "call";
        private final String mCaller;

        public DismissCallRequest(Context context, String caller) {
            super(Method.DELETE, Url.get(context, CALL), sGson, new JsonObject());
            mCaller = caller;
            Log.d(TAG, "Dismiss call: " + caller);
        }
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return new HashMap<String, String>() {{
                put("caller", mCaller);
            }};
        }
    }
}
