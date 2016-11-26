package com.callaplace.call_a_place;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public enum CallType {
    INCOMING(R.drawable.ic_call_received_green_24dp),
    OUTGOING(R.drawable.ic_call_made_green_24dp),
    MISSED(R.drawable.ic_call_missed_red_24dp);

    private final int mIcon;

    CallType(int icon) {
        mIcon = icon;
    }

    public int getIcon() {
        return mIcon;
    }

    static class Serializer implements JsonSerializer<CallType> {
        @Override
        public JsonElement serialize(CallType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
    static class Deserializer implements JsonDeserializer<CallType> {
        @Override
        public CallType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return CallType.valueOf(json.getAsString());
        }
    }
}
