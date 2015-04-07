<p align="center" >
<img src="https://s3-eu-west-1.amazonaws.com/hokoassets/hoko_logo.png" alt="Hoko" title="Hoko">
</p>

[![Build Status](https://travis-ci.org/hokolinks/hoko-android.svg?branch=open_source)](https://travis-ci.org/hokolinks/hoko-android)

# What's Hoko?

The Hoko Framework is a easy to use deep linking framework that enables an app to map deep linking routes to actual behavior in the app. This behavior can include showing a particular screen or performing a certain action. Hoko also provides new ways for users to be redirected to your app regardless of the platform they are on.

After integrating Hoko in your app you should be able to open your app by opening URI links such as...

```
your.app.scheme://<mapped_route>/<route_param>?<query_params>
```

# Quick Start - Hoko Framework for Android

This document is just a quick start introduction to the Hoko Framework for Android. You can read the full documentation at [http://hokolinks.com/documentation#android](http://hokolinks.com/documentation#android).

To integrate Hoko in your app, just follow the 4 simple steps below after adding it to your project.

## Add Hoko to your project

1. Add Hoko to your `gradle.build` file.

```java
// Build.gradle
dependencies {
	compile 'com.hokolinks:hoko:1.2.1@aar'
}
```

## Start using Hoko

### SDK Setup

In your `Application` subclass setup the Hoko Framework in the `onCreate(...)` method:

```java
@Override
public void onCreate() {
	super.onCreate();
	Hoko.setup(this, "YOUR-APP-TOKEN");
}
```

### 2. Setting up the AndroidManifest.xml

To register a URL Scheme on your application you must add `HokoActivity` to your `AndroidManifest.xml`, making sure to replace your `URL scheme` with an appropriate scheme in **reverse DNS notation** (e.g. *com.hoko.hokotestbed*).


```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	package="===YOUR.PACKAGE.NAME===">

	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
	<application
		android:name="===YOUR.PACKAGE.NAME===.Application">
		<!-- Your other activities go here -->
		<!-- Start of Hoko Code -->
		<activity
			android:name="com.hoko.activity.HokoActivity"
			android:alwaysRetainTaskState="true"
			android:launchMode="singleTask"
			android:noHistory="true"
			android:theme="@android:style/Theme.NoDisplay">
			<intent-filter>
				<data android:scheme="===YOUR-URL-SCHEME===" />
				<action android:name="android.intent.action.VIEW" />
				
				<category android:name="android.intent.category.VIEW" />
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

### 3. Deeplinking

#### Activities

To map routes to your `Activities` all you have to do is use the proper annotations.

```java
// ProductActivity.java
@DeeplinkRoute("product/:product_id")
public class ProductActivity extends Activity {

	@DeeplinkRouteParameter("product_id")
	private int mProductId;
	
	@DeeplinkQueryParameter("product_price")
	private String mProductPrice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Hoko.deeplinking().inject(this);
	}
}
```

This will make sure that any incoming deeplinks with a certain route format will map its variables to your annotated variables.

### 4. Analytics

In order to provide you with metrics on push notifications and deeplinking campaigns, it is advised to delegate key events to the Analytics module (e.g. in-app purchases, referals, etc).

All you have to do is:

```java
Hoko.analytics().trackKeyEvent("dress_purchase", 29.99);
```

You can also identify your users to create targeted campaigns on Hoko.

```java
Hoko.analytics().identifyUser("johndoe", HokoUserAccountType.GITHUB, "John Doe", "johndoe@hoko.com", new Date(), HokoUserGender.MALE);
```

### Full documentation

We recommend you to read the full documentation at [http://hokolinks.com/documentation#android](http://hokolinks.com/documentation#android).


# Author

Hoko, S.A.

# License

Hoko is available under the Apache license. See the LICENSE file for more info.
