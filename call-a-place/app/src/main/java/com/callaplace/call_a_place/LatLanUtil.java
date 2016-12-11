package com.callaplace.call_a_place;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LatLanUtil {
    static class Serializer implements JsonSerializer<LatLng> {
        @Override
        public JsonElement serialize(LatLng src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject dst = new JsonObject();
            dst.add("lat", new JsonPrimitive(src.latitude));
            dst.add("lon", new JsonPrimitive(src.longitude));
            return dst;
        }
    }
    static class Deserializer implements JsonDeserializer<LatLng> {
        @Override
        public LatLng deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new LatLng(
                    json.getAsJsonObject().get("lat").getAsDouble(),
                    json.getAsJsonObject().get("lon").getAsDouble());
        }
    }
}

