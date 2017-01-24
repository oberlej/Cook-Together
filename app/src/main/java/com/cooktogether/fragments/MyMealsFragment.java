package com.cooktogether.fragments;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.adapter.MealsListAdapter;
import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.listener.OnBackPressListener;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyMealsFragment extends AbstractMealListFragment implements OnBackPressListener {
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
                myMeals.clear();
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
                        ((HomeActivity) mParent).selectDrawerItem(((HomeActivity) mParent).getNvDrawer().getMenu().findItem(R.id.nav_meal_detail), getString(R.string.new_meal));
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

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() == null) return false;

        int childCount = getParentFragment().getChildFragmentManager().getBackStackEntryCount();
        if (childCount == 0) {
            // it has no child Fragment
            // can not handle the onBackPressed task by itself
            return false;

        } else {
            // get the child Fragment
            FragmentManager childFragmentManager = getParentFragment().getChildFragmentManager();
            OnBackPressListener childFragment = (OnBackPressListener) childFragmentManager.getFragments().get(0);

            // propagate onBackPressed method call to the child Fragment
            if (!childFragment.onBackPressed()) {
                // child Fragment was unable to handle the task
                // It could happen when the child Fragment is last last leaf of a chain
                // removing the child Fragment from stack
                childFragmentManager.popBackStackImmediate();

            }

            // either this Fragment or its child handled the task
            // either way we are successful and done here
            return true;
        }
    }
}
