package com.hokolinks.tests;

import com.hokolinks.BuildConfig;
import com.hokolinks.deeplinking.Handling;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.DeeplinkCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(HokoGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HandlingTest {

    /** Countdown latch */
    private CountDownLatch lock;

    private Deeplink deeplink;

    private Date timestamp;

    @Before
    public void setUp() {
        deeplink = null;
        timestamp = null;
        lock = null;
    }

    @Test
    public void testAnnonymousHandling() throws Exception{

        lock = new CountDownLatch(1);

        Handling handling = new Handling();

        handling.addHandler(new DeeplinkCallback() {
            @Override
            public void deeplinkOpened(Deeplink deeplink) {
                HandlingTest.this.deeplink = deeplink;
                lock.countDown();
            }
        });

        handling.handle(Deeplink.deeplink("product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }, null));

        lock.await();

        HashMap<String, String> expectedRouteParameters = new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        };

        HashMap<String, String> expectedQueryParameters = new HashMap<String, String>() {
            {
                put("query", "param");
            }
        };

        assertThat(deeplink.getRoute()).isEqualTo("product/:product_id");
        assertThat(deeplink.getRouteParameters()).isEqualTo(expectedRouteParameters);
        assertThat(deeplink.getQueryParameters()).isEqualTo(expectedQueryParameters);
    }

    public void testInterfaceImplementationHandling() throws Exception{

        lock = new CountDownLatch(1);

        Handling handling = new Handling();

        TestHandler handler = new TestHandler(lock);

        handling.addHandler(handler);

        handling.handle(Deeplink.deeplink("product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }, null));

        lock.await();

        HashMap<String, String> expectedRouteParameters = new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        };

        HashMap<String, String> expectedQueryParameters = new HashMap<String, String>() {
            {
                put("query", "param");
            }
        };

        assertThat(handler.deeplink.getRoute()).isEqualTo("product/:product_id");
        assertThat(handler.deeplink.getRouteParameters()).isEqualTo(expectedRouteParameters);
        assertThat(handler.deeplink.getQueryParameters()).isEqualTo(expectedQueryParameters);

    }

    public void testMultipleHandling() throws Exception{

        lock = new CountDownLatch(2);

        Handling handling = new Handling();

        TestHandler handler = new TestHandler(lock);

        handling.addHandler(handler);

        handling.addHandler(new DeeplinkCallback() {
            @Override
            public void deeplinkOpened(Deeplink deeplink) {
                HandlingTest.this.deeplink = deeplink;
                lock.countDown();
            }
        });

        handling.handle(Deeplink.deeplink("product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }, null));

        lock.await();

        HashMap<String, String> expectedRouteParameters = new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        };

        HashMap<String, String> expectedQueryParameters = new HashMap<String, String>() {
            {
                put("query", "param");
            }
        };

        assertThat(handler.deeplink.getRoute()).isEqualTo("product/:product_id");
        assertThat(handler.deeplink.getRouteParameters()).isEqualTo(expectedRouteParameters);
        assertThat(handler.deeplink.getQueryParameters()).isEqualTo(expectedQueryParameters);

        assertThat(deeplink.getRoute()).isEqualTo("product/:product_id");
        assertThat(deeplink.getRouteParameters()).isEqualTo(expectedRouteParameters);
        assertThat(deeplink.getQueryParameters()).isEqualTo(expectedQueryParameters);
    }

    public void testHandlingOrder() throws Exception{

        lock = new CountDownLatch(2);

        Handling handling = new Handling();

        TestHandler handler = new TestHandler(lock);

        handling.addHandler(handler);

        handling.addHandler(new DeeplinkCallback() {
            @Override
            public void deeplinkOpened(Deeplink deeplink) {
                HandlingTest.this.deeplink = deeplink;
                HandlingTest.this.timestamp = new Date();
                lock.countDown();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        handling.handle(Deeplink.deeplink("product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }, null));

        lock.await();

        assertThat(timestamp).isAfter(handler.timestamp);

    }

    private class TestHandler implements DeeplinkCallback {

        public Date timestamp;
        public Deeplink deeplink;

        private CountDownLatch lock;

        public TestHandler(CountDownLatch lock) {
            this.lock = lock;
        }

        @Override
        public void deeplinkOpened(Deeplink deeplink) {
            this.deeplink = deeplink;
            this.timestamp = new Date();
            this.lock.countDown();
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
