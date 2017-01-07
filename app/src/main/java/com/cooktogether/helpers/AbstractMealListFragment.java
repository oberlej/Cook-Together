package com.cooktogether.helpers;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MealViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public abstract class AbstractMealListFragment extends AbstractBaseFragment {
    private FirebaseRecyclerAdapter<Meal, MealViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;

    public AbstractMealListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_list, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();

        mRecycler = (RecyclerView) view.findViewById(R.id.meals_list_rcv);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
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
                        // Launch Meal Details Fragment
                        ((HomeActivity) mParent).setMealKey(mealKey);
                        //if the user is the one who proposed the meal, they can edit it
                        if (model.getUserKey().equals(mParent.getUid())) {
                            ((HomeActivity) mParent).selectDrawerItem(((HomeActivity) mParent).getNvDrawer().getMenu().findItem(R.id.nav_meal_detail), getString(R.string.update_meal));
                        } else {
                            ((HomeActivity) mParent).goToMeal(mealKey);
                        }
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

