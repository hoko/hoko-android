package com.hokolinks.deeplinking.listeners;

/**
 * LinkGenerationListener is a callback for the generate Smartlink calls, it will call the
 * onLinkGenerated in case of success or onError in case of failure.
 */
public interface LinkGenerationListener {
    void onLinkGenerated(String smartlink);

    void onError(Exception e);
}
