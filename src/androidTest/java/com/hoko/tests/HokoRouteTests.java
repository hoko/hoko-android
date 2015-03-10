package com.hoko.tests;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.hoko.model.HokoDevice;
import com.hoko.model.HokoRoute;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanbruel on 10/03/15.
 */
public class HokoRouteTests  extends ApplicationTestCase {

    public HokoRouteTests() {
        super(Application.class);
    }
    public void testRoute() {
        HokoRoute route = new HokoRoute("product/:product_id/price/:price/open", null, null, null, null);
        assertEquals("product/:product_id/price/:price/open", route.getRoute());
    }

    public void testComponents() {
        HokoRoute route = new HokoRoute("product/:product_id/price/:price/open", null, null, null, null);

        List<String> expectedRouteComponents = new ArrayList<String>() {
            {
                add("product");
                add(":product_id");
                add("price");
                add(":price");
                add("open");
            }
        };

        assertEquals(expectedRouteComponents, route.getComponents());
    }

    public void testJSON() throws Exception {
        HokoRoute route = new HokoRoute("product/:product_id/price/:price/open", null, null, null, getContext());

        JSONObject expectedJSON = new JSONObject();
        JSONObject expectedJSONRoute = new JSONObject();
        expectedJSONRoute.put("build", "0");
        expectedJSONRoute.put("device", HokoDevice.getVendor() + " " + HokoDevice.getModel());
        expectedJSONRoute.put("path", "product/:product_id/price/:price/open");
        expectedJSON.put("route", expectedJSONRoute);

        assertEquals(expectedJSON.toString(), route.getJSON().toString());
    }

}
