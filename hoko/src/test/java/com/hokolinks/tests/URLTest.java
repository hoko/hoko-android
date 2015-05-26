package com.hokolinks.tests;


import com.hokolinks.BuildConfig;
import com.hokolinks.model.IntentRouteImpl;
import com.hokolinks.model.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(HokoGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
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
        IntentRouteImpl intentRoute = new IntentRouteImpl("param/:param/other_param/:other_param", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters = url.matchesWithRoute(intentRoute);

        HashMap<String, String> expectedRouteParameters = new HashMap<String, String>() {
            {
                put("param", "1");
                put("other_param", "2");
            }
        };

        assertThat(routeParameters).isEqualTo(expectedRouteParameters);
    }

    public void testRouteNotMatched() {
        IntentRouteImpl intentRoute = new IntentRouteImpl("param/:param/other_param/:other_param/something", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters = url.matchesWithRoute(intentRoute);

        assertThat(routeParameters).isNullOrEmpty();
    }

    public void testRouteNotMatchedExtraParameter() {
        IntentRouteImpl intentRoute = new IntentRouteImpl("param/:param/other_param/:other_param", null, null, null, null);
        URL url = new URL("hoko://param/1/other_param/2/50?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters = url.matchesWithRoute(intentRoute);

        assertThat(routeParameters).isNullOrEmpty();
    }

}