package com.cooktogether.helpers;

import android.content.Intent;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by jeremiaoberle on 1/9/17.
 */
public interface BasicFunctions {

    public DatabaseReference getDB();

    public FirebaseAuth getAuth();

    public boolean isConnected();

    public void checkIsConnected();

    public FirebaseUser getCurrentUser();

    public String getUid();

    public void logout();

    public FirebaseStorage getStorage();

    public StorageReference getRootRef();
}
