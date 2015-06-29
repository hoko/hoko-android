package com.hokolinks.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.SmartlinkResolveListener;

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
        if (urlString.startsWith("http") && urlString.contains("hoko.link")) {
            Hoko.deeplinking().openSmartlink(urlString, this);
        } else {
            openDeeplink(urlString);
        }
    }

    @Override
    public void onLinkResolved(String deeplink) {
        finish();
        openDeeplink(deeplink);
    }

    @Override
    public void onError(Exception e) {
        finish();
        openDeeplink(null);
    }

    private void openDeeplink(String deeplink) {
        Hoko.deeplinking().openURL(deeplink);
    }
}
