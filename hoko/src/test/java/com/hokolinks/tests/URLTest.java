package com.hokolinks.tests;


import com.hokolinks.BuildConfig;
import com.hokolinks.model.Route;
import com.hokolinks.model.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(constants = BuildConfig.class, emulateSdk = 21)
@RunWith(RobolectricGradleTestRunner.class)
public class URLTest {

    @Test
    public void testSanitize() {
        String sanitizedURLString = URL.sanitizeURL("hoko://///hoko/needs/testing////is/sanitization/ok///");
        assertThat(sanitizedURLString).isEqualTo("hoko://hoko/needs/testing/is/sanitization/ok");
    }

    @Test
    public void testNoNeedForSanitization() {
        String sanitizedURLString = URL.sanitizeURL("hoko://hoko/needs/testing/is/sanitization/ok");
        assertThat(sanitizedURLString).isEqualTo("hoko://hoko/needs/testing/is/sanitization/ok");
    }

    @Test
    public void testQuery() {
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> expectedQueryParameters = new HashMap<String, String>() {
            {
                put("test", "1");
                put("q_param", "2");
                put("string", "hi there");
            }
        };

        assertThat(url.getQueryParameters()).isEqualTo(expectedQueryParameters);
    }

    public void testScheme() {
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");
        assertThat(url.getScheme()).isEqualTo("hoko");
    }

    public void testRouteMatched() {
        Route route = new Route("param/:param/other_param/:other_param", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        HashMap<String, String> expectedRouteParameters = new HashMap<String, String>() {
            {
                put("param", "1");
                put("other_param", "2");
            }
        };

        assertThat(routeParameters).isEqualTo(expectedRouteParameters);
    }

    public void testRouteNotMatched() {
        Route route = new Route("param/:param/other_param/:other_param/something", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertThat(routeParameters).isNullOrEmpty();
    }

    public void testRouteNotMatchedExtraParameter() {
        Route route = new Route("param/:param/other_param/:other_param", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2/50?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertThat(routeParameters).isNullOrEmpty();
    }

}