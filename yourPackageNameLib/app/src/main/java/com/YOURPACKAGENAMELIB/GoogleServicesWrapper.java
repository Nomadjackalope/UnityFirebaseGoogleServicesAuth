package com.YOURPACKAGENAMELIB;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by benjamin on 10/17/17.
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
                .requestIdToken(activity.getApplicationContext().getString(R.string.webClientId))
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
