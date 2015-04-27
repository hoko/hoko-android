package com.hokolinks.tests;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.LinkGenerationListener;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.exceptions.NullDeeplinkException;
import com.hokolinks.model.exceptions.RouteNotMappedException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by ivanbruel on 16/03/15.
 */
@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class LinkGeneratorTest {

    /** Countdown latch */
    private CountDownLatch lock;
    private String smartlink;
    private Exception exception;

    @BeforeClass
    public static void setUp() throws IOException {
        Hoko.setVerbose(false);

        Hoko.setup(Robolectric.application, "API");
        Hoko.deeplinking().routing().mapRoute("store/:language_code/product/:product_id", null, new HashMap<String, Field>() {
            {
                put("language_code", null);
                put("product_id", null);
            }
        }, null);
    }

    @Before
    public void setUpLock() {
        lock = null;
    }

    @After
    public void tearDown() {
        smartlink = null;
        exception = null;
        lock = null;
    }

    @Test
    public void testBasicHokolink() throws Exception {
        lock = new CountDownLatch(1);
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        response.setEntity(new StringEntity("{smartlink: 'http://hoko.link/PRMLNK'}"));
        Robolectric.addPendingHttpResponse(response);
        HashMap<String, String> routeParameters = new HashMap<String, String>() {
            {
                put("language_code", "en-US");
                put("product_id", "1234");
            }
        };

        HashMap<String, String> queryParameters = new HashMap<String, String>() {
            {
                put("utm_source", "test_case");
                put("timestamp", "12341234");
            }
        };

        Hoko.deeplinking().generateSmartlink(Deeplink.deeplink("store/:language_code/product/:product_id", routeParameters, queryParameters), new LinkGenerationListener() {
            @Override
            public void onLinkGenerated(String smartlink) {
                LinkGeneratorTest.this.smartlink = smartlink;
                lock.countDown();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        lock.await();
        assertThat(this.smartlink).isEqualTo("http://hoko.link/PRMLNK");
    }

    @Test
    public void testMissingRouteParameterHokolink() throws Exception {
        lock = new CountDownLatch(1);
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        response.setEntity(new StringEntity("{smartlink: 'http://hoko.link/PRMLNK'}"));
        Robolectric.addPendingHttpResponse(response);
        HashMap<String, String> routeParameters = new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        };

        HashMap<String, String> queryParameters = new HashMap<String, String>() {
            {
                put("utm_source", "test_case");
                put("timestamp", "12341234");
            }
        };

        Hoko.deeplinking().generateSmartlink(Deeplink.deeplink("store/:language_code/product/:product_id", routeParameters, queryParameters), new LinkGenerationListener() {
            @Override
            public void onLinkGenerated(String smartlink) {
                LinkGeneratorTest.this.smartlink = smartlink;
                lock.countDown();
            }

            @Override
            public void onError(Exception e) {
                LinkGeneratorTest.this.exception = e;
                lock.countDown();
            }
        });
        lock.await();
        assertThat(this.exception).isInstanceOf(NullDeeplinkException.class);
        assertThat(this.smartlink).isNull();
    }

    @Test
    public void testUnknownRouteHokolink() throws Exception {
        lock = new CountDownLatch(1);
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        response.setEntity(new StringEntity("{smartlink: 'http://hoko.link/PRMLNK'}"));
        Robolectric.addPendingHttpResponse(response);
        HashMap<String, String> routeParameters = new HashMap<String, String>() {
            {
                put("language_code", "en-US");
                put("collection_id", "1234");
            }
        };

        HashMap<String, String> queryParameters = new HashMap<String, String>() {
            {
                put("utm_source", "test_case");
                put("timestamp", "12341234");
            }
        };

        Hoko.deeplinking().generateSmartlink(Deeplink.deeplink("store/:language_code/collection/:collection_id", routeParameters, queryParameters), new LinkGenerationListener() {
            @Override
            public void onLinkGenerated(String smartlink) {
                LinkGeneratorTest.this.smartlink = smartlink;
                lock.countDown();
            }

            @Override
            public void onError(Exception e) {
                LinkGeneratorTest.this.exception = e;
                lock.countDown();
            }
        });
        lock.await();
        assertThat(this.exception).isInstanceOf(RouteNotMappedException.class);
        assertThat(this.smartlink).isNull();
    }

}
