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

import java.util.Locale;

public class EventService extends FirebaseMessagingService implements Response.ErrorListener {

    private static final String TAG = "EventService";
    private static final String STATIC_MAP_API = "AIzaSyDPgh1HS_la8ZFlUD0Ymk1YqtX8qrZQGSc";

    private final Gson mGson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLanUtil.Deserializer())
            .create();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        String loc = Locale.getDefault().getCountry();
        if (remoteMessage.getData().containsKey("loc")) {
            LatLng latLng = mGson.fromJson(remoteMessage.getData().get("loc"), LatLng.class);
            loc = String.format("%f,%f", latLng.latitude, latLng.longitude);
        }
        int width = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
        int height = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_height);
        String mapIconUrl = String.format("https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=14&size=%dx%d&markers=%s&key=%s",
                loc, width, height, loc, STATIC_MAP_API);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(new ImageRequest(mapIconUrl, new SendNotification(this),
                0, 0, ImageView.ScaleType.CENTER, null, this));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Could not load image");
    }

    private static class SendNotification implements Response.Listener<Bitmap> {
        private static int id = 0;

        private final Context mContext;

        SendNotification(Context context) {
            mContext = context;
        }

        @Override
        public void onResponse(Bitmap mapImage) {
            final int notificationId = id++;

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(CancelNotification.EXTRA_ID, notificationId);
            PendingIntent piAnswer = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent dismiss = new Intent(mContext, CancelNotification.class);
            dismiss.putExtra(CancelNotification.EXTRA_ID, notificationId);
            PendingIntent piDismiss = PendingIntent.getBroadcast(
                    mContext, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            Notification.Builder notificationBuilder = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_action_answer)
                    .setLargeIcon(mapImage)
                    .setContentTitle("Call from a place!")
                    .setContentText("This is a call")
                    .setSound(defaultSoundUri, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setFullScreenIntent(piAnswer, false)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .addAction(new Notification.Action.Builder(
                            R.drawable.ic_action_answer, "Answer", piAnswer).build())
                    .addAction(new Notification.Action.Builder(
                            R.drawable.ic_action_cancel, "Dismiss", piDismiss).build());

            NotificationManager nMgr = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            nMgr.notify(notificationId, notificationBuilder.build());
        }
    }

    public static class CancelNotification extends BroadcastReceiver {
        public static final String EXTRA_ID = "android.intent.extra.notification.ID";

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            nMgr.cancel(intent.getIntExtra(EXTRA_ID, -1));
        };
    }
}
