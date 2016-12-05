package com.cooktogether.mainscreens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.fragments.LogInFragment;
import com.cooktogether.fragments.SignUpFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthenticationActivity extends AppCompatActivity {
    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseUser mFirebaseUser;

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
}
