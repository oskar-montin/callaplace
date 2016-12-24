package com.callaplace.call_a_place;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;

public class ReceiverRequest<T> extends GsonRequest<T, Void> {
    private RequestQueue mQueue;

    public ReceiverRequest(int method, String url, Gson gson, T data) {
        super(method, url, gson, data, Void.class, null, null);
        setShouldCache(false);
    }
    public ReceiverRequest<T> thenStopQueue(RequestQueue queue) {
        mQueue = queue;
        return this;
    }

    @Override
    protected void deliverResponse(Void response) {
        if (mQueue != null) {
            mQueue.stop();
        }
    }
}
