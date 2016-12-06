package com.cooktogether.helpers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public abstract class AbstractBaseActivity extends AppCompatActivity {

    public DatabaseReference getDB() {
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mDatabase;
    }

    private DatabaseReference mDatabase = null;

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


}
