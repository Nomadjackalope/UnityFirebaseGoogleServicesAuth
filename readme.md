Signing in to Firebase with Unity using Google Services as the token provider
October 2017

Difficulty - INTERMEDIATE, assumes knowledge of Firebase and Android

Desciption: Create an Android Studio library .aar and integrate it with a Unity project containing the Unity Firebase Sdk in order to use Google accounts on Android to authenticate users with Firebase Authentication.


Downloads:

* [Firebase SDK](https://firebase.google.com/download/unity)
* google-services.json for com.yourpackagename & com.yourpackagenamelib
    
Software:
* Unity (I used 2017.2.0b2 for Linux 64bit)
* Android Studio (I used 2.3.3)
* OS: I used Elementary OS - Linux 64bit




## Setup Firebase

Open a new Unity project. 2d, no analytics

The Long: [Add Firebase SDK Auth package](https://firebase.google.com/docs/unity/setup)

The Quick: Assets>Import Package>Custom Package navigate to extracted sdk folder and add FirebaseAuth.unitypackage, import all

In your Firebase project, add two android apps: com.yourpackagename & com.yourpackagenamelib

[Add sha1 to firebase projects](https://developers.google.com/android/guides/client-auth)

For debug keytool, there is no password, just hit enter

Add com.yourpackagename google-services.json to folder Assets/Plugins/Android/

## Build Settings
Switch to Android Build File>Build Settings>Android>Switch Platform Button

Set Build System to Gradle

Tick On Export Project, Development Build, Autoconnect Profiler and Script Debugging

Exit Build Settings


Change Package Name to com.yourpackagename

1. either with popup from Firebase
2. or Edit>Project Settings>Player>Android>Other Settings>Package Name

Turn on Custom Gradle Template in Edit>Project Settings>Player>Android>Publishing Settings>Turn On Custom Gradle Template

Navigate to Assets/Plugins/Android/ and open mainTemplate.gradle with an editor

Change it like so

```
mainTemplate.gradle

remove // GENERATED BY UNITY...
buildscript {
    ...
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'
		classpath 'com.google.gms:google-services:3.0.0'
	}
}

allprojects {
    repositories {
        ...
        maven {
            url "https://maven.google.com"
        }
    }
}

...

dependencies {
	compile 'com.google.firebase:firebase-core:11.4.0'
	compile fileTree(dir: 'libs', include: ['*.jar'])
**DEPS**
compile 'com.google.android.gms:play-services-auth:11.4.0'
}

apply plugin: 'com.google.gms.google-services'

android {
    ...
    packagingOptions {
        pickFirst 'jsr305_annotations/Jsr305_annotations.gwt.xml'
        pickFirst 'build-data.properties'
        pickFirst 'third_party/java_src/error_prone/project/annotations/Google_internal.gwt.xml'
        pickFirst 'third_party/java_src/error_prone/project/annotations/Annotations.gwt.xml'
        pickFirst 'error_prone/Annotations.gwt.xml'
        pickFirst 'protobuf.meta'
    }
}

```

## Unity Scripts and Scene

Add Scripts folder in Assets folder and put in GradleTestWrapper and MainActivity with the following code

```
GradleTestWrapper.cs

using UnityEngine;

public class GradleTestWrapper : MonoBehaviour {

	public static GradleTestWrapper instance;

	private AndroidJavaObject mTestObj;
	private AndroidJavaClass unityClass;
	private AndroidJavaObject unityActivity;

	public GradleTestWrapper() {
		if (instance == null)
			instance = this;

		mTestObj = new AndroidJavaObject("com.nomadjackalope.YOURPACKAGE.GoogleServicesWrapper");
		unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		unityActivity = unityClass.GetStatic<AndroidJavaObject>("currentActivity");

	}

	public void startUpGoogleSignIn() {
		Debug.Log("GTW| startUpGoogleSignIn");
		mTestObj.Call("setupGoogleSignIn", unityActivity);
	}

	public void signIn() {
		Debug.Log("GTW| signIn");		
		mTestObj.Call("signIn", unityActivity);
	}

	public string getToken() {
		string token = mTestObj.Call<string>("getToken");
		Debug.Log("GTW| token: " + token);
		return token;
	}
}

```

```
MainActivity.cs

using UnityEngine;
using UnityEngine.UI;

public class MainActivity : MonoBehaviour {

	GradleTestWrapper test;

	// Use this for initialization
	void Start () {
		test = new GradleTestWrapper();
			
		InitializeFirebase();
	}

	public void click() {
		Debug.Log("MA| clicked");

		test.startUpGoogleSignIn();
		test.signIn();

		tokenAcquired = false;
	}

	int count = -1;
	bool tokenAcquired = true;
	string token;

	void FixedUpdate() {
		if(count > 0 && count % 100 == 0 && !tokenAcquired) {
			Debug.Log("MA| trying for token");
			token = test.getToken();
			if(token != "") {
				tokenAcquired = true;
				Debug.Log("MA| token acquired: " + token);
				// Once we have the token get the user into Firebase
				authenticateFirebase(token, null);
			}
		}
		count++;
	}

	//------------- Firebase ----------------

	private Firebase.Auth.FirebaseAuth auth;
	private Firebase.Auth.FirebaseUser user;

	private string displayName;
	private string emailAddress;

	void InitializeFirebase() {
		auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
		auth.StateChanged += AuthStateChanged;
		AuthStateChanged(this, null);
	}

	void AuthStateChanged(object sender, System.EventArgs eventArgs) {
		if (auth.CurrentUser != user) {
			bool signedIn = user != auth.CurrentUser && auth.CurrentUser != null;
			if (!signedIn && user != null) {
				Debug.Log("Signed out " + user.UserId);
			}
			user = auth.CurrentUser;
			if (signedIn) {
				Debug.Log("Signed in " + user.UserId);
				displayName = user.DisplayName ?? "";
				emailAddress = user.Email ?? "";
				Debug.Log("Signed in email: " + emailAddress);
			}
		}
	}

	void authenticateFirebase(string googleIdToken, string googleAccessToken) {
		Firebase.Auth.Credential credential =
		Firebase.Auth.GoogleAuthProvider.GetCredential(googleIdToken, googleAccessToken);
		auth.SignInWithCredentialAsync(credential).ContinueWith(task => {
		if (task.IsCanceled) {
			Debug.LogError("SignInWithCredentialAsync was canceled.");
			return;
		}
		if (task.IsFaulted) {
			Debug.LogError("SignInWithCredentialAsync encountered an error: " + task.Exception);
			return;
		}

		Firebase.Auth.FirebaseUser newUser = task.Result;
		Debug.LogFormat("User signed in successfully: {0} ({1})",
			newUser.DisplayName, newUser.UserId);
		});
	}

	public void signOut() {
		auth.SignOut();
	}

	void onDestroy() {
		auth.StateChanged -= AuthStateChanged;
		auth = null;
	}
}

```

Save the default scene as MainScene

Drag MainActivity onto Main Camera

Add a new object>UI>Button

Select the Canvas, change render mode to Screen Space - Camera, drag the MainCamera in, and change scale factor to 2

Select the Button, click on the anchor area, and do alt+center

Add an On Click () in the inspector under Button (Script) with the plus sign

Drag Main Camera on the object space in the newly created OnClick

Select MainActivity>click method in the dropdown

Save the scene

Go back to Build Settings and click Add Open Scenes

## Android Studio Google Services Wrapper

Open a new Android Studio project.

Name it so that Package name is **com.yourpackagenamelib** with a lib and create a no activity project (I used api version 16)

Delete androidTest and Test folders from the Java directory

Make two classes; GoogleServicesWrapper & GoogleSignInActivity

Put in the following code

```
GoogleServicesWrapper

package com.yourpackagenamelib;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by YOU on 10/17/17.
 */

public class GoogleServicesWrapper {

    private static final String TAG = "L_GSW| ";

    private static final int RC_SIGN_IN = 392;

    public static GoogleServicesWrapper instance;

    private GoogleApiClient googleApiClient;

    private String token;

    public GoogleServicesWrapper() {
        if(instance == null) {
            instance = this;
        }
    }

    // Startup a new GoogleSignIn client from an existing activity
    private void setupGoogleSignIn(Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getApplicationContext().getString(R.string.webClientId)) //R.string.clientId))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                //.enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public void signIn(Activity activity) {
        // Use unity activity passed in to start a new activity that handles callback(onActivityResult)
        Intent intent = new Intent(activity.getApplicationContext(), GoogleSignInActivity.class);
        activity.startActivity(intent);

    }

    public GoogleApiClient getClient() {
        return googleApiClient;
    }

    public void setToken(String token) {
        Log.d(TAG, "token: " + token);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

```

```
GoogleSignInActivity

package com.yourpackagenamelib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by YOU on 10/17/17.
 */

public class GoogleSignInActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 392;
    private static final String TAG = "L_GSIA| ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signIn(GoogleServicesWrapper.instance.getClient());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void signIn(GoogleApiClient client) {
        Log.d(TAG, "signInCalled");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        startActivityForResult(signInIntent, RC_SIGN_IN); // if 2nd arg is >0, the number will be returned when the activity exits
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult called");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        Log.d(TAG, "handleSignInStatus: " + result.getStatus().toString());
        if(result.isSuccess()) {
            // Signed in successfully, show authenticated UI
            GoogleSignInAccount acct = result.getSignInAccount();
            GoogleServicesWrapper.instance.setToken(acct.getIdToken());
        }
    }
}

```

Make AndroidManifest.xml look like the following

```
AndroidManifest.xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"

    package="com.yourpackagenamelib">

    <application
        android:label="@string/app_name">
        <activity android:name=".GoogleSignInActivity"></activity>
    </application>

</manifest>

```

Put your OAuth 2.0 Web application Client id from https://console.cloud.google.com/apis/credentials into strings.xml as webClientId

Make styles.xml like so. Many of these changes are fixing conflicts that would happen later on so don't skip them.

```
styles.xml

<resources>

    <!-- Base application theme. -->
    <style name="AppTheme" parent="android:Theme.Light.NoTitleBar.Fullscreen">
        <!-- Customize your theme here. -->
    </style>

</resources>

```

Delete the mipmap folder and say yes to deleting the sub directories

add *classpath 'com.google.gms:google-services:3.0.0' as shown

```
build.gradle(Project: YOURPROJECT)

buildscript {
    ...
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'
        classpath 'com.google.gms:google-services:3.0.0'
    }
}
...

```

Change application to library and add google services to build.gradle(Module:app) [more info about Android Libraries](https://developer.android.com/studio/projects/android-library.html)

```
build.gradle(Module: app)

apply plugin: 'com.android.library'

android {
    ...
    defaultConfig {
        //applicationId // comment this out
        ...
    }
}

dependencies {
    ...
    compile 'com.google.android.gms:play-services-auth:9.8.0'
}

apply plugin: 'com.google.gms.google-services'


```

Put the google-services.json for com.yourpackagenamelib into the app folder. An easy way to get there is right click app in Android Studio Project view and click "show in files" or "show in folder" You should see a few folders: app, build, and gradle. put the json in the app folder.

Build the module with the hammer icon or Build>Make Project

Almost there! Take a breather. Hopefully you haven't encountered too many issues yet :)

## Integrate Library Into Unity

Navigate to the root folder of your com.yourpackagenamelibs Android Studio project, you should see a few folders: app, build, and gradle.

Drag the app/build/outputs/aar/app-debug.aar file into Unity Project Assets/Plugins/Android


## Build Android APK

Now, build the Unity project by going File>Build Settings>Export and name it v1

Building Player can take a long time the first time and looks like it's hanging, just wait. Mine took around 2 minutes.

Open the newly created Gradle project by going into Android Studio>File>Open and navigating to the v1 folder but opening YOURPROJECT folder with Android Studio

Gradle sync issues might happen, try clicking "re-download dependencies and sync project"

Copy your com.yourpackagename google-services.json into v1/YOURPROJECT folder

Now you can Run with the play button to your device. Watching the Android Monitor you will see your token logged when you click the button.

## Please Help

If there are any simplifications to the process or files please submit a change.

I likely won't be able to help you if you have many problems because I just scraped this together from a lot of searching and trial and error.

If you know how to get the google-services.json into app folder from Unity, we wouldn't need to export project. I think that Unity could build and run, speeding up the build process quite a bit

The activity stays open and you have to press back to get back to unity.

I have only gone through this tutorial in using Debug mode for everything. Let me know if something breaks because of release.

## Likely issue

If you have trouble getting a SUCCESS returned, use the libTest project to debug.

You might also try using your final android studio project, open gradle window on the far right YOURPROJECT>Tasks>Android> double click "signing report" and add that sha1 key your main firebase app, not the lib one.




 







