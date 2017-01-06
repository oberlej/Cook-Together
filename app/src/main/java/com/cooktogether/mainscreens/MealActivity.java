package com.cooktogether.mainscreens;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.helpers.AbstractBaseActivity;
import com.cooktogether.R;
import com.cooktogether.helpers.ConversationActivity;
import com.cooktogether.listener.RecyclerItemClickListener;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.cooktogether.adapter.locationOptionsAdapter;

public class MealActivity extends AbstractBaseActivity {
    private LinearLayout mListOfDays;
    private EditText mTitle;
    private EditText mDescription;
    private List<Day> mDaysNotFree;
    private List<Day> mDaysFree;

    private String mMealKey = null;
    private String mUserKey;
    private boolean mIsUpdate = false;

    private boolean mAnswer;
    private EditText mLocationName;

    // for the list of location options
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private UserLocation selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsConnected();

        setContentView(R.layout.activity_meal);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));

        //for the list of location options
        mRecyclerView = (RecyclerView) findViewById(R.id.location_options);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        selectedLocation = ((locationOptionsAdapter)mAdapter).getSelectedLocation(position);
                        mLocationName.setText(selectedLocation.toString());
                        ((locationOptionsAdapter)mAdapter).clear();
                        mRecyclerView.clearFocus();
                    }
                })
        );



        initFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveMeal();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_cancel:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case DialogInterface.BUTTON_POSITIVE:
                                mAnswer = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                mAnswer = false;
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Discard meal ?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                if (mAnswer) {
                    Toast.makeText(getApplicationContext(), "Meal " + mTitle.getText().toString() + " discarded.", Toast.LENGTH_LONG).show();
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveMeal() {
        if (!mIsUpdate) {
            mMealKey = getDB().child("meals").push().getKey();
        }

        if (selectedLocation == null) {
            Toast.makeText(getApplicationContext(), "Unvalid location or no service available", Toast.LENGTH_LONG).show();
            //todo Alert box to change the location
        }

        Meal m = new Meal(mTitle.getText().toString(), mDescription.getText().toString(), getUid(), mMealKey, mDaysFree, selectedLocation);

        getDB().child("meals").child(mMealKey).setValue(m);

        if (!mIsUpdate) {
            Toast.makeText(getApplicationContext(), "Meal " + mTitle.getText().toString() + " created.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Meal " + mTitle.getText().toString() + " updated.", Toast.LENGTH_LONG).show();

        }
        return true;
    }

    private void initFields() {
        mListOfDays = (LinearLayout) findViewById(R.id.create_list_of_days);
        mTitle = (EditText) findViewById(R.id.create_title);
        mDescription = (EditText) findViewById(R.id.create_description);
        mLocationName = (EditText) findViewById(R.id.create_location);

        mLocationName.addTextChangedListener(new TextWatcher() {
            ArrayList<UserLocation> locations = new ArrayList<UserLocation>();
            public void afterTextChanged(Editable s) {

                if(mLocationName.isFocused()) {
                    locations = getLocation(mLocationName.getText().toString());
                    if(locations.isEmpty()) {
                        mLocationName.setError("Location is not found, please try again");
                    }else {
                        mAdapter = new locationOptionsAdapter();
                        mRecyclerView.setAdapter(mAdapter);
                        ((locationOptionsAdapter) mAdapter).setMOptions(locations);
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        initNotFreeDays();
        mDaysFree = new ArrayList<Day>();

        Intent intent = getIntent();
        if (intent.hasExtra(getResources().getString(R.string.MEAL_KEY))) {
            mMealKey = intent.getStringExtra(getResources().getString(R.string.MEAL_KEY));
            mIsUpdate = mMealKey != null && !mMealKey.isEmpty();
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
                    Toast.makeText(getApplicationContext(), "Failed to load meal.", Toast.LENGTH_SHORT).show();
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

    /**
     * onClick handler for the add a freee day button.
     * Will open alertdialog and propose all days that are not aded to the freeDays list.
     *
     * @param view
     */
    public void addDay(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
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

    private void updateFreeDaysLayout() {
        mListOfDays.removeAllViews();
        for (Day d : mDaysFree) {
            View dayWrapper = getLayoutInflater().inflate(R.layout.item_day, mListOfDays, false);
            dayWrapper.setTag(R.id.TAG_DAY, d);
            ((TextView) dayWrapper.findViewById(R.id.day_name)).setText(d.getName());
            ((CheckBox) dayWrapper.findViewById(R.id.day_lunch_cb)).setChecked(d.isLunch());
            ((CheckBox) dayWrapper.findViewById(R.id.day_dinner_cb)).setChecked(d.isDinner());
            mListOfDays.addView(dayWrapper);
        }
    }

    public void removeDay(View view) {
        //remove dayWrapper from list
        View parent = (View) ((View) view.getParent()).getParent();
        Day day = (Day) parent.getTag(R.id.TAG_DAY);
        mListOfDays.removeView(parent);
        mDaysFree.remove(day);
        mDaysNotFree.add(day);
        Collections.sort(mDaysNotFree);
    }

    public void toggleMealTime(View view) {
        int id = view.getId();
        CheckBox cb = (CheckBox) view;
        LinearLayout dayWrapper = (LinearLayout) (((View) view.getParent()).getParent());
        Day d = (Day) dayWrapper.getTag(R.id.TAG_DAY);
        if (id == R.id.day_lunch_cb) {
            d.setLunch(cb.isChecked());
        } else if (id == R.id.day_dinner_cb) {
            d.setDinner(cb.isChecked());
        }
    }

    /*
    For now it returns only the first address found to make it simple
     */
    private ArrayList<UserLocation> getLocation(String locationName) {
        Geocoder geo = new Geocoder(this, Locale.getDefault());

        ArrayList<UserLocation> locations = new ArrayList<UserLocation>();

        try {
            List<Address> addresses = geo.getFromLocationName(locationName, 5);
            if (addresses.size() > 0) {
                for(Address address : addresses) {
                    UserLocation location = new UserLocation();
                    location.setLatitude(address.getLatitude());
                    location.setLongitude(address.getLongitude());
                    location.setAddress(address);
                    locations.add(location);
                }
            }
        } catch (IOException e) {
            Log.e("getLocation", e.getMessage());
        }
        return locations;
    }

    public void contact(View view){
        String conversationKey = getDB().child("conversations").push().getKey();

        List<String> usersKeys = new ArrayList<String>();
        usersKeys.add(getUid());
        usersKeys.add(mUserKey);
        Conversation newConv = new Conversation(mTitle.getText().toString(), conversationKey, usersKeys);

        getDB().child("conversations").child(conversationKey).setValue(newConv);

        Button send = (Button)findViewById(R.id.send_button);
        send.setText("Connected ");
        Intent intent = new Intent(this, ConversationActivity.class);
        Bundle extras = new Bundle();
        extras.putString("key", conversationKey);
        extras.putString("title", newConv.getTitle());
        intent.putExtras(extras);
        //intent.putExtra(getResources().getString(R.string.CONVERSATION_KEY), conversationKey);
        //intent.putExtra(getResources().getString(R.string.CONVERSATION_TITLE), newConv.getTitle());
        startActivity(intent);
    }
}
