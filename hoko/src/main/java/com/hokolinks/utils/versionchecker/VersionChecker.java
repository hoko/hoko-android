package com.hokolinks.utils.versionchecker;

import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class VersionChecker {

    private static final String HokoVersionCheckerGitHubApi =
            "https://api.github.com/repos/hokolinks/hoko-android/releases?per_page=1";
    private static final String HokoVersionCheckerGitHubVersionName = "tag_name";
    private static VersionChecker mInstance;

    public static VersionChecker getInstance() {
        if (mInstance == null) {
            mInstance = new VersionChecker();
        }
        return mInstance;
    }

    private static boolean requiresUpdate(String currentVersion, String githubVersion) {
        String normalisedCurrentVersion = normalisedVersion(currentVersion);
        String normalisedGithubVersion = normalisedVersion(githubVersion);
        return normalisedCurrentVersion.compareTo(normalisedGithubVersion) < 0;

    }

    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    public void checkForNewVersion(final String currentVersion) {
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.GET,
                HokoVersionCheckerGitHubApi, null, null).toRunnable(new HttpRequestCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                String versionName = jsonObject.optString(HokoVersionCheckerGitHubVersionName);
                if (versionName != null) {
                    String versionNumber = versionName.replace("v", "");
                    if (requiresUpdate(currentVersion, versionNumber)) {
                        android.util.Log.e("HOKO", "A new version of HOKO is available at "
                                + "http://github.com/hokolinks/hoko-android " + versionName);
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
