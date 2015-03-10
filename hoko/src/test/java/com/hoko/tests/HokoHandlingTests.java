package com.hoko.tests;

import android.test.InstrumentationTestCase;

import com.hoko.deeplinking.HokoHandling;
import com.hoko.deeplinking.listeners.HokoHandler;
import com.hoko.model.HokoDeeplink;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ivanbruel on 10/03/15.
 */
public class HokoHandlingTests extends InstrumentationTestCase {

    /** Countdown latch */
    private CountDownLatch lock;

    private HokoDeeplink deeplink;

    private Date timestamp;

    @Override
    protected void setUp() throws Exception {
        deeplink = null;
        timestamp = null;
        lock = null;
    }

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

        assertEquals("product/:product_id", deeplink.getRoute());
        assertEquals(expectedRouteParameters, deeplink.getRouteParameters());
        assertEquals(expectedQueryParameters, deeplink.getQueryParameters());

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

        assertEquals("product/:product_id", handler.deeplink.getRoute());
        assertEquals(expectedRouteParameters, handler.deeplink.getRouteParameters());
        assertEquals(expectedQueryParameters, handler.deeplink.getQueryParameters());
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

        assertEquals("product/:product_id", handler.deeplink.getRoute());
        assertEquals(expectedRouteParameters, handler.deeplink.getRouteParameters());
        assertEquals(expectedQueryParameters, handler.deeplink.getQueryParameters());

        assertEquals("product/:product_id", deeplink.getRoute());
        assertEquals(expectedRouteParameters, deeplink.getRouteParameters());
        assertEquals(expectedQueryParameters, deeplink.getQueryParameters());
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

        assertEquals(handler.timestamp.compareTo(timestamp) < 0, true);

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
