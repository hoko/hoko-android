package com.hokolinks.pushnotifications;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hokolinks.Hoko;
import com.hokolinks.model.App;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.URL;
import com.hokolinks.utils.log.HokoLog;


/**
 * An IntentService to receive hoko push notification intents.
 * It creates the push notification with the pending deeplinking intent and a message which comes
 * from the Hoko platform.
 */
@SuppressLint("Registered")
public class NotificationHandler extends IntentService {

    // Key strings to parse incoming Hoko notifications.
    public static final String HokoNotificationHandlerMessageKey = "hoko_message";
    public static final String HokoNotificationHandlerDeeplinkURLStringKey = "hoko_deeplink";
    private static final String HokoNotificationHandlerDefaultMessage = "Hoko Notification";
    private static final int HokoNotificationBaseIdentifier = 9001;

    public NotificationHandler() {
        super("GcmIntentService");
    }

    /**
     * Getter for the launch intent for the current application.
     *
     * @return The launch intent.
     */
    private Intent getLaunchIntent() {
        return getPackageManager().getLaunchIntentForPackage(getPackageName());
    }

    /**
     * The intent handler, parses the incoming push notification intent and presents the actual
     * push notification.
     *
     * @param intent The push notification intent.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
                try {
                    handleNotification(this, intent);
                } catch (Exception e) {
                    HokoLog.e(e);
                }
            }
        }
    }

    /**
     * The parsing of the notification intent happens here. It creates a push notification
     * with the application's name as title, the application's icon as the image and sets the
     * message text from what the Hoko platform sent. Also gets the deeplinking intent from
     * the HokoDeeplinking module to set it to the notification.
     *
     * @param context The Handler context.
     * @param intent  The push notification intent.
     */
    private void handleNotification(Context context, Intent intent) {
        String message = intent.getStringExtra(HokoNotificationHandlerMessageKey);
        if (message == null)
            message = HokoNotificationHandlerDefaultMessage;
        String urlString = intent.getStringExtra(HokoNotificationHandlerDeeplinkURLStringKey);
        int icon = App.getIcon(context);
        String title = App.getName(context);
        Intent launchIntent = getLaunchIntent();
        int notificationId = HokoNotificationBaseIdentifier;
        if (urlString != null) {
            URL hokoURL = new URL(urlString);
            try {
                notificationId = hokoURL.getQueryParameters().get(Deeplink.HokoDeeplinkOpenLinkIdentifierKey).hashCode() % HokoNotificationBaseIdentifier;
            } catch (Exception e) {
                HokoLog.e(e);
            }
            Intent deeplinkingIntent = Hoko.deeplinking().routing().intentForURL(hokoURL);
            if (deeplinkingIntent != null)
                launchIntent = deeplinkingIntent;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(
                context.getApplicationContext(),
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        showNotification(context, contentIntent, icon, title, message, notificationId);

    }

    /**
     * Creates and shows an actual push notification on the device independent on the platform
     * API level.
     *
     * @param context    The Handler context.
     * @param intent     The pending intent.
     * @param icon       The icon to show on the notification.
     * @param title      The title to show on the notification.
     * @param body       The message to show on the notification.
     * @param identifier The identifier for the notification.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(16)
    private void showNotification(Context context, PendingIntent intent, int icon, String title, String body, int identifier) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(icon);
        builder.setLargeIcon(App.getIconBitmapForNotification(context));
        builder.setTicker(body);
        builder.setContentText(body);
        if (body.length() > 30) {
            builder.setStyle(new Notification.BigTextStyle().bigText(body).setBigContentTitle(title));
        }
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(soundUri, AudioManager.STREAM_NOTIFICATION);
        Notification notification;
        if (Build.VERSION.SDK_INT < 16) {
            notification = builder.getNotification();
        } else {
            notification = builder.build();
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notification.defaults |= Notification.DEFAULT_ALL;
        notificationManager.notify(identifier, notification);
    }

}