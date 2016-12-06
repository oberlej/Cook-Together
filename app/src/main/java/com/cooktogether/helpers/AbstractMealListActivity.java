package com.cooktogether.helpers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.mainscreens.MealActivity;
import com.cooktogether.model.Day;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MealViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractMealListActivity extends AbstractBaseActivity {
    private FirebaseRecyclerAdapter<Meal, MealViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public AbstractMealListActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
        setContentView(R.layout.activity_meal_list);
//        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.app_name);

        mRecycler = (RecyclerView) findViewById(R.id.meals_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getApplicationContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query mealsQuery = getQuery(getDB());

        mAdapter = new FirebaseRecyclerAdapter<Meal, MealViewHolder>(Meal.class, R.layout.item_meal, MealViewHolder.class, mealsQuery) {

            @Override
            protected Meal parseSnapshot(DataSnapshot snapshot) {
                return Meal.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final MealViewHolder viewHolder, final Meal model, final int position) {
                final DatabaseReference mealRef = getRef(position);

                // Set click listener for the whole meal view
                final String mealKey = mealRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getApplicationContext(), MealActivity.class);
                        intent.putExtra(getResources().getString(R.string.MEAL_KEY), mealKey);
                        startActivity(intent);
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}

