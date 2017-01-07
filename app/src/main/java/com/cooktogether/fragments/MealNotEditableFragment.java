package com.cooktogether.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Day;
import com.cooktogether.model.DayEnum;
import com.cooktogether.model.Meal;
import com.cooktogether.model.UserLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private String mealKey = null;
    private String mealUserKey = null;
    private HomeActivity mParent;

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

        //for the list of location options
        locationName = (TextView) view.findViewById(R.id.meal_location);

        initNotFreeDays();
        daysFree = new ArrayList<Day>();
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
                for (Day d : meal.getFreeDays()) {
                    daysNotFree.remove(d);
                    daysFree.add(d);
                }
                updateFreeDaysLayout();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load meal.", Toast.LENGTH_SHORT).show();
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
        String conversationKey = mParent.getDB().child("user-conversations").child(mParent.getUid()).push().getKey();

        List<String> usersKeys = new ArrayList<String>();
        usersKeys.add(mParent.getUid());
        usersKeys.add(mealUserKey);
        Conversation newConv = new Conversation(title.getText().toString(), conversationKey, usersKeys);

        mParent.getDB().child("user-conversations").child(mParent.getUid()).child(conversationKey).setValue(newConv);

        mParent.goToConversation(conversationKey);

    }

    public static Fragment newInstance() {
        return new MealNotEditableFragment();
    }
}
