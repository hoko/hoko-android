package com.hokolinks.utils.networking.async;

import android.os.AsyncTask;

/**
 * An easy to use wrapper around the AsyncTask, by providing a Runnable it executes in the
 * background thread. (Used on GoogleCloudMessaging calls)
 */
public class HokoAsyncTask extends AsyncTask<Void, Void, Void> {

    private Runnable mRunnable;

    public HokoAsyncTask(Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mRunnable.run();
        return null;
    }
}
