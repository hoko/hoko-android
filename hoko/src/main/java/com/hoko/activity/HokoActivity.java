package com.hoko.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.hoko.Hoko;

/**
 * HokoActivity serves the purpose of receiving incoming deeplinking intents and forwarding them to
 * the HokoDeeplinking module where it will be parsed and start the associated activity.
 */
@SuppressLint("Registered")
public class HokoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Hoko.deeplinking().openURL(getIntent().getData().toString());
    }
}
