package com.hokolinks.pushnotifications;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.hokolinks.Hoko;

/**
 * A WakefulBroadcastReceiver to receive push notification intents from the Google Cloud
 * Messaging platform. It starts the HokoNotificationHandler to parse and show the actual
 * notification.
 */
public class NotificationReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Hoko.pushNotifications().openPushNotification(this, context, intent);
    }
}
