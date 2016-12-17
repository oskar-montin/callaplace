package com.callaplace.call_a_place;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class GsonRequest<T,U> extends JsonRequest<U> {
    private final Gson mGson;
    private final Class<U> mClass;

    public GsonRequest(int method,
                       String url,
                       Gson gson,
                       T data,
                       Class<U> clazz) {
        this(method, url, gson, data, clazz, new Listener<U>(), new Listener<U>());
    }
    public GsonRequest(int method,
                       String url,
                       Gson gson,
                       T data,
                       Class<U> clazz,
                       Response.Listener<U> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, gson.toJson(data), listener, errorListener);
        mGson = gson;
        mClass = clazz;
    }

    @Override
    protected Response<U> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(mGson.fromJson(jsonString, mClass),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    private static class Listener<U> implements Response.Listener<U>, Response.ErrorListener{
        @Override
        public void onResponse(U response) {

        }        @Override
        public void onErrorResponse(VolleyError error) {}

    }
}
