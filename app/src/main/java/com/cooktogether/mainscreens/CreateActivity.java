package com.cooktogether.mainscreens;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cooktogether.R;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class CreateActivity extends AppCompatActivity {
    private LinearLayout mListOfDays;
    private Button mBtnNewDay;
    private Spinner mPopupSpinner;

    public void removeDay(View view) {
        //remove dayWrapper from list
        View parent = (View)((View)view.getParent()).getParent();
        int childIndex = 0;
        for(int i = 0;i<mListOfDays.getChildCount();i++){
            if(mListOfDays.getChildAt(i).equals(parent)){
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


    private enum Day {
        MONDAY("Monday"), TUESDAY("Tuesday"), WEDNESDAY("Wednesday"), THURSDAY("Thursday"), FRIDAY("Friday"), SATURDAY("Saturday"), SUNDAY("Sunday");

        public boolean added = false;
        public String name;

        Day(String name) {
            this.name = name;
        }
    }

    private List<String> days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        initFields();
    }

    private void initFields() {
        mBtnNewDay = (Button) findViewById(R.id.create_new_day_btn);
        mListOfDays = (LinearLayout) findViewById(R.id.create_list_of_days);

        days = new ArrayList<String>();
        for (Day d : Day.values()) {
            days.add(d.name);
        }
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
}
