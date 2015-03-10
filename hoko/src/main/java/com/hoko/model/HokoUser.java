package com.hoko.model;

import android.content.Context;

import com.hoko.analytics.HokoUserAccountType;
import com.hoko.analytics.HokoUserGender;
import com.hoko.utils.HokoDateUtils;
import com.hoko.utils.HokoUtils;
import com.hoko.utils.log.HokoLog;
import com.hoko.utils.networking.HokoNetworking;
import com.hoko.utils.networking.async.HokoHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

/**
 * HokoUser is the representation of a User, with a few properties to allow user segmentation
 * on the Hoko platform. There are 2 kinds of users: anonymous and identified.
 * Anonymous users have no attributes are exist solely for the purpose of tracking individual users.
 * Identified users have attributes which can later be used for segmentation in Hokolink redirection.
 * <p>
 * When identifying a user with an identifier it will associated the previous anonymous user
 * (in case it exists) the new user being created. This helps with dealing with duplicate users
 * especially when applications have Onboarding and Logged in contexts, where it is actually the
 * same user.
 */
public class HokoUser implements Serializable {

    // Key for the saving/loading the current user from file.
    private static final String HokoUserCurrentUserKey = "current_user";

    private String mIdentifier;
    private boolean mAnonymous;
    private HokoUserAccountType mAccountType;
    private String mName;
    private String mEmail;
    private Date mBirthDate;
    private HokoUserGender mGender;
    private String mPreviousIdentifier;
    private float mTimezoneOffset;

    /**
     * This constructs an anonymous HokoUser with an automatically generated identifier and no
     * attributes.
     */
    public HokoUser() {
        this(null, HokoUserAccountType.NONE, null, null, null, HokoUserGender.UNKNOWN, null);
    }

    /**
     * This constructs an identified HokoUser with the given attributes.
     * The previous identifier serves the purpose of merging the previous anonymous user data with
     * the HokoUser object being initialized here. This will also retrieve the current timezone.
     *
     * @param identifier         A unique identifier for users of the application.
     * @param accountType        The login system user to identify this user.
     * @param name               The name of the user.
     * @param email              The user's email address.
     * @param birthDate          The user's birth date.
     * @param gender             The user's gender.
     * @param previousIdentifier The previous identifier in case the previous user was anonymous.
     */
    public HokoUser(String identifier, HokoUserAccountType accountType, String name, String email, Date birthDate, HokoUserGender gender, String previousIdentifier) {
        if (identifier != null) {
            mIdentifier = identifier;
            mAccountType = accountType;
            mName = name;
            mEmail = email;
            mBirthDate = birthDate;
            mGender = gender;
            mPreviousIdentifier = previousIdentifier;
            mAnonymous = false;
        } else {
            mIdentifier = HokoUtils.generateUUID();
            mAnonymous = true;
            mAccountType = HokoUserAccountType.NONE;
            mGender = HokoUserGender.UNKNOWN;
        }
        mTimezoneOffset = getCurrentTimezoneOffset();
    }

    /**
     * Retrieves the current timezone offset from UTC in hour units.
     * E.g. UTC-01:00 = -1, UTC+05:45 = 5.75
     *
     * @return The timezone offset from UTC in hour units.
     */
    public static float getCurrentTimezoneOffset() {
        return TimeZone.getDefault().getRawOffset() / (1000.0f * 60 * 60);
    }

    /**
     * Retrieves the current user from the filesystem. This avoids identification calls where
     * the user might already be logged in. Is merely here to help avoid unnecessary steps and
     * communication overhead.
     *
     * @param context A context object.
     * @return The current HokoUser.
     */
    public static HokoUser currentUser(Context context) {
        return (HokoUser) HokoUtils.loadFromFile(HokoUserCurrentUserKey, context);
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public boolean isAnonymous() {
        return mAnonymous;
    }

    /**
     * Is a counterpart to the currentUser(...) method, saving a given HokoUser object to the
     * filesystem to be later loaded with the currentUser(...) method.
     *
     * @param context A context object.
     */
    public void save(Context context) {
        HokoUtils.saveToFile(this, HokoUserCurrentUserKey, context);
    }

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * HokoUser has been identified.
     *
     * @param context A context object.
     * @param token The Hoko API Token.
     */
    public void post(String token, Context context) {
        HokoNetworking.getNetworking().addRequest(new HokoHttpRequest(HokoHttpRequest.HokoNetworkOperationType.POST, "users", token, json(context).toString()));
    }

    /**
     * Converts all the HokoUser information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @param context A context object.
     * @return The JSONObject representation of HokoUser.
     */
    public JSONObject json(Context context) {
        try {
            JSONObject rootObject = new JSONObject();
            rootObject.put("user", baseJSON(context));
            return rootObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

    public JSONObject baseJSON(Context context) {
        try {
            JSONObject userObject = new JSONObject();
            userObject.put("timestamp", HokoDateUtils.format(new Date()));
            userObject.put("timezone_offset", mTimezoneOffset);
            userObject.put("identifier", mIdentifier);
            userObject.put("anonymous", mAnonymous);
            userObject.put("account_type", mAccountType.ordinal());
            userObject.putOpt("name", mName);
            userObject.putOpt("email", mEmail);
            userObject.putOpt("birth_date", HokoDateUtils.formatDate(mBirthDate));
            userObject.put("gender", mGender.ordinal());
            userObject.putOpt("device", HokoDevice.json(context));
            userObject.putOpt("previous_identifier", mPreviousIdentifier);
            return userObject;
        } catch (JSONException e) {
            return null;
        }
    }

}
