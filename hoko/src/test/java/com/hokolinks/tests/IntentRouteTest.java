package com.hokolinks.tests;

import com.hokolinks.BuildConfig;
import com.hokolinks.Hoko;
import com.hokolinks.model.Device;
import com.hokolinks.model.IntentRouteImpl;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(HokoGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IntentRouteTest {

    @BeforeClass
    public static void setUp() {
        Hoko.setVerbose(false);
    }

    @Test
    public void testRoute() {
        IntentRouteImpl intentRoute = new IntentRouteImpl("product/:product_id/price/:price/open", null, null, null, null);
        assertThat("product/:product_id/price/:price/open").isEqualTo(intentRoute.getRoute());
    }

    @Test
    public void testComponents() {
        IntentRouteImpl intentRoute = new IntentRouteImpl("product/:product_id/price/:price/open", null, null, null, null);

        List<String> expectedRouteComponents = new ArrayList<String>() {
            {
                add("product");
                add(":product_id");
                add("price");
                add(":price");
                add("open");
            }
        };

        assertThat(intentRoute.getComponents()).isEqualTo(expectedRouteComponents);
    }

    // For CI to work for the time being, this definitely needs fixing (is only breaking on travis and not locally)
    @Test
    public void testJSON() throws Exception {
        IntentRouteImpl intentRoute = new IntentRouteImpl("product/:product_id/price/:price/open", null, null, null, RuntimeEnvironment.application);

        JSONObject expectedJSON = new JSONObject();
        JSONObject expectedJSONRoute = new JSONObject();
        expectedJSONRoute.put("build", "1");
        expectedJSONRoute.put("device", Device.getVendor() + " " + Device.getModel());
        expectedJSONRoute.put("path", "product/:product_id/price/:price/open");
        expectedJSONRoute.put("version", "1.0");
        expectedJSON.put("route", expectedJSONRoute);

        assertThat(intentRoute.getJSON(RuntimeEnvironment.application).toString()).isEqualTo(expectedJSON.toString());
    }

}
