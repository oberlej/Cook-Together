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

/**
 * Created by jeremiaoberle on 1/7/17.
 */

public abstract class AbstractBaseFragment extends Fragment {
    protected AbstractBaseActivity mParent;

    protected abstract void init(View view);

    public DatabaseReference getDB() {
        return mParent.getDB();
    }

    public FirebaseAuth getAuth() {
        return mParent.getAuth();
    }


    public boolean isConnected() {
        return mParent.isConnected();
    }

    public void checkIsConnected() {
        mParent.checkIsConnected();
    }

    public FirebaseUser getCurrentUser() {
        return mParent.getCurrentUser();
    }

    public String getUid() {
        return mParent.getUid();
    }

    public void logout() {
        mParent.logout();
    }
}
