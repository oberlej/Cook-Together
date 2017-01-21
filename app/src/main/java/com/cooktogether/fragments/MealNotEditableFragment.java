package com.cooktogether.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Day;
import com.cooktogether.model.DayEnum;
import com.cooktogether.model.Meal;
import com.cooktogether.model.Reservation;
import com.cooktogether.model.StatusEnum;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hela on 07/01/17.
 */

public class MealNotEditableFragment extends Fragment {
    private TextView title;
    private TextView description;
    private LinearLayout mListOfDays;

    private List<Day> daysNotFree;
    private List<Day> daysFree;
    private boolean mIsUpdate = false;

    private TextView locationName;
    private Button contact_btn;
    private Button reserve_btn;

    private String mealKey = null;
    private String mealUserKey = null;
    private HomeActivity mParent;
    private int mNbrReservations;
    private int mNbrPersons;
    private ProgressBar progressBar;
    private TextView progressBarTxt;

    public MealNotEditableFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_not_editable_meal, container, false);
        initFields(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }


    private void initFields(View view) {
        mParent = (HomeActivity) getActivity();
        mealKey = mParent.getMealKey();
        title = (TextView) view.findViewById(R.id.meal_title);
        description = (TextView) view.findViewById(R.id.meal_description);
        mListOfDays = (LinearLayout) view.findViewById(R.id.list_of_days);
        contact_btn = (Button) view.findViewById(R.id.contact_button);
        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact(v);
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        progressBarTxt = (TextView) view.findViewById(R.id.progress_bar_txt);

        reserve_btn = (Button) view.findViewById(R.id.reserve_btn);
        reserve_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserve(v);
            }
        });

        //for the list of location options
        locationName = (TextView) view.findViewById(R.id.meal_location);

        initNotFreeDays();
        loadMeal();
    }

    private void initNotFreeDays() {
        daysNotFree = new ArrayList<Day>();
        for (DayEnum de : DayEnum.values()) {
            daysNotFree.add(new Day(de));
        }
    }

    private void loadMeal() {
        mParent.getDB().child("meals").child(mealKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Meal meal = Meal.parseSnapshot(dataSnapshot);

                mealUserKey = meal.getUserKey();
                locationName.setText(meal.getLocation().toString());
                title.setText(meal.getTitle());
                description.setText(meal.getDescription());

                initNotFreeDays();
                daysFree = new ArrayList<Day>();
                for (Day d : meal.getFreeDays()) {
                    daysNotFree.remove(d);
                    daysFree.add(d);
                }
                updateFreeDaysLayout();

                mNbrReservations = meal.getNbrReservations();
                progressBar.setProgress(mNbrReservations);

                mNbrPersons = meal.getNbrPersons();
                progressBar.setMax(mNbrPersons);
                progressBarTxt.setText(mNbrReservations + "/" + mNbrPersons + " places reserved");
                if (meal.getBooked()) {
                    reserve_btn.setText("BOOKED");
                    reserve_btn.setEnabled(false);
                    contact_btn.setEnabled(false);
                }

                HashMap<String, String> rsv = new HashMap<String, String>();//userkey, reservation key
                if (dataSnapshot.hasChild("reservations")) {
                    for (DataSnapshot d : dataSnapshot.child("reservations").getChildren())
                        rsv.put((String) d.getValue(), d.getKey());
                }
                if (rsv.containsKey(mParent.getUid())) {
                    contact_btn.setEnabled(true);
                    Query q = mParent.getDB().child("reservations").child(rsv.get(mParent.getUid()));
                    q.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Reservation r = Reservation.parseSnapshot(dataSnapshot);
                            if (r.getStatus().equals(StatusEnum.WAITING.getStatus()))
                                reserve_btn.setText("WAITING FOR A RESPONSE");
                            else if (r.getStatus().equals(StatusEnum.ACCEPTED.getStatus()))
                                reserve_btn.setText("Reservation Accepted!");
                            else
                                reserve_btn.setText("Reservation Refused");
                            reserve_btn.setEnabled(false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                reserve_btn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String[] getNames(List<Day> list) {
        String[] names = new String[list.size()];
        int i = 0;
        for (Day d : list) {
            names[i++] = d.getName();
        }
        return names;
    }

    private void updateFreeDaysLayout() {
        mListOfDays.removeAllViews();
        for (Day d : daysFree) {
            View dayWrapper = mParent.getLayoutInflater().inflate(R.layout.item_day, mListOfDays, false);
            dayWrapper.setTag(R.id.TAG_DAY, d);
            ((ImageView) dayWrapper.findViewById(R.id.day_remove_btn)).setVisibility(View.GONE);
            ((TextView) dayWrapper.findViewById(R.id.day_name)).setText(d.getName());
            CheckBox lunch = ((CheckBox) dayWrapper.findViewById(R.id.day_lunch_cb));
            lunch.setChecked(d.isLunch());
            CheckBox dinner = ((CheckBox) dayWrapper.findViewById(R.id.day_dinner_cb));
            dinner.setChecked(d.isDinner());
            lunch.setClickable(false);
            dinner.setClickable(false);
            mListOfDays.addView(dayWrapper);
        }
    }

    public void contact(View view) {

        Query conversation = mParent.getDB().child("user-conversations").child(mParent.getUid()).child(mealKey);//mParent.getDB().child("users").child(mParent.getUid()).orderByChild(mealKey).equals(true); wont work until all the users are added to the users table
        String conversationKey = mealKey; // here we consider that the conversation key is the meal key

        List<String> usersKeys = new ArrayList<String>();
        usersKeys.add(mParent.getUid());
        usersKeys.add(mealUserKey);
        Conversation newConv = new Conversation(title.getText().toString(), conversationKey, usersKeys);

        HashMap<String, Object> convMap = newConv.toHashMap();
        convMap.remove("messages"); //to not delete previous messages if any
        conversation.getRef().updateChildren(convMap);

        mParent.goToConversation(conversationKey);

    }

    public void reserve(View v) {
        String reservationKey = mParent.getDB().child("reservations").push().getKey();
        Reservation newReserv = new Reservation(reservationKey, mParent.getUid(), mealKey, StatusEnum.WAITING);
        //set the new reservation
        mParent.getDB().child("reservations").child(reservationKey).setValue(newReserv);
        //update user reservations
        mParent.getDB().child("users").child(mParent.getUid()).child("reservations").child(reservationKey).setValue(true);
        //meals user reservations
        mParent.getDB().child("meals").child(mealKey).child("reservations").child(reservationKey).setValue(mParent.getUid());
        mParent.getDB().child("meals").child(mealKey).child("nbrReservations").setValue(mNbrReservations + 1);
        if (mNbrReservations + 1 == mNbrPersons) {
            mParent.getDB().child("meals").child(mealKey).child("booked").setValue(true);
        }
        Toast.makeText(getContext(), "A reservation demand has been!", Toast.LENGTH_LONG).show();
    }

    public static Fragment newInstance() {
        return new MealNotEditableFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((HomeActivity) mParent).mMealKey = null;
    }
}

