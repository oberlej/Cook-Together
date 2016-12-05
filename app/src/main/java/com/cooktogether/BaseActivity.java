package com.cooktogether;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;


public abstract class BaseActivity extends AppCompatActivity {

    public boolean isConnected() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void checkIsConnected() {
        if (!isConnected()) {
            logout();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void logout() {
        if (isConnected()) FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
    }
}
