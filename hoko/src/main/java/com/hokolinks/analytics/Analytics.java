package com.hokolinks.analytics;

import android.content.Context;

import com.hokolinks.deeplinking.listeners.Handler;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.Session;
import com.hokolinks.utils.lifecycle.ApplicationLifecycle;
import com.hokolinks.utils.lifecycle.ApplicationLifecycleCallback;

/**
 * The Analytics module provides all the necessary APIs to manage user and application behavior.
 * Users should be identified to this module, as well as key events (e.g. sales, referrals, etc)
 * in order to track campaign value and allow user segmentation.
 */
public class Analytics implements Handler {

    private Context mContext;
    private String mToken;
    private Session mSession;

    /**
     * Initializing Analytics loads the previous user from storage if possible, otherwise
     * it creates a new anonymous user. Also registers for application lifecycle callbacks to
     * close opened sessions accordingly.
     *
     * @param token   A Hoko token string.
     * @param context A context object.
     */
    public Analytics(String token, Context context) {
        mContext = context;
        mToken = token;
        registerApplicationLifecycleCallbacks();
    }

    // Session

    /**
     * Ends the current user's deeplinking session in case one exists. Also posts the session
     * information to the Hoko backend and resets the current session.
     */
    private void endCurrentSession() {
        if (mSession != null) {
            mSession.end();
            mSession.post(mToken, mContext);
            mSession = null;
        }
    }

    // Handler Interface

    /**
     * Handler Interface implementation, receives a handle(deeplink) call from the
     * Deeplinking module. This will render the previous session as "ended" and will start a new
     * deeplinking session taking basis the inbound deeplink. Will also notify the Hoko backend that
     * a deeplink was opened.
     *
     * @param deeplink The deeplink object.
     */
    @Override
    public void handle(Deeplink deeplink) {
        endCurrentSession();
        mSession = new Session(deeplink);
        deeplink.post(mContext, mToken);
    }

    // Application Lifecycle

    /**
     * Registers Analytics to receive application lifecycle callbacks.
     * It will use the onPause() call to end the current deeplinking session as further data will
     * not reliably depend on a previous unbound deeplink.
     */
    private void registerApplicationLifecycleCallbacks() {
        ApplicationLifecycle.registerApplicationLifecycleCallback(mContext,
                new ApplicationLifecycleCallback() {
                    @Override
                    public void onResume() {
                    }

                    @Override
                    public void onPause() {
                        Analytics.this.endCurrentSession();
                    }
                });
    }

}
