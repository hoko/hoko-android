package com.hoko.tests;

import android.test.InstrumentationTestCase;

import com.hoko.model.HokoRoute;
import com.hoko.model.HokoURL;

import java.util.HashMap;

public class HokoURLTests extends InstrumentationTestCase{

    public void testSanitize() {
        String sanitizedURLString = HokoURL.sanitizeURL("hoko://///hoko/needs/testing////is/sanitization/ok///");
        assertEquals("hoko://hoko/needs/testing/is/sanitization/ok", sanitizedURLString);
    }

    public void testNoNeedForSanitization() {
        String sanitizedURLString = HokoURL.sanitizeURL("hoko://hoko/needs/testing/is/sanitization/ok");
        assertEquals("hoko://hoko/needs/testing/is/sanitization/ok", sanitizedURLString);
    }

    public void testQuery() {
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> expectedQueryParameters = new HashMap<String, String>() {
            {
                put("test", "1");
                put("q_param", "2");
                put("string", "hi there");
            }
        };

        assertEquals(expectedQueryParameters, url.getQueryParameters());
    }

    public void testScheme() {
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");
        assertEquals("hoko", url.getScheme());
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

        assertEquals(expectedRouteParameters, routeParameters);
    }

    public void testRouteNotMatched() {
        HokoRoute route = new HokoRoute("param/:param/other_param/:other_param/something", null, null, null, null);
        HokoURL url = new HokoURL("hoko://param/1/other_param/2?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertEquals(null, routeParameters);
    }

    public void testRouteNotMatchedExtraParameter() {
        HokoRoute route = new HokoRoute("param/:param/other_param/:other_param", null, null, null, null);
        HokoURL url = new HokoURL("hoko://param/1/other_param/2/50?test=1&q_param=2&string=hi+there");

        HashMap<String, String> routeParameters =  url.matchesWithRoute(route);

        assertEquals(null, routeParameters);
    }

}