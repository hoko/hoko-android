package com.hokolinks.utils.networking;

import android.content.Context;

import com.hokolinks.model.Device;
import com.hokolinks.utils.Utils;
import com.hokolinks.utils.lifecycle.ApplicationLifecycle;
import com.hokolinks.utils.lifecycle.ApplicationLifecycleCallback;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Networking class is a wrapper around HokoHttpRequests in order to process them in a
 * serial queue, where requests can be retried in case of failure. The flushes are triggered by
 * a timer to avoid clogging the device's network. This class will also handle the persistence of
 * HokoHttpRequests in order to avoid losing any data on application closes/crashes and network
 * problems. It will also only try to flush in case the device recognizes it has internet
 * connectivity.
 */
public class Networking {

    // Filename to save the http tasks to storage
    private static final String HTTP_TASKS_FILENAME = "http_tasks";

    // Configuration of the Networking
    private static final int FLUSH_TIMER_INTERVAL = 30000; // in millis
    private static final int HTTP_TASKS_NUMBER_OF_RETRIES = 3;

    // Static class to avoid duplication of Networking instances
    private static Networking mInstance;


    private Context mContext;
    final private List<HttpRequest> mHttpTasks;
    private Timer mTimer;

    /**
     * Private constructor, will try to load the tasks from file to resume them as soon as possible.
     *
     * @param context A context object.
     */
    @SuppressWarnings("unchecked")
    private Networking(Context context) {
        mContext = context;
        List<HttpRequest> httpTasks = null;
        try {
            httpTasks = (List<HttpRequest>)
                    Utils.loadFromFile(HTTP_TASKS_FILENAME, context);
        } catch (ClassCastException e) {
            httpTasks = new ArrayList<HttpRequest>();
        }
        if (httpTasks == null) {
            httpTasks = new ArrayList<HttpRequest>();
        }
        mHttpTasks = httpTasks;
        flush();
        registerActivityLifecycleCallbacks();
    }

    /**
     * This function will setup the Networking static instance, resuming pending http requests
     * and starting normal functionality.
     *
     * @param context A context object.
     */
    public static void setupNetworking(Context context) {
        if (mInstance == null) {
            mInstance = new Networking(context);
        }
    }

    /**
     * Returns the static Networking instance.
     *
     * @return The static Networking instance.
     */
    public static Networking getNetworking() {
        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }

    //Tasks

    /**
     * Executes the pending http request tasks in case the device has internet connectivity,
     * otherwise it will just reset the timer.
     */
    private void flush() {
        if (mHttpTasks.size() > 0 && Device.hasInternetConnectivity(mContext)) {
            stopFlushTimer();
            executeTasks();
        } else {
            startFlushTimer();
        }
    }

    /**
     * Adds an http request to the queue, will only add it in case it has not surpassed the maximum
     * number of retries.
     *
     * @param httpRequest A HttpRequest object.
     */
    public void addRequest(HttpRequest httpRequest) {
        if (httpRequest.getNumberOfRetries() < HTTP_TASKS_NUMBER_OF_RETRIES) {
            HokoLog.d("Adding request to queue");
            synchronized (mHttpTasks) {
                mHttpTasks.add(httpRequest);
            }
        }
        saveTasks();
    }

    private void removeRequest(HttpRequest httpRequest) {
        synchronized (mHttpTasks) {
            mHttpTasks.remove(httpRequest);
        }
    }

    /**
     * Executes the HttpRequests in a serial queue. Will only start the next request in case the
     * previous one has finished. This will handle incrementing the number of retries in case of
     * failure and re-adding them to the http request queue.
     */
    private void executeTasks() {
        ExecutorService service = Executors.newFixedThreadPool(1);
        synchronized (mHttpTasks) {
            for (final HttpRequest httpRequest : mHttpTasks) {
                service.execute(httpRequest.toRunnable(new HttpRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        HokoLog.d("Success " + jsonObject.toString());
                        removeRequest(httpRequest);
                        saveTasks();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        httpRequest.incrementNumberOfRetries();
                        removeRequest(httpRequest);
                        addRequest(httpRequest);
                    }
                }));
            }
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                startFlushTimer();
            }
        });
    }

    /**
     * Saves all the current http requests to file, guaranteeing persistence.
     */
    private void saveTasks() {
        Utils.saveToFile(mHttpTasks, HTTP_TASKS_FILENAME, mContext);
    }

    //Timer

    /**
     * Starts the flush timer in case it is stopped, will do nothing otherwise.
     */
    private synchronized void startFlushTimer() {
        if (mTimer != null) {
            return;
        }
        mTimer = new Timer();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopFlushTimer();
                flush();
            }
        }, FLUSH_TIMER_INTERVAL);
    }

    /**
     * Stops the flush timer.
     */
    private synchronized void stopFlushTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    // Application Lifecycle

    /**
     * Registers activity lifecycle callbacks to know when the application is in background and
     * foreground, stopping the timer or flushing.
     */
    private void registerActivityLifecycleCallbacks() {
        ApplicationLifecycle.registerApplicationLifecycleCallback(mContext,
                new ApplicationLifecycleCallback() {
                    @Override
                    public void onResume() {
                        flush();
                    }

                    @Override
                    public void onPause() {
                        stopFlushTimer();
                    }
                });
    }


}
