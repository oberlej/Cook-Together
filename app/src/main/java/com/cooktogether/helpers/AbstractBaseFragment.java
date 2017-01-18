package com.cooktogether.helpers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

import com.cooktogether.mainscreens.AuthenticationActivity;
import com.cooktogether.mainscreens.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by jeremiaoberle on 1/7/17.
 */

public abstract class AbstractBaseFragment extends Fragment implements BasicFunctions{
    protected AbstractBaseActivity mParent;

    public void onCreate(){

    }

    protected abstract void init(View view);

    @Override
    public DatabaseReference getDB() {
        return mParent.getDB();
    }

    @Override
    public FirebaseAuth getAuth() {
        return mParent.getAuth();
    }

    @Override
    public boolean isConnected() {
        return mParent.isConnected();
    }

    @Override
    public void checkIsConnected() {
        mParent.checkIsConnected();
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return mParent.getCurrentUser();
    }

    @Override
    public String getUid() {
        return mParent.getUid();
    }

    @Override
    public void logout() {
        mParent.logout();
    }

    @Override
    public FirebaseStorage getStorage() {
        return mParent.getStorage();
    }

    @Override
    public StorageReference getRootRef() {
        return mParent.getRootRef();
    }
}
