package com.hokolinks.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.hokolinks.Hoko;

/**
 * HokoActivity serves the purpose of receiving incoming deeplinking intents and forwarding them to
 * the Deeplinking module where it will be parsed and start the associated activity.
 */
@SuppressLint("Registered")
public class HokoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String urlString = getIntent().getData().toString();
        Hoko.deeplinking().openURL(urlString);
    }
}