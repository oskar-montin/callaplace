package com.callaplace.call_a_place;

import android.location.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LocationUtil {
    static class Serializer implements JsonSerializer<Location> {
        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject dst = new JsonObject();
            dst.add("lat", new JsonPrimitive(src.getLatitude()));
            dst.add("lon", new JsonPrimitive(src.getLongitude()));
            return dst;
        }
    }
}
