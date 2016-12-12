package com.cooktogether.mainscreens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.R;
import com.cooktogether.helpers.AbstractMealListActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MapMealViewHolder;
import com.cooktogether.viewholder.MealViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class SearchActivity extends AbstractMealListActivity {

    public  SearchActivity(){}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query allPosts = databaseReference.child("meals");
        //todo only other people posts
        return allPosts;
    }
}
