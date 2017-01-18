package com.cooktogether.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;

import com.cooktogether.adapter.locationOptionsAdapter;
import com.cooktogether.helpers.AbstractBaseFragment;

import com.cooktogether.helpers.AbstractLocationFragment;
import com.cooktogether.listener.RecyclerItemClickListener;

import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Day;
import com.cooktogether.model.DayEnum;
import com.cooktogether.model.Meal;
import com.cooktogether.model.UserLocation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyMealFragment extends AbstractLocationFragment implements View.OnClickListener {
    private LinearLayout mListOfDays;
    private EditText mTitle;
    private EditText mDescription;
    private List<Day> mDaysNotFree;

    private List<Day> mDaysFree;

    private String mMealKey = null;
    private String mUserKey;
    private boolean mIsUpdate = false;

    private boolean mAnswer;
    //private Button mContact_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal, container, false);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveMeal();
                ((HomeActivity) mParent).loadDefaultScreen();
                return true;
            case R.id.action_cancel:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case DialogInterface.BUTTON_POSITIVE:
                                String message = mIsUpdate ? "Changes discarded." : "Meal " + mTitle.getText().toString() + " discarded.";
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                ((HomeActivity) mParent).loadDefaultScreen();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                String message = mIsUpdate ? "Discard changes ?" : "Discard meal ?";
                builder.setMessage(message)
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveMeal() {
        if (getSelectedLocation() == null) {
            Toast.makeText(getContext(), "Unvalid location or no service available", Toast.LENGTH_LONG).show();
            return false;
            //todo Alert box to change the location
        }

        if (!mIsUpdate) {
            mMealKey = getDB().child("meals").push().getKey();
        }

        Meal m = new Meal(mTitle.getText().toString(), mDescription.getText().toString(), mParent.getUid(), mMealKey, mDaysFree, getSelectedLocation());

        getDB().child("meals").child(mMealKey).setValue(m);

        if (!mIsUpdate) {
            Toast.makeText(getContext(), "Meal " + mTitle.getText().toString() + " created.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Meal " + mTitle.getText().toString() + " updated.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void init(View view) {
        mParent = (HomeActivity) getActivity();

        view.findViewById(R.id.create_new_day_btn).setOnClickListener(this);

        //init location bar
        initLocationBar(view);

        mListOfDays = (LinearLayout) view.findViewById(R.id.create_list_of_days);
        mTitle = (EditText) view.findViewById(R.id.create_title);
        mDescription = (EditText) view.findViewById(R.id.create_description);

        initNotFreeDays();
        mDaysFree = new ArrayList<Day>();

        mMealKey = ((HomeActivity) mParent).getMealKey();
        mIsUpdate = mMealKey != null && !mMealKey.isEmpty();
        if (mIsUpdate) {
            loadMeal();
        }
    }

    private void initNotFreeDays() {
        mDaysNotFree = new ArrayList<Day>();
        for (DayEnum de : DayEnum.values()) {
            mDaysNotFree.add(new Day(de));
        }
    }

    private void loadMeal() {
        if (mIsUpdate) {
            //// TODO: 1/7/17 change to single valueeventlistenenr ?? 
            getDB().child("meals").child(mMealKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Meal meal = Meal.parseSnapshot(dataSnapshot);

                    mUserKey = meal.getUserKey();
                    mLocationName.setText(meal.getLocation().toString());
                    mTitle.setText(meal.getTitle());
                    mDescription.setText(meal.getDescription());
                    initNotFreeDays();
                    for (Day d : meal.getFreeDays()) {
                        mDaysNotFree.remove(d);
                        mDaysFree.add(d);
                    }
                    updateFreeDaysLayout();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load meal.", Toast.LENGTH_LONG).show();
                    ((HomeActivity) mParent).loadDefaultScreen();
                }
            });
        }
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
        for (Day d : mDaysFree) {
            View dayWrapper = mParent.getLayoutInflater().inflate(R.layout.item_day, mListOfDays, false);
            dayWrapper.setTag(R.id.TAG_DAY, d);
            ((TextView) dayWrapper.findViewById(R.id.day_name)).setText(d.getName());
            ((CheckBox) dayWrapper.findViewById(R.id.day_lunch_cb)).setChecked(d.isLunch());
            ((CheckBox) dayWrapper.findViewById(R.id.day_dinner_cb)).setChecked(d.isDinner());
            dayWrapper.findViewById(R.id.day_dinner_cb).setOnClickListener(this);
            dayWrapper.findViewById(R.id.day_lunch_cb).setOnClickListener(this);
            dayWrapper.findViewById(R.id.day_remove_btn).setOnClickListener(this);
            mListOfDays.addView(dayWrapper);
        }
    }

    private void addDay() {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("Add a new day");
        b.setItems(getNames(mDaysNotFree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Day newDay = mDaysNotFree.get(which);
                mDaysNotFree.remove(which);
                //add the new day and sort the list
                mDaysFree.add(newDay);
                Collections.sort(mDaysFree);

                //re-create the list of free days
                updateFreeDaysLayout();
            }

        });
        b.show();
    }

    private void removeDay(View view) {
        //remove dayWrapper from list
        View parent = (View) (view.getParent()).getParent();
        Day day = (Day) parent.getTag(R.id.TAG_DAY);
        mListOfDays.removeView(parent);
        mDaysFree.remove(day);
        mDaysNotFree.add(day);
        Collections.sort(mDaysNotFree);
    }

    private void toggleMealTime(View view) {
        int id = view.getId();
        CheckBox cb = (CheckBox) view;
        LinearLayout dayWrapper = (LinearLayout) ((view.getParent()).getParent());
        Day d = (Day) dayWrapper.getTag(R.id.TAG_DAY);
        if (id == R.id.day_lunch_cb) {
            d.setLunch(cb.isChecked());
        } else if (id == R.id.day_dinner_cb) {
            d.setDinner(cb.isChecked());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_new_day_btn:
                addDay();
                break;
            case R.id.day_remove_btn:
                removeDay(v);
                break;
            case R.id.day_lunch_cb:
            case R.id.day_dinner_cb:
                toggleMealTime(v);
                break;
        }
    }


    @Override
    public void setmButton(View v) {
        mEnterButton = (Button) v.findViewById(R.id.enter_location_btn);
        mEnterButton.setVisibility(View.GONE);
    }

    @Override
    public void setmLocationName(View v) {
        mLocationName = (EditText) v.findViewById(R.id.create_location);
    }
}
