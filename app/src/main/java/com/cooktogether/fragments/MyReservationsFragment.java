package com.cooktogether.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.adapter.ReservationListAdapter;
import com.cooktogether.helpers.AbstractBaseFragment;
import com.cooktogether.listener.RecyclerItemClickListener;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Meal;
import com.cooktogether.model.Reservation;
import com.cooktogether.model.User;
import com.cooktogether.viewholder.ReservationViewHolder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hela on 18/01/17.
 */

public class MyReservationsFragment  extends AbstractBaseFragment {
    protected RecyclerView.Adapter<ReservationViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;
    private TextView mEmptyList;
    private ArrayList<Reservation> reservations;
    private ArrayList<Meal> meals;
    private HashMap<String, User> users;

    public static MyReservationsFragment newInstance() {
        return new MyReservationsFragment();
    }

    public MyReservationsFragment() {
        this.reservations = new ArrayList<Reservation>();
        this.meals = new ArrayList<Meal>();
        this.users = new HashMap<String, User>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation_list, container, false);
        init(view);
        return view;
    }

    @Override
    protected void init(final View view) {
        mParent = (HomeActivity) getActivity();
        mParent.getSupportActionBar().setTitle("My Reservations");
        ((HomeActivity)mParent).hideKeyboard(getContext());

        mRecycler = (RecyclerView) view.findViewById(R.id.reservation_list_rcv);
        mRecycler.setHasFixedSize(true);

        //to be shown in case of empty list
        mEmptyList = (TextView) view.findViewById(R.id.reservations_empty_list);


        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query reservationsQuery = getQuery(getDB());
        setAdapter(reservationsQuery);
        mRecycler.setAdapter(mAdapter);

    }

    protected void setAdapter(Query reservationsQuery) {

        reservationsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Reservation rsv = Reservation.parseSnapshot(dataSnapshot);
                if(((HomeActivity)mParent).getUser().getReservations().contains(rsv.getReservationKey())){
                    reservations.add(rsv);
                    getDB().child("meals").child(rsv.getMealKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Meal m = Meal.parseSnapshot(dataSnapshot);
                            meals.add(m);
                            mAdapter = new ReservationListAdapter(meals, reservations);
                            mRecycler.setAdapter(mAdapter);
                            mEmptyList.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    getDB().child("users").child(rsv.getUserKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = User.parseSnapshot(dataSnapshot);
                            users.put(rsv.getMealKey(), user);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if(reservations.isEmpty()) {
                    mEmptyList.setText("Nothing to show!");
                    mEmptyList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Meal selectedMeal = ((ReservationListAdapter) mAdapter).getSelectedMeal(position);
                        // Launch Meal Details Fragment
                        ((HomeActivity)mParent).setToVisit(users.get(selectedMeal.getMealKey()));
                        ((HomeActivity) mParent).goToMeal(selectedMeal.getMealKey());
                    }
                })
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanAdapter();
    }

    protected void cleanAdapter() {
        if (mAdapter != null) {
            ((ReservationListAdapter) mAdapter).cleanup();
        }
    }

    public Query getQuery(DatabaseReference databaseReference){
        return databaseReference.child("reservations");
    }
}