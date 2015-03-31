package com.hokolinks.model;

import com.hokolinks.utils.DateUtils;
import com.hokolinks.utils.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Event contains the necessary information to be able to track key events in a given
 * deeplinking session (Session). It contains a name, an amount and a created at date value
 * to pin point when such an even happened.
 */
public class Event {

    private String mName;
    private double mAmount;
    private Date mCreatedAt;

    /**
     * Event is created by providing a name and an amount. The created at value is gathered from
     * the current date on the device, including the timezone.
     *
     * @param name   The event name.
     * @param amount The amount (€, $, etc).
     */
    public Event(String name, double amount) {
        mName = name;
        mAmount = amount;
        mCreatedAt = new Date();
    }

    /**
     * Converts all the Event information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @return The JSONObject representation of Event.
     */
    public JSONObject json() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("name", mName);
            jsonObject.putOpt("amount", String.valueOf(mAmount));
            jsonObject.putOpt("created_at", DateUtils.format(mCreatedAt));
            return jsonObject;
        } catch (JSONException e) {
            Log.e(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<Event> name='" + mName + "' amount='" + mAmount + "' createdAt='"
                + mCreatedAt + "'";
    }

}
