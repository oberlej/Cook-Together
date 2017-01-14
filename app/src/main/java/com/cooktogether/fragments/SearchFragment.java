package com.cooktogether.fragments;

import com.cooktogether.helpers.AbstractMealListFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class SearchFragment extends AbstractMealListFragment {

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    public SearchFragment() {

    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query othersPosts = databaseReference.child("meals").orderByChild("userKey").equalTo(false,getUid());
        return othersPosts;
    }
}
