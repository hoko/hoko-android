package com.hokolinks.deeplinking;

import com.hokolinks.model.Deeplink;
import com.hokolinks.model.DeeplinkCallback;
import com.hokolinks.model.FilterCallback;

import java.util.ArrayList;

/**
 * This class serves the purpose of abstracting the call of filters from the Deeplinking
 * module, it calls the filters in the order they were added.
 */
public class Filtering {

    private ArrayList<FilterCallback> mFilters;

    public Filtering() {
        mFilters = new ArrayList<>();
    }

    /**
     * Adds a FilterCallback object to the registered filters.
     *
     * @param callback A FilterCallback object.
     */
    public void addFilter(FilterCallback callback) {
        mFilters.add(callback);
    }

    /**
     * Removes a FilterCallback object from the registered filters.
     *
     * @param callback A FilterCallback object.
     * @return true if filter was removed, false otherwise.
     */
    public boolean removeFilter(FilterCallback callback) {
        return mFilters.remove(callback);
    }

    /**
     * Calls all the filters to make sure the deeplink should be opened.
     *
     * @param deeplink A deeplink object.
     */
    public boolean filter(Deeplink deeplink) {
        for (FilterCallback filterCallback : mFilters) {
            if (!filterCallback.openDeeplink(deeplink)) {
                return false;
            }
        }
        return true;
    }

}
