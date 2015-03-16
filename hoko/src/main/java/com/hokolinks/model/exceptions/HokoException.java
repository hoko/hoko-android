package com.hokolinks.model.exceptions;

import org.json.JSONException;
import org.json.JSONObject;

public class HokoException extends Exception {


    protected HokoException(int code, String message) {
        super("Code=" + code + " Description=" + message);
    }

    public static HokoException serverException(JSONObject errorJSON) {
        try {
            if (errorJSON.has("warning")) {
                return new HokoServerWarningException(errorJSON.getInt("status"),
                        errorJSON.getString("warning"));
            } else if (errorJSON.has("error")) {
                return new HokoServerErrorException(errorJSON.getInt("status"),
                        errorJSON.getString("error"));
            } else {
                return new HokoGenericServerException();
            }
        } catch (JSONException jsonException) {
            return new HokoGenericServerException();
        }
    }


}
