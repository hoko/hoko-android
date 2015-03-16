package com.hokolinks.model;

import android.content.Context;

import com.hokolinks.utils.HokoDateUtils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.HokoNetworking;
import com.hokolinks.utils.networking.async.HokoHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The HokoSession class serves the purpose of starting and tracking a deeplinking session.
 * Sessions are only created upon an inbound deeplink, after creation the developer can track
 * some key events to materialize the engagement and conversion of users. These events will be
 * ignored if there was no inbound deeplink.
 */
public class HokoSession {

    private HokoDeeplink mDeeplink;
    private Date mStartedAt;
    private Date mEndedAt;
    private HokoUser mUser;
    private List<HokoEvent> mKeyEvents;

    /**
     * A HokoSession is created by providing a HokoUser and a HokoDeeplink object. It will then
     * set the startedAt date as the current date and create an empty list of HokoEvents to
     * track in the future.
     *
     * @param user     A HokoUser instance.
     * @param deeplink A HokoDeeplink instance.
     */
    public HokoSession(HokoUser user, HokoDeeplink deeplink) {
        mUser = user;
        mDeeplink = deeplink;
        mStartedAt = new Date();
        mKeyEvents = new ArrayList<HokoEvent>();
    }

    public HokoUser getUser() {
        return mUser;
    }

    public void setUser(HokoUser user) {
        this.mUser = user;
    }

    /**
     * Tracks a Key Event on a particular HokoSession.
     *
     * @param event A HokoEvent instance.
     */
    public void trackKeyEvent(HokoEvent event) {
        mKeyEvents.add(event);
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
     * HokoSession has ended.
     *
     * @param context A context object.
     * @param token   The Hoko API Token.
     */
    public void post(String token, Context context) {
        HokoNetworking.getNetworking().addRequest(
                new HokoHttpRequest(HokoHttpRequest.HokoNetworkOperationType.POST, "sessions",
                        token, json(context).toString()));
    }

    /**
     * Converts all the HokoSession information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @param context A context object.
     * @return The JSONObject representation of HokoSession.
     */
    public JSONObject json(Context context) {
        try {
            JSONObject sessionJsonObject = new JSONObject();
            sessionJsonObject.putOpt("started_at", HokoDateUtils.format(mStartedAt));
            sessionJsonObject.putOpt("duration", getDuration());
            sessionJsonObject.putOpt("user", mUser.json(context).getJSONObject("user"));
            sessionJsonObject.putOpt("key_events", eventsJSON());
            sessionJsonObject.putOpt(HokoDeeplink.HokoDeeplinkLinkIdentifierKey,
                    mDeeplink.getLinkIdentifier());
            sessionJsonObject.putOpt(HokoDeeplink.HokoDeeplinkHokolinkIdentifierKey,
                    mDeeplink.getHokolinkIdentifier());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("session", sessionJsonObject);
            return jsonObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

    /**
     * Creates a JSONArray out of the JSON representation of a list of HokoEvents.
     *
     * @return A JSONArray containing HokoEvents in JSON format.
     */
    private JSONArray eventsJSON() {
        JSONArray jsonArray = new JSONArray();
        for (HokoEvent event : mKeyEvents) {
            jsonArray.put(event.json());
        }
        return jsonArray;
    }


}
