package com.cooktogether.fragments;

import com.cooktogether.helpers.AbstractMealListFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class SearchListFragment extends AbstractMealListFragment {

    public static SearchListFragment newInstance() {
        return new SearchListFragment();
    }

    public SearchListFragment() {

    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query allPosts = databaseReference.child("meals").orderByChild("userKey").equalTo(false,getUid());
        //todo only other people posts
        return allPosts;
    }
}
