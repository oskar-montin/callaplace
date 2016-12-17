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

    private static final String TAG = "InstanceIDService";

    private String mDeviceId;

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        if (mDeviceId == null) {
            mDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(new RegistrationToken(this, mDeviceId, refreshedToken));
    }

    static class RegistrationToken extends GsonRequest<RegistrationToken.Body, Void> {
        private static final String TOKEN = "token";
        private static final Gson sGson = new Gson();

        public RegistrationToken(Context context, String id, String token) {
            super(Method.POST, Url.get(context, TOKEN), sGson, new Body(id, token), Void.TYPE);
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
