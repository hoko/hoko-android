package com.hokolinks.deeplinking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hokolinks.Hoko;
import com.hokolinks.utils.log.Log;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by ivanbruel on 23/03/15.
 */
public class DeferredDeeplinkingBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String referrer = intent.getExtras().getString("referrer");
        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");
            Uri uri = Uri.parse("http://fakepath.com/query?" + referrer);
            String deeplink = uri.getQueryParameter("utm_content");
            Log.d("Opening deferred deeplink " + deeplink);

            Hoko.deeplinking().openURL(deeplink);
        } catch (IOException e) {
            Log.e(e);
        }


    }
}
