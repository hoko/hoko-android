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
            openDeeplink(urlString, null);
        }
    }

    @Override
    public void onLinkResolved(String deeplink, JSONObject metadata) {
        finish();
        openDeeplink(deeplink, metadata);
    }

    @Override
    public void onError(Exception e) {
        finish();
        openDeeplink(null, null);
    }

    private void openDeeplink(String deeplink, JSONObject metadata) {
        Hoko.deeplinking().openURL(deeplink, metadata);
    }
}
