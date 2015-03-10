package com.hoko.utils.lifecycle;

/**
 * Callback to be used with HokoApplicationLifecycle.
 * Has 2 methods, onResume for when the application is resumed and oNPause when the application is
 * paused.
 */
public interface HokoApplicationLifecycleCallback {

    /**
     * Will be called when the application is resumed.
     */
    public void onResume();

    /**
     * Will be called when the application is paused.
     */
    public void onPause();

}
