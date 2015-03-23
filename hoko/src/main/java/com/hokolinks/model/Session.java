package com.hokolinks.model;

import android.content.Context;

import com.hokolinks.utils.DateUtils;
import com.hokolinks.utils.log.Log;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private User mUser;
    private List<Event> mKeyEvents;

    /**
     * A Session is created by providing a User and a Deeplink object. It will then
     * set the startedAt date as the current date and create an empty list of HokoEvents to
     * track in the future.
     *
     * @param user     A User instance.
     * @param deeplink A Deeplink instance.
     */
    public Session(User user, Deeplink deeplink) {
        mUser = user;
        mDeeplink = deeplink;
        mStartedAt = new Date();
        mKeyEvents = new ArrayList<Event>();
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    /**
     * Tracks a Key Event on a particular Session.
     *
     * @param event A Event instance.
     */
    public void trackKeyEvent(Event event) {
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
            sessionJsonObject.putOpt("user", mUser.json(context).getJSONObject("user"));
            sessionJsonObject.putOpt("key_events", eventsJSON());
            sessionJsonObject.putOpt(Deeplink.HokoDeeplinkOpenLinkIdentifierKey,
                    mDeeplink.getOpenIdentifier());
            sessionJsonObject.putOpt(Deeplink.HokoDeeplinkSmartlinkIdentifierKey,
                    mDeeplink.getSmartlinkIdentifier());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("session", sessionJsonObject);
            return jsonObject;
        } catch (JSONException e) {
            Log.e(e);
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
        for (Event event : mKeyEvents) {
            jsonArray.put(event.json());
        }
        return jsonArray;
    }


}
