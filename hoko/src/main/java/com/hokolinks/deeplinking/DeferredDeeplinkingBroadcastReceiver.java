package com.hokolinks.deeplinking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hokolinks.utils.log.Log;

/**
 * Created by ivanbruel on 23/03/15.
 */
public class DeferredDeeplinkingBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(intent.toString());
    }
}
