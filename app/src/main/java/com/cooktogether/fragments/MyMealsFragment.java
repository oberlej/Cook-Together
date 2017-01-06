package com.cooktogether.fragments;

import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.mainscreens.HomeActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyMealsFragment extends AbstractMealListFragment {

    public static MyMealsFragment newInstance() {
        return new MyMealsFragment();
    }

    public MyMealsFragment() {

    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("meals").orderByChild("userKey").equalTo(((HomeActivity)getActivity()).getUid());
    }
}
