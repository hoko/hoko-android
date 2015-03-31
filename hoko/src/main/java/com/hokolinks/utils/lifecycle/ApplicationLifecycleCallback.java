package com.hokolinks.utils.lifecycle;

/**
 * Callback to be used with ApplicationLifecycle.
 * Has 2 methods, onResume for when the application is resumed and oNPause when the application is
 * paused.
 */
public interface ApplicationLifecycleCallback {

    /**
     * Will be called when the application is resumed.
     */
    void onResume();

    /**
     * Will be called when the application is paused.
     */
    void onPause();

}
