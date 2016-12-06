package com.cooktogether.mainscreens;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.BaseActivity;
import com.cooktogether.R;
import com.cooktogether.model.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

public class CreateActivity extends BaseActivity {
    private LinearLayout mListOfDays;
    private EditText mTitle;
    private EditText mDescription;
    private EditText mLocationName;
    private Button mBtnNewDay;
    private Spinner mPopupSpinner;
    private List<String> days;

    private DatabaseReference mDatabase;


    private enum Day {
        MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY("Thursday"), FRIDAY("Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");

        public boolean added = false;
        public String name;

        Day(String name) {
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));

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
                String mealId = mDatabase.child("meals").push().getKey();
                String locationName = mLocationName.getText().toString();
                Location location = getLocation(locationName);
                if(location == null){
                    //todo Alert box to change the location
                }
                Meal m = new Meal(mTitle.getText().toString(),mDescription.getText().toString(),getUid(),mealId, location);
                mDatabase.child("meals").child(mealId).setValue(m);
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_cancel:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFields() {
        mBtnNewDay = (Button) findViewById(R.id.create_new_day_btn);
        mListOfDays = (LinearLayout) findViewById(R.id.create_list_of_days);
        mTitle = (EditText) findViewById(R.id.create_title);
        mDescription = (EditText) findViewById(R.id.create_description);
        mLocationName = (EditText) findViewById(R.id.create_location);

        days = new ArrayList<String>();
        for (Day d : Day.values()) {
            days.add(d.name);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void addDay(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Add a new day");
        b.setItems(days.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                View dayWrapper = getLayoutInflater().inflate(R.layout.day_layout, mListOfDays, false);

                int origIndex = Day.valueOf(days.get(which).toUpperCase()).ordinal();
                dayWrapper.setTag(R.id.TAG_ORIG_INDEX, origIndex);
                dayWrapper.findViewById(R.id.day_remove_btn).setTag(R.id.TAG_ORIG_INDEX, origIndex);
                ((TextView) dayWrapper.findViewById(R.id.day_name)).setText(days.get(which));

                int index = 0;
                int count = mListOfDays.getChildCount();
                View v = null;
                for (index = 0; index < count; index++) {
                    v = mListOfDays.getChildAt(index);
                    if ((int) v.getTag(R.id.TAG_ORIG_INDEX) > origIndex) {
                        break;
                    }
                }
                mListOfDays.addView(dayWrapper, index);
                days.remove(which);
            }

        });
        b.show();
    }

    public void removeDay(View view) {
        //remove dayWrapper from list
        View parent = (View) ((View) view.getParent()).getParent();
        int childIndex = 0;
        for (int i = 0; i < mListOfDays.getChildCount(); i++) {
            if (mListOfDays.getChildAt(i).equals(parent)) {
                childIndex = i;
            }
        }
        mListOfDays.removeViewAt(childIndex);

        //add to dialog list
        int origIndex = (int) view.getTag(R.id.TAG_ORIG_INDEX);
        int index = 0;
        for (String s : days) {
            if (Day.valueOf(s.toUpperCase()).ordinal() > origIndex) {
                break;
            }
            index++;
        }
        Day day = Day.values()[origIndex];
        days.add(index, day.name);
    }

    /*
    For now it returns only the first address found to make it simple
     */
    private Location getLocation(String locationName){
        Geocoder geo = new Geocoder(this, Locale.getDefault());
        Location location = new Location("");
        try {
            List<Address> addresses =  geo.getFromLocationName(locationName, 1);
            if(addresses.size() > 0){
                Address address = addresses.get(0);
                location.setLatitude(address.getLatitude());
                location.setLongitude(address.getLongitude());
                return location;
            }
        }catch (IOException e){
            Log.e("getLocation", e.getMessage());
        }
        return null;
    }
}
