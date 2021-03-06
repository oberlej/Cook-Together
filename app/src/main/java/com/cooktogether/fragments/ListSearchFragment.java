package com.cooktogether.fragments;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.cooktogether.adapter.MealsListAdapter;
import com.cooktogether.helpers.AbstractMealListFragment;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.listener.OnBackPressListener;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListSearchFragment extends AbstractMealListFragment  implements OnBackPressListener {
    private ArrayList<Meal> othersMeals;
    private HashMap<String, User> users;

    public static ListSearchFragment newInstance() {
        return new ListSearchFragment();
    }

    public ListSearchFragment() {
        othersMeals = new ArrayList<Meal>();
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query allPosts = databaseReference.child("meals");
        return allPosts;
    }

    @Override
    public void setAdapter(Query mealsQuery) {
        users = new HashMap<String, User>();

        mealsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot mealsSnapshot) {
                users.clear();
                othersMeals.clear();
                for (DataSnapshot mealSnap : mealsSnapshot.getChildren()) {
                    final Meal meal = Meal.parseSnapshot(mealSnap);
                    if (!meal.getUserKey().equals(getUid())) {
                        othersMeals.add(meal);
                        users.put(meal.getMealKey(), null);
                        getDB().child("users").child(meal.getUserKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = User.parseSnapshot(dataSnapshot);
                                users.put(meal.getMealKey(), user);
                                if(checkAllUsersLoaded()) {
                                    ((MealsListAdapter) mAdapter).setUsers(users);
                                    ((MealsListAdapter) mAdapter).setMeals(othersMeals);
                                    mRecycler.setAdapter(mAdapter);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                ((MealsListAdapter) mAdapter).setContext(getContext());
                ((MealsListAdapter) mAdapter).setDb(getDB());
                ((MealsListAdapter) mAdapter).setRootRef(getRootRef());
                mEmptyList.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (getContext() != null)
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
                        //to get the user pic
                        ((HomeActivity)mParent).setToVisit(users.get(selectedMeal.getMealKey()));
                        ((HomeActivity) mParent).goToMeal(selectedMeal.getMealKey());
                    }
                })
        );
        if (othersMeals.isEmpty()) {
            mEmptyList.setText("No meals have been proposed by other people lately. Make new propositions or come back later!");
            mEmptyList.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkAllUsersLoaded() {
        for(User user :users.values())
            if (user == null)
                return false;
        return true;
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
