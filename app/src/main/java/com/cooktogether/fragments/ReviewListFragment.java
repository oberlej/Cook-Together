package com.cooktogether.fragments;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.ReviewArrayAdapter;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Review;
import com.cooktogether.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReviewListFragment extends ListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        User u = ((HomeActivity) getActivity()).getUser();
        if (u != null) {
            View view = inflater.inflate(R.layout.fragment_review_list, container, false);
            ReviewArrayAdapter adapter = new ReviewArrayAdapter(getContext(), R.layout.item_review, u.getReviews());
            // Attach the adapter to a ListView
            setListAdapter(adapter);
            return view;
        } else {
            Toast.makeText(getContext(), "Failed to load reviews. Please try again.", Toast.LENGTH_LONG).show();
            ((HomeActivity) getActivity()).loadDefaultScreen();
            return null;
        }
    }
}

