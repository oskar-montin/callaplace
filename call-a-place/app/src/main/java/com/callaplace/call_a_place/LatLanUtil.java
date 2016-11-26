package com.callaplace.call_a_place;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LatLanUtil {
    static class Serializer implements JsonSerializer<LatLng> {
        @Override
        public JsonElement serialize(LatLng src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray dst = new JsonArray();
            dst.set(0, new JsonPrimitive(src.latitude));
            dst.set(1, new JsonPrimitive(src.longitude));
            return dst;
        }
    }
    static class Deserializer implements JsonDeserializer<LatLng> {
        @Override
        public LatLng deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new LatLng(json.getAsJsonArray().get(0).getAsDouble(),
                    json.getAsJsonArray().get(1).getAsDouble());
        }
    }
}
