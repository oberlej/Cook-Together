package com.cooktogether.mainscreens;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.fragments.LogInFragment;
import com.cooktogether.fragments.SignUpFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationActivity extends AppCompatActivity {
    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;

    private Fragment mLogIn;
    private Fragment mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.app_name);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //init fragments
        mLogIn = new LogInFragment().newInstance();
        mSignUp = new SignUpFragment().newInstance();

        getSupportFragmentManager().beginTransaction().add(R.id.activity_authentication, mLogIn).hide(mLogIn).add(R.id.activity_authentication, mSignUp).hide(mSignUp).commit();

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    loadHome();
                } else {
                    // User is signed out
                    showLogIn();
                }
            }
        };


        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            showLogIn();
        } else {
            loadHome();
        }
    }

    private void loadHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLogIn() {

        getSupportFragmentManager().beginTransaction().hide(mSignUp).show(mLogIn).commit();
    }

    public void showSignUp() {
        getSupportFragmentManager().beginTransaction().hide(mLogIn).show(mSignUp).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public CallbackManager getCallbackManager() {
        return mCallbackManager;
    }
}
