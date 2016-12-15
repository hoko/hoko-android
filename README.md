<p align="center" >
<img src="https://s3-eu-west-1.amazonaws.com/hokoassets/hoko_logo.png" alt="HOKO" title="HOKO">
</p>

[![Download](https://api.bintray.com/packages/hoko/maven/hoko-android/images/download.svg)](https://bintray.com/hoko/maven/hoko-android/_latestVersion)
[![Build Status](https://travis-ci.org/hoko/hoko-android.svg?branch=open_source)](https://travis-ci.org/hoko/hoko-android)

# Quick Start - HOKO for Android

This document is just a quick start introduction to the HOKO for Android. You can read the
full documentation at [http://hokolinks.com/documentation#android](http://hokolinks.com/documentation#android).

To integrate HOKO in your app, just follow the 3 simple steps below.

### 1. Add HOKO to your project

Add HOKO to your `gradle.build` file:

```java
// Build.gradle
dependencies {
	compile 'com.hokolinks:hoko:2.3.2'
}
```

In your `Application` subclass setup HOKO in the `onCreate(...)` method:

```java
@Override
public void onCreate() {
	super.onCreate();
	Hoko.setup(this, "YOUR-ANDROID-TOKEN");
}
```

You can find the Android token under "Settings" when you open your app on the HOKO dashboard.

### 2. Setup the AndroidManifest.xml

To register a URL Scheme on your application you must add `HokoActivity` to your
`AndroidManifest.xml`, making sure to replace your `URL scheme` with an appropriate scheme
in **reverse DNS notation** (e.g. *com.hoko.hokotestbed*).


```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	package="com.bananas">

	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

	<application
		android:name="com.bananas.BananasApplication">
		<!-- Your other activities go here -->
		<!-- Start of Hoko Code -->
		<activity
			android:name="com.hokolinks.activity.HokoActivity"
			android:alwaysRetainTaskState="true"
			android:launchMode="singleTask"
			android:noHistory="true"
			android:theme="@android:style/Theme.NoDisplay">
			<intent-filter>
				<data android:scheme="bananas" />
				<action android:name="android.intent.action.VIEW" />
				<category android:name="android.intent.category.DEFAULT" />
				<category android:name="android.intent.category.BROWSABLE" />
			</intent-filter>
		</activity>
		<activity
			android:name="com.hokolinks.activity.HokoAppLinksActivity"
			android:alwaysRetainTaskState="true"
			android:launchMode="singleTask"
			android:noHistory="true"
			android:theme="@android:style/Theme.NoDisplay">
			<intent-filter>
				<data
					android:host="bananas.hoko.link"
					android:scheme="http" />
				<data
					android:host="bananas.hoko.link"
					android:scheme="https" />
				<action android:name="android.intent.action.VIEW" />
				<category android:name="android.intent.category.DEFAULT" />
				<category android:name="android.intent.category.BROWSABLE" />
			</intent-filter>
		</activity>
		<receiver android:name="com.hokolinks.deeplinking.DeferredDeeplinkingBroadcastReceiver"
            		android:exported="true">
        		 <intent-filter>
                		<action android:name="com.android.vending.INSTALL_REFERRER" />
            		</intent-filter>
        	</receiver>

	</application>
</manifest>
```

### 3. Map your application.

To map routes to your `Activities` all you have to do is use the proper annotations.

```java
// ProductActivity.java
@DeeplinkRoute("product/:product_id")
public class ProductActivity extends Activity {
	// You should map your variables with @DeeplinkRouteParameter
	@DeeplinkRouteParameter("product_id")
	private int mProductId;

	// If you want you can also use @DeeplinkQueryParameter to map query parameters
	@DeeplinkQueryParameter("product_price")
	private String mProductPrice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Hoko.deeplinking().inject(this);
	}
}
```

The previous example would match an incoming link like this `product/29?product_price=19`

This will make sure that any incoming deep links with a certain route format will map its variables
to your annotated variables.

### Full documentation

We recommend you to read the full documentation at [http://support.hokolinks.com/quickstart/android/](http://support.hokolinks.com/quickstart/android/).
