package com.callaplace.call_a_place;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Url {
    private static final String CONFIG_FILE = "config.properties";
    private static final String BASE_URL_KEY = "server";
    private static String sBaseUrl;

    private Url() {}

    public static String get(Context context, String path) {
        return String.format(base(context), path);
    }

    private static String base(Context context) {
        if (sBaseUrl == null) {
            Properties properties = new Properties();
            try {
                InputStream inputStream = context.getAssets().open(CONFIG_FILE);
                properties.load(inputStream);
            } catch (IOException e) {
                return null;
            }
            sBaseUrl = properties.getProperty(BASE_URL_KEY) + "/%s";
        }
        return sBaseUrl;
    }
}
