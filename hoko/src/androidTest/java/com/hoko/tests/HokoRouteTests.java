package com.hoko.tests;

import com.hoko.model.HokoDevice;
import com.hoko.model.HokoRoute;

import org.json.JSONObject;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by ivanbruel on 10/03/15.
 */
public class HokoRouteTests {

    @Test
    public void testRoute() {
        HokoRoute route = new HokoRoute("product/:product_id/price/:price/open", null, null, null, null);
        assertThat("product/:product_id/price/:price/open").isEqualTo(route.getRoute());
    }

    @Test
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

        assertThat(route.getComponents()).isEqualTo(expectedRouteComponents);
    }

    @Test
    public void testJSON() throws Exception {
        HokoRoute route = new HokoRoute("product/:product_id/price/:price/open", null, null, null, Robolectric.application);

        JSONObject expectedJSON = new JSONObject();
        JSONObject expectedJSONRoute = new JSONObject();
        expectedJSONRoute.put("device", HokoDevice.getVendor() + " " + HokoDevice.getModel());
        expectedJSONRoute.put("path", "product/:product_id/price/:price/open");
        expectedJSON.put("route", expectedJSONRoute);

        assertThat(route.getJSON().toString()).isEqualTo(expectedJSON.toString());
    }

}
