package com.hokolinks.model;

import android.content.Context;

import com.hokolinks.utils.DateUtils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * The Session class serves the purpose of starting and tracking a deeplinking session.
 * Sessions are only created upon an inbound deeplink, after creation the developer can track
 * some key events to materialize the engagement and conversion of users. These events will be
 * ignored if there was no inbound deeplink.
 */
public class Session {

    private Deeplink mDeeplink;
    private Date mStartedAt;
    private Date mEndedAt;

    /**
     * A Session is created by providing a User and a Deeplink object. It will then
     * set the startedAt date as the current date and create an empty list of HokoEvents to
     * track in the future.
     *
     * @param deeplink A Deeplink instance.
     */
    public Session(Deeplink deeplink) {
        mDeeplink = deeplink;
        mStartedAt = new Date();
    }

    /**
     * Ends the current session, setting the end of session date as the current date.
     */
    public void end() {
        mEndedAt = new Date();
    }

    // Duration in Seconds
    private long getDuration() {
        return Math.round((mEndedAt.getTime() - mStartedAt.getTime()) / 1000.0);
    }

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * Session has ended.
     *
     * @param context A context object.
     * @param token   The Hoko API Token.
     */
    public void post(String token, Context context) {
        Networking.getNetworking().addRequest(
                new HttpRequest(HttpRequest.HokoNetworkOperationType.POST, "sessions",
                        token, json(context).toString()));
    }

    /**
     * Converts all the Session information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @param context A context object.
     * @return The JSONObject representation of Session.
     */
    public JSONObject json(Context context) {
        try {
            JSONObject sessionJsonObject = new JSONObject();
            sessionJsonObject.putOpt("started_at", DateUtils.format(mStartedAt));
            sessionJsonObject.putOpt("duration", getDuration());
            sessionJsonObject.putOpt("device", Device.json(context));
            sessionJsonObject.putOpt(Deeplink.HokoDeeplinkOpenLinkIdentifierKey,
                    mDeeplink.getOpenIdentifier());
            sessionJsonObject.putOpt(Deeplink.HokoDeeplinkSmartlinkIdentifierKey,
                    mDeeplink.getSmartlinkIdentifier());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("session", sessionJsonObject);
            return jsonObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

}
