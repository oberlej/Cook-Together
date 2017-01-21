package com.cooktogether.fragments;

import android.view.View;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.adapter.MealsListAdapter;
import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyMealsFragment extends AbstractMealListFragment {
    private boolean current;
    private ArrayList<Meal> myMeals;

    public static MyMealsFragment newInstance() {
        return new MyMealsFragment();
    }

    public MyMealsFragment() {
        myMeals = new ArrayList<Meal>();
        current = true;
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("meals").orderByChild("userKey").equalTo(((HomeActivity) getActivity()).getUid());
    }

    @Override
    public void setAdapter(Query mealsQuery) {
        if (getArguments().containsKey("current")) //it should be true
            current = getArguments().getBoolean("current");

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot mealsSnapshot) {
                for (DataSnapshot mealSnap : mealsSnapshot.getChildren()) {
                    Meal meal = Meal.parseSnapshot(mealSnap);
                    if (current && !meal.getBooked())
                        myMeals.add(meal);
                    else if (!current && meal.getBooked())
                        myMeals.add(meal);
                }
                if (!myMeals.isEmpty()) {
                    ((MealsListAdapter) mAdapter).setMeals(myMeals);
                    mRecycler.setAdapter(mAdapter);
                    mEmptyList.setVisibility(View.GONE);
                } else {
                    if (!current)
                        mEmptyList.setText("Nothing to show!");
                    mEmptyList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (getContext() != null)
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        mAdapter = new MealsListAdapter(myMeals);
        mRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Meal selectedMeal = ((MealsListAdapter) mAdapter).getSelectedMeal(position);
                        // Launch Meal Details Fragment
                        ((HomeActivity) mParent).setMealKey(selectedMeal.getMealKey());
                        ((HomeActivity) mParent).selectDrawerItem(((HomeActivity) mParent).getNvDrawer().getMenu().findItem(R.id.nav_meal_detail), getString(R.string.update_meal));
                    }
                })
        );
    }

    @Override
    public void cleanAdapter() {
        if (mAdapter != null) {
            ((MealsListAdapter) mAdapter).cleanup();
        }
    }

}
