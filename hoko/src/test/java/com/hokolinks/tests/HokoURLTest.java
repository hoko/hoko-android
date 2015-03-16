package com.hokolinks.tests;


import com.hokolinks.model.HokoRoute;
import com.hokolinks.model.HokoURL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class HokoURLTest {

    @Test
    public void testSanitize() {
        String sanitizedURLString = HokoURL.sanitizeURL("hoko://///hoko/needs/testing////is/sanitization/ok///");
        assertThat(sanitizedURLString).isEqualTo("hoko://hoko/needs/testing/is/sanitization/ok");
    }

    @Test
    public void testNoNeedForSanitization() {
        String sanitizedURLString = HokoURL.sanitizeURL("hoko://hoko/needs/testing/is/sanitization/ok");
        assertThat(sanitizedURLString).isEqualTo("hoko://hoko/needs/testing/is/sanitization/ok");
    }

    @Test
    public void testQuery() {
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

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
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");
        assertThat(url.getScheme()).isEqualTo("hoko");
    }

    public void testRouteMatched() {
        HokoRoute route = new HokoRoute("param/:param/other_param/:other_param", null, null, null, null);
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

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
        HokoRoute route = new HokoRoute("param/:param/other_param/:other_param/something", null, null, null, null);
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertThat(routeParameters).isNullOrEmpty();
    }

    public void testRouteNotMatchedExtraParameter() {
        HokoRoute route = new HokoRoute("param/:param/other_param/:other_param", null, null, null, null);
        HokoURL url = new HokoURL("hoko://param/1/other_param/2/50?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertThat(routeParameters).isNullOrEmpty();
    }

}