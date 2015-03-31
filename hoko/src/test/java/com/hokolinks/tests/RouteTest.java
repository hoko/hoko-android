package com.hokolinks.tests;

import com.hokolinks.Hoko;
import com.hokolinks.model.Device;
import com.hokolinks.model.Route;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by ivanbruel on 10/03/15.
 */
@Config(manifest=Config.NONE)
public class RouteTest {

    @BeforeClass
    public static void setUp() {
        Hoko.setVerbose(false);
    }

    @Test
    public void testRoute() {
        Route route = new Route("product/:product_id/price/:price/open", null, null, null, null);
        assertThat("product/:product_id/price/:price/open").isEqualTo(route.getRoute());
    }

    @Test
    public void testComponents() {
        Route route = new Route("product/:product_id/price/:price/open", null, null, null, null);

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
        Route route = new Route("product/:product_id/price/:price/open", null, null, null, Robolectric.application);

        JSONObject expectedJSON = new JSONObject();
        JSONObject expectedJSONRoute = new JSONObject();
        expectedJSONRoute.put("device", Device.getVendor() + " " + Device.getModel());
        expectedJSONRoute.put("path", "product/:product_id/price/:price/open");
        expectedJSON.put("route", expectedJSONRoute);

        assertThat(route.getJSON().toString()).isEqualTo(expectedJSON.toString());
    }

}
