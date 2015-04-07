package com.hokolinks.pushnotifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hokolinks.Hoko;
import com.hokolinks.model.Device;
import com.hokolinks.model.exceptions.NoGooglePlayServicesException;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import java.io.IOException;

/**
 * The HokoPushNotifications module provides all the necessary APIs to manage push notifications generated
 * by the Hoko service.
 */
public class PushNotifications {

    private Context mContext;
    private String mGCMSenderId;

    public PushNotifications(Context context, String gcmSenderId) {
        mContext = context;
        mGCMSenderId = gcmSenderId;

        if (hasGooglePlayServices()) {
            requestPushToken();
        } else {
            HokoLog.e(new NoGooglePlayServicesException());
        }

    }

    /**
     * Detects if the device has Google Play Services installed and if they are up-to-date.
     *
     * @return true in case they are available, false otherwise.
     */
    private boolean hasGooglePlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
            return resultCode == ConnectionResult.SUCCESS;
        } catch (Error e) {
            HokoLog.e(new NoGooglePlayServicesException());
            HokoLog.e(e);
        }
        return false;
    }

    /**
     * Requests the push notification token from the Google Cloud Messaging platform and sets it
     * in the HokoAnalytics platform to update the current user information.
     * This is wrapped around an AsyncTask to avoid problems with the GoogleCloudMessaging
     * background thread requirements.
     */
    private void requestPushToken() {
        new NetworkAsyncTask(new Runnable() {
            @Override
            public void run() {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
                try {
                    String pushToken = gcm.register(mGCMSenderId);
                    if (pushToken != null) {
                        String oldToken = Device.getPushToken(mContext);

                        if (oldToken == null || !oldToken.equals(pushToken)) {
                            Hoko.analytics().setPushToken(pushToken);
                        }
                    }
                } catch (IOException e) {
                    HokoLog.e(e);
                }
            }
        }).execute();
    }

    /**
     * API to be called when a push notification is received. In case you implement your own
     * push notification broadcast receiver you should call this method and check its return
     * value before handling the push notification yourself. Ignore this method if you are using
     * the HokoNotificationReceiver as your only push notification receiver.
     * <pre>{@code
     * if (!Hoko.pushNotifications().openPushNotification(this, context, intent)) {
     *     // Your push notifications code here
     * }
     * }</pre>
     *
     * @param wakefulBroadcastReceiver Your wakeful broadcast receiver for push notifications.
     * @param context                  The context it receives.
     * @param intent                   The intent it receives.
     * @return true if the notification was handled by Hoko, false otherwise.
     */
    public boolean openPushNotification(WakefulBroadcastReceiver wakefulBroadcastReceiver, Context context, Intent intent) {
        String deeplinkURL = intent.getStringExtra(NotificationHandler.HokoNotificationHandlerDeeplinkURLStringKey);
        String message = intent.getStringExtra(NotificationHandler.HokoNotificationHandlerMessageKey);
        if ((deeplinkURL == null || deeplinkURL.length() == 0) && (message == null || message.length() == 0))
            return false;
        else {
            ComponentName comp = new ComponentName(context.getPackageName(),
                    NotificationHandler.class.getName());
            // Start the service, keeping the device awake while it is launching.
            WakefulBroadcastReceiver.startWakefulService(context, (intent.setComponent(comp)));
            wakefulBroadcastReceiver.setResultCode(Activity.RESULT_OK);
            return true;
        }
    }
}
