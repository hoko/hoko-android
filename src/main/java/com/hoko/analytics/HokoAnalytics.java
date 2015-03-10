package com.hoko.analytics;

import android.content.Context;

import com.hoko.deeplinking.listeners.HokoHandler;
import com.hoko.model.HokoDeeplink;
import com.hoko.model.HokoEvent;
import com.hoko.model.HokoSession;
import com.hoko.model.HokoUser;
import com.hoko.model.exceptions.HokoIgnoringKeyEventException;
import com.hoko.utils.lifecycle.HokoApplicationLifecycle;
import com.hoko.utils.lifecycle.HokoApplicationLifecycleCallback;
import com.hoko.utils.log.HokoLog;

import java.util.Date;

/**
 * The HokoAnalytics module provides all the necessary APIs to manage user and application behavior.
 * Users should be identified to this module, as well as key events (e.g. sales, referrals, etc) in order
 * to track campaign value and allow user segmentation.
 */
public class HokoAnalytics implements HokoHandler {

    private Context mContext;
    private String mToken;
    private HokoUser mUser;
    private HokoSession mSession;

    /**
     * Initializing HokoAnalytics loads the previous user from storage if possible, otherwise
     * it creates a new anonymous user. Also registers for application lifecycle callbacks to
     * close opened sessions accordingly.
     *
     * @param token   A Hoko token string.
     * @param context A context object.
     */
    public HokoAnalytics(String token, Context context) {
        mContext = context;
        mToken = token;
        mUser = HokoUser.currentUser(context);
        if (mUser == null) {
            identifyUser();
        } else {
            mUser.post(mToken, mContext);
        }
        registerApplicationLifecycleCallbacks();
    }

    // User Identification

    /**
     * identifyUser() should be called if you have no information about the user. (e.g. your app has no
     * login whatsoever) or if the application's user has logged out of his account.
     * <pre>{@code
     * Hoko.analytics().identifyUser();
     * }</pre>
     */
    public void identifyUser() {
        if (mUser == null || !mUser.isAnonymous()) {
            endCurrentSession();
            mUser = new HokoUser();
            mUser.save(mContext);
            mUser.post(mToken, mContext);
        }
    }

    /**
     * identifyUser(identifier, accountType) should be called when you can identify the user with a
     * unique identifier and a given account type.
     * <pre>{@code
     * Hoko.analytics().identifyUser("john.doe@email.com", HokoAnalytics.HokoUserAccountType.DEFAULT);
     * }</pre>
     *
     * @param identifier  A unique identifier for the user in the scope of your application.
     * @param accountType The account type in which the user fits.
     */
    public void identifyUser(String identifier, HokoUserAccountType accountType) {
        identifyUser(identifier, accountType, null, null, null, HokoUserGender.UNKNOWN);
    }

    /**
     * identifyUser(identifier, accountType) should be called when you can identify the user with
     * a unique identifier, a given account type, and a few attributes which help to segment users
     * in the Hoko service.
     * <pre>{@code
     * Hoko.analytics().identifyUser("john.doe", HokoAnalytics.HokoUserAccountType.GITHUB, "John Doe", "john.doe@email.com", new Date(), HokoAnalytics.HokoUserGender.MALE);
     * }</pre>
     *
     * @param identifier  A unique identifier for the user in the scope of your application.
     * @param accountType The account type in which the user fits.
     * @param name        The user's name.
     * @param email       The user's email address.
     * @param birthDate   The user's date of birth.
     * @param gender      The user's gender (Male/Female/Unknown).
     */
    public void identifyUser(String identifier, HokoUserAccountType accountType, String name, String email, Date birthDate, HokoUserGender gender) {
        if (mUser != null || !identifier.equalsIgnoreCase(mUser.getIdentifier())) {
            String previousIdentifier = mUser.isAnonymous() ? mUser.getIdentifier() : null;
            mUser = new HokoUser(identifier, accountType, null, email, birthDate, gender, previousIdentifier);
            mUser.save(mContext);
            mUser.post(mToken, mContext);
            if (previousIdentifier == null) {
                endCurrentSession();
            } else if (mSession != null) {
                mSession.setUser(mUser);
            }
        }
    }

    // Events

    /**
     * trackKeyEvent(eventName) unlike common analytics events should be used only on conversion or
     * key metrics (e.g. in-app purchase, retail sales, referrals, etc). This will lead to better
     * conversion and engagement tracking of your users through the Push Notifications and
     * Deeplinking campaigns.
     * <pre>{@code
     * Hoko.analytics().trackKeyEvent("purchasedPremium");
     * }</pre>
     *
     * @param eventName A name to identify uniquely the key event that occurred.
     */
    public void trackKeyEvent(String eventName) {
        trackKeyEvent(eventName, 0);
    }

    /**
     * trackKeyEvent: unlike common analytics events should be used only on conversion or key metrics
     * (e.g. in-app purchase, retail sales, referrals, etc). This will lead to better conversion and
     * engagement tracking of your users through the Push Notifications and Deeplinking campaigns.
     * <pre>{@code
     * Hoko.analytics().trackKeyEvent("purchasedPremium", 29.99);
     * }</pre>
     *
     * @param eventName A name to identify uniquely the key event that occurred.
     * @param amount    A number that represents a possible sale (e.g. in-app, retail, etc) in currency value.
     */
    public void trackKeyEvent(String eventName, double amount) {
        HokoEvent event = new HokoEvent(eventName, amount);
        if (mSession != null) {
            mSession.trackKeyEvent(event);
        } else {
            HokoLog.e(new HokoIgnoringKeyEventException(event));
        }
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

    // HokoHandler Interface

    /**
     * HokoHandler Interface implementation, receives a handle(deeplink) call from the HokoDeeplinking
     * module. This will render the previous session as "ended" and will start a new deeplinking
     * session taking basis the inbound deeplink. Will also notify the Hoko backend that a deeplink
     * was opened.
     *
     * @param deeplink The deeplink object.
     */
    @Override
    public void handle(HokoDeeplink deeplink) {
        endCurrentSession();
        mSession = new HokoSession(mUser, deeplink);
        deeplink.post(mContext, mToken, mUser);
    }

    // Application Lifecycle

    /**
     * Registers HokoAnalytics to receive application lifecycle callbacks.
     * It will use the onPause() call to end the current deeplinking session as further data will
     * not reliably depend on a previous unbound deeplink.
     */
    private void registerApplicationLifecycleCallbacks() {
        HokoApplicationLifecycle.registerApplicationLifecycleCallback(mContext, new HokoApplicationLifecycleCallback() {
            @Override
            public void onResume() {
            }

            @Override
            public void onPause() {
                HokoAnalytics.this.endCurrentSession();
            }
        });
    }

}
