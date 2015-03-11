package com.hoko.tests;

import com.hoko.deeplinking.HokoHandling;
import com.hoko.deeplinking.listeners.HokoHandler;
import com.hoko.model.HokoDeeplink;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by ivanbruel on 10/03/15.
 */
public class HokoHandlingTests {

    /** Countdown latch */
    private CountDownLatch lock;

    private HokoDeeplink deeplink;

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

        HokoHandling handling = new HokoHandling();

        handling.addHandler(new HokoHandler() {
            @Override
            public void handle(HokoDeeplink deeplink) {
                HokoHandlingTests.this.deeplink = deeplink;
                lock.countDown();
            }
        });

        handling.handle(new HokoDeeplink("hoko", "product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }));

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

        HokoHandling handling = new HokoHandling();

        HokoTestHandler handler = new HokoTestHandler(lock);

        handling.addHandler(handler);

        handling.handle(new HokoDeeplink("hoko", "product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }));

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

        HokoHandling handling = new HokoHandling();

        HokoTestHandler handler = new HokoTestHandler(lock);

        handling.addHandler(handler);

        handling.addHandler(new HokoHandler() {
            @Override
            public void handle(HokoDeeplink deeplink) {
                HokoHandlingTests.this.deeplink = deeplink;
                lock.countDown();
            }
        });

        handling.handle(new HokoDeeplink("hoko", "product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }));

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

        HokoHandling handling = new HokoHandling();

        HokoTestHandler handler = new HokoTestHandler(lock);

        handling.addHandler(handler);

        handling.addHandler(new HokoHandler() {
            @Override
            public void handle(HokoDeeplink deeplink) {
                HokoHandlingTests.this.deeplink = deeplink;
                HokoHandlingTests.this.timestamp = new Date();
                lock.countDown();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {

                }
            }
        });

        handling.handle(new HokoDeeplink("hoko", "product/:product_id", new HashMap<String, String>() {
            {
                put("product_id", "1234");
            }
        }, new HashMap<String, String>() {
            {
                put("query", "param");
            }
        }));

        lock.await();

        assertThat(timestamp).isAfter(handler.timestamp);

    }

    private class HokoTestHandler implements HokoHandler {

        public Date timestamp;
        public HokoDeeplink deeplink;

        private CountDownLatch lock;

        public HokoTestHandler(CountDownLatch lock) {
            this.lock = lock;
        }

        @Override
        public void handle(HokoDeeplink deeplink) {
            this.deeplink = deeplink;
            this.timestamp = new Date();
            this.lock.countDown();
            try {
                Thread.sleep(1);
            } catch (Exception e) {

            }
        }
    }

}
