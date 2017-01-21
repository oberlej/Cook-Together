package com.cooktogether.helpers;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.viewholder.MealViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;


public abstract class AbstractMealListFragment extends AbstractBaseFragment {
    protected RecyclerView.Adapter<MealViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;
    protected TextView mEmptyList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_list, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(final View view) {
        mParent = (HomeActivity) getActivity();
        mRecycler = (RecyclerView) view.findViewById(R.id.meals_list_rcv);
        mRecycler.setHasFixedSize(true);
        mEmptyList = (TextView) view.findViewById(R.id.meals_empty_list);

        //to be shown in case of empty list
        mEmptyList.setVisibility(View.VISIBLE);
        mEmptyList.setText("Make new propositions of meals you would like to cook and share with people around you and let the fun begin!");

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query mealsQuery = getQuery(getDB());
        setAdapter(mealsQuery);
        mRecycler.setAdapter(mAdapter);

    }

    public abstract void setAdapter(Query mealsQuery);

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanAdapter();
    }

    public abstract void cleanAdapter();

    public abstract Query getQuery(DatabaseReference databaseReference);
}

