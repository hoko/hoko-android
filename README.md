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

1. Download the [Hoko SDK](https://github.com/hokolinks/hoko-android/archive/master.zip).
2. Be sure your project includes the `Android Support Library v4` and the `Google Play Services` if you want to use the push notifications module.

```java
// Build.gradle
dependencies {
	compile fileTree(include: ['*.jar'], dir: 'libs')
	compile 'com.google.android.gms:play-services:6.5.87'
	compile 'com.android.support:support-v4:21.0.3'
}
```

## Start using Hoko

### SDK Setup

In your `Application` subclass setup the Hoko Framework in the `onCreate(...)` method:

```java
@Override
public void onCreate() {
	super.onCreate();
	Hoko.setup(this, "YOUR-APP-TOKEN", "YOUR-GCM-TOKEN");
}
```


Where your `Google Cloud Messaging Token` is the `Project Number` generated when a Google API aplication is registered on the [Google Developer Console](https://code.google.com/apis/console/).

### 2. Setting up the AndroidManifest.xml

To register a URL Scheme on your application you must add `HokoActivity` to your `AndroidManifest.xml`, making sure to replace your `URL scheme` with an appropriate scheme in **reverse DNS notation** (e.g. *com.hoko.hokotestbed*).

To make sure your application receives notifications from the `Hoko` service, you have to add the boilerplate code to your `AndroidManifest.xml` as well.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
	package="===YOUR.PACKAGE.NAME===">

	<!-- Required permissions for push notifications and Hoko integration -->
	<permission
		android:name="===YOUR.PACKAGE.NAME===.permission.C2D_MESSAGE"
		android:protectionLevel="signature" />

	<uses-permission android:name="===YOUR.PACKAGE.NAME===.permission.C2D_MESSAGE" />
	<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
	<uses-permission android:name="android.permission.WAKE_LOCK" />
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
		<meta-data
			android:name="com.google.android.gms.version"
			android:value="@integer/google_play_services_version" />
            		
		<!--Hoko notification receiver -->
		<receiver
			android:name="com.hoko.pushnotifications.HokoNotificationReceiver"
			android:permission="com.google.android.c2dm.permission.SEND"> 
			<intent-filter>
				<action android:name="com.google.android.c2dm.intent.RECEIVE" />
				<category android:name="===YOUR.PACKAGE.NAME===" />
			</intent-filter>
		</receiver>
		<service
			android:name="com.hoko.pushnotifications.HokoNotificationHandler"
			android:exported="true" />
		<!-- End of Hoko Code -->
		
	</application>
</manifest>
```

### 3. Deeplinking

To map routes to your `Activities` all you have to do is use the proper annotations.

```java
@HokoDeeplinkable("product/:product_id")
public class ProductActivity extends Activity {

	@HokoRouteParameter("product_id")
	private int mProductId;
	
	@HokoQueryParameter("product_price")
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
