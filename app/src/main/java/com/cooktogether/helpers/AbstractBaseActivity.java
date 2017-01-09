package com.cooktogether.helpers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public abstract class AbstractBaseActivity extends AppCompatActivity implements BasicFunctions{
    protected abstract void init();

    @Override
    public DatabaseReference getDB() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    @Override
    public boolean isConnected() {
        return getAuth().getCurrentUser() != null;
    }

    @Override
    public void checkIsConnected() {
        if (!isConnected()) {
            logout();
        }
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    @Override
    public String getUid() {
        if (isConnected()) {
            return getCurrentUser().getUid();
        }
        return null;
    }

    @Override
    public void logout() {
        if (isConnected()) {
            getAuth().signOut();
        }

        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public FirebaseStorage getStorage() {
        return FirebaseStorage.getInstance();
    }

    @Override
    public StorageReference getRootRef() {
        return getStorage().getReferenceFromUrl("gs://cook-together-314b4.appspot.com");
    }
}
