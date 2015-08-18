package com.hokolinks.tests;

import com.hokolinks.BuildConfig;
import com.hokolinks.deeplinking.Filtering;
import com.hokolinks.deeplinking.Handling;
import com.hokolinks.deeplinking.Routing;
import com.hokolinks.model.Route;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(HokoGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoutingTest {

    @Test
    public void testRoutingSort() {
        Routing routing = new Routing("token", RuntimeEnvironment.application, new Handling(), new Filtering());
        routing.mapRoute("product/:product_id", null);
        routing.mapRoute("product/xpto/:id", null);
        routing.mapRoute("product/xpto/zzz", null);
        routing.mapRoute("product/xpto", null);
        routing.mapRoute("mkay", null);
        routing.mapRoute("anything", null);
        routing.mapRoute("zoidberg", null);
        ArrayList<Route> routes = routing.getRoutes();
        assertThat(routes.get(0).getRoute()).isEqualTo("anything");
        assertThat(routes.get(1).getRoute()).isEqualTo("mkay");
        assertThat(routes.get(2).getRoute()).isEqualTo("zoidberg");
        assertThat(routes.get(3).getRoute()).isEqualTo("product/xpto");
        assertThat(routes.get(4).getRoute()).isEqualTo("product/:product_id");
        assertThat(routes.get(5).getRoute()).isEqualTo("product/xpto/zzz");
        assertThat(routes.get(6).getRoute()).isEqualTo("product/xpto/:id");
    }

}
