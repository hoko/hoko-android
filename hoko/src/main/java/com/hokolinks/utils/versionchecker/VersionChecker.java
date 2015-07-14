package com.hokolinks.utils.versionchecker;

import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class VersionChecker {

    private static boolean requiresUpdate(String currentVersion, String githubVersion) {
        String normalisedCurrentVersion = normalisedVersion(currentVersion);
        String normalisedGithubVersion = normalisedVersion(githubVersion);
        return normalisedCurrentVersion.compareTo(normalisedGithubVersion) < 0;

    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String separator, int maxWidth) {
        String[] split = Pattern.compile(separator, Pattern.LITERAL).split(version);
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : split) {
            stringBuilder.append(String.format("%" + maxWidth + 's', string));
        }
        return stringBuilder.toString();
    }

    public static void checkForNewVersion(final String currentVersion, String token) {
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.GET,
                HttpRequest.getURLFromPath("version"), token, null).toRunnable(new HttpRequestCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                String version = jsonObject.optString("version");
                if (version != null) {
                    if (requiresUpdate(currentVersion, version)) {
                        android.util.Log.e("HOKO", "A new version of HOKO is available, please " +
                                "update your gradle.properties to \"compile 'com.hokolink:hoko:" +
                                version + "'\"");
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                HokoLog.e(e);
            }

        })).execute();
    }

}
