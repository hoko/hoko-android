package com.hokolinks.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.SmartlinkResolveListener;

import org.json.JSONObject;

/**
 * HokoActivity serves the purpose of receiving incoming deeplinking intents and forwarding them to
 * the Deeplinking module where it will be parsed and start the associated activity.
 */
@SuppressLint("Registered")
public class HokoActivity extends Activity implements SmartlinkResolveListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String urlString = getIntent().getData().toString();
        if (urlString.startsWith("http")) {
            Hoko.deeplinking().openSmartlink(urlString, this);
        } else {
            Hoko.deeplinking().openURL(urlString);
        }
    }

    @Override
    public void onLinkResolved(String deeplink, JSONObject metadata) {
        finish();
    }

    @Override
    public void onError(Exception e) {
        finish();
        Hoko.deeplinking().openURL(null);
    }

}