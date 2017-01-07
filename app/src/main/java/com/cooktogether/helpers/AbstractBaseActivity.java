package com.cooktogether.helpers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public abstract class AbstractBaseActivity extends AppCompatActivity {

    public DatabaseReference getDB() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseAuth getAuth(){
        return FirebaseAuth.getInstance();
    }


    public boolean isConnected() {
        return getAuth().getCurrentUser() != null;
    }

    public void checkIsConnected() {
        if (!isConnected()) {
            logout();
        }
    }

    public FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public String getUid() {
        if(isConnected()) {
            return getCurrentUser().getUid();
        }
        return null;
    }

    public void logout() {
        if (isConnected()) {
            getAuth().signOut();
        }

        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
