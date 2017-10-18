package com.YOURPACKAGENAMELIB;

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
 * Created by benjamin on 10/17/17.
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