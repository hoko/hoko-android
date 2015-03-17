package com.hokolinks.deeplinking.listeners;

/**
 * HokoLinkGenerationListener is a callback for the generate Smartlink calls, it will call the
 * onLinkGenerated in case of success or onError in case of failure.
 */
public interface HokoLinkGenerationListener {
    void onLinkGenerated(String smartlink);

    void onError(Exception e);
}
