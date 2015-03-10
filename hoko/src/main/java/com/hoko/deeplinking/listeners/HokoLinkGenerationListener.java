package com.hoko.deeplinking.listeners;

/**
 * HokoLinkGenerationListener is a callback for the generate Hokolink calls, it will call the
 * onLinkGenerated in case of success or onError in case of failure.
 */
public interface HokoLinkGenerationListener {
    public void onLinkGenerated(String hokolink);

    public void onError(Exception e);
}
