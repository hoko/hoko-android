package com.hokolinks.utils.versionchecker;

import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class VersionChecker {

    private static final String GITHUB_API =
            "https://api.github.com/repos/hokolinks/hoko-android/releases?per_page=1";
    private static final String GITHUB_VERISON_KEY = "tag_name";

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

    public void checkForNewVersion(final String currentVersion) {
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.GET,
                GITHUB_API, null, null).toRunnable(new HttpRequestCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                String versionName = jsonObject.optString(GITHUB_VERISON_KEY);
                if (versionName != null) {
                    String versionNumber = versionName.replace("v", "");
                    if (requiresUpdate(currentVersion, versionNumber)) {
                        android.util.Log.e("HOKO", "A new version of HOKO is available, please " +
                                "update your gradle.properties to \"compile 'com.hokolink:hoko:" +
                                versionNumber + "'\"");
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
