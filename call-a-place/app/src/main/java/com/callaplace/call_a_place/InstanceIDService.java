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
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String id = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        requestQueue.add(new RegistrationToken(this, id, refreshedToken));
    }

    static class RegistrationToken extends GsonRequest<RegistrationToken.Body, Void> {
        private static final String TOKEN = "token";

        public RegistrationToken(Context context, String id, String token) {
            super(Method.POST, Url.get(context, TOKEN), sGson, new Body(id, token), Void.TYPE);
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
