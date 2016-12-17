package com.callaplace.call_a_place;

import android.content.Context;

public enum Url {
    LOCATION("location"),
    CALL("call"),
    DEVICE("device", true);

    private static final String CONFIG_FILE = "config.properties";
    private static final String BASE_URL_KEY = "server";
    private static String sBaseUrl;

    private String mValue;
    private final boolean mUseIdPath;

    Url(String key){
        this(key, false);
    }
    Url(String key, boolean useIdPath){
        mValue = key;
        mUseIdPath = useIdPath;
    }

    public String val() {
        return val(null);
    }
    public String val(String id) {
        return String.format(mValue, sBaseUrl, id);
    }

    public static void load(Context context) {




    }
}
