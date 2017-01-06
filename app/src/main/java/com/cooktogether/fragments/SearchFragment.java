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
        Query allPosts = databaseReference.child("meals");
        //todo only other people posts
        return allPosts;
    }
}
