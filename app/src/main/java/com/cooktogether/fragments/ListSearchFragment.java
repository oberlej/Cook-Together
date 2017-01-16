package com.cooktogether.fragments;

import android.view.View;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.adapter.MealsListAdapter;
import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.viewholder.MealViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListSearchFragment extends AbstractMealListFragment {
    ArrayList<Meal> othersMeals;

    public static ListSearchFragment newInstance() {
        return new ListSearchFragment();
    }

    public ListSearchFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query allPosts = databaseReference.child("meals");
        return allPosts;
    }

    @Override
    protected void setAdapter(Query mealsQuery) {
        othersMeals = new ArrayList<Meal>();
        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot mealsSnapshot) {
                for (DataSnapshot mealSnap : mealsSnapshot.getChildren()) {
                    Meal meal = Meal.parseSnapshot(mealSnap);
                    if (!meal.getUserKey().equals(getUid()))
                        othersMeals.add(meal);
                }
                ((MealsListAdapter) mAdapter).setMeals(othersMeals); //= new MealsListAdapter(othersMeals);
                mRecycler.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        mAdapter = new MealsListAdapter(othersMeals);
        mRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Meal selectedMeal = ((MealsListAdapter) mAdapter).getSelectedMeal(position);
                        // Launch Meal Details Fragment
                        ((HomeActivity) mParent).setMealKey(selectedMeal.getMealKey());
                        ((HomeActivity) mParent).goToMeal(selectedMeal.getMealKey());
                    }
                })
        );

    }

    @Override
    protected void cleanAdapter() {
        if (mAdapter != null) {
            ((MealsListAdapter) mAdapter).cleanup();
        }
    }


}