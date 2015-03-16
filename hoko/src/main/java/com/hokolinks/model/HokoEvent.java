package com.hokolinks.model;

import com.hokolinks.utils.HokoDateUtils;
import com.hokolinks.utils.log.HokoLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * HokoEvent contains the necessary information to be able to track key events in a given
 * deeplinking session (HokoSession). It contains a name, an amount and a created at date value
 * to pin point when such an even happened.
 */
public class HokoEvent {

    private String mName;
    private double mAmount;
    private Date mCreatedAt;

    /**
     * HokoEvent is created by providing a name and an amount. The created at value is gathered from
     * the current date on the device, including the timezone.
     *
     * @param name   The event name.
     * @param amount The amount (€, $, etc).
     */
    public HokoEvent(String name, double amount) {
        mName = name;
        mAmount = amount;
        mCreatedAt = new Date();
    }

    /**
     * Converts all the HokoEvent information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @return The JSONObject representation of HokoEvent.
     */
    public JSONObject json() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("name", mName);
            jsonObject.putOpt("amount", String.valueOf(mAmount));
            jsonObject.putOpt("created_at", HokoDateUtils.format(mCreatedAt));
            return jsonObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<HokoEvent> name='" + mName + "' amount='" + mAmount + "' createdAt='"
                + mCreatedAt + "'";
    }

}
