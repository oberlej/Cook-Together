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
    //private FirebaseRecyclerAdapter<Meal, MapMealViewHolder> mAdapter;

    public  SearchActivity(){}
   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();
        setContentView(R.layout.activity_localization);
        mRecycler = (RecyclerView) findViewById(R.id.meals_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getApplicationContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query mealsQuery = getQuery(getDB());

        mAdapter = new FirebaseRecyclerAdapter<Meal, MapMealViewHolder>(Meal.class, R.layout.item_meal, MapMealViewHolder.class, mealsQuery) {

            @Override
            protected Meal parseSnapshot(DataSnapshot snapshot) {
                return Meal.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final MapMealViewHolder viewHolder, final Meal model, final int position) {
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
    }*/

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query allPosts = databaseReference.child("meals").orderByChild(getUid());
        //todo only other people posts
        return allPosts;
    }
}
