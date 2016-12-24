package com.callaplace.call_a_place;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.Gson;

public class InstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "CAP/InstanceIDService";
    private static final Gson sGson = new Gson();

    @Override
    public void onTokenRefresh() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        final String id = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String token = FirebaseInstanceId.getInstance().getToken();
        queue.add(new RegistrationToken(this, id, token).thenStopQueue(queue));
    }

    static class RegistrationToken extends ReceiverRequest<RegistrationToken.Body> {
        private static final String TOKEN = "token";

        public RegistrationToken(Context context, String id, String token) {
            super(Method.POST, Url.get(context, TOKEN), sGson, new Body(id, token));
            Log.d(TAG, "Sending registration token: " + token);
        }
        static class Body {
            private final String id;
            private final String token;
            Body(String id, String token) {
                this.id = id;
                this.token = token;
            }
        }
    }
}
