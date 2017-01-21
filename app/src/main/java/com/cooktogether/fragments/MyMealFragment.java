package com.cooktogether.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cooktogether.R;
import com.cooktogether.helpers.AbstractLocationFragment;
import com.cooktogether.helpers.UploadPicture;
import com.cooktogether.mainscreens.HomeActivity;
import com.cooktogether.model.Conversation;
import com.cooktogether.model.Day;
import com.cooktogether.model.DayEnum;
import com.cooktogether.model.Meal;
import com.cooktogether.model.Reservation;
import com.cooktogether.model.StatusEnum;
import com.cooktogether.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

    //reservation
    private int mNbrPersons;
    private int mNbrReservations;
    private CheckBox mBooked;
    private List<Reservation> mReservations;
    private HashMap<Reservation, User> mRsv_demands;
    private HashMap<Reservation, User> mRsv_accepted;
    private ProgressBar progressBar;

    // for the list of rsv demands
    private LinearLayout rsv_demands;
    private TextView mNbrRsvView;
    private EditText mNbrPersonsView;
    //for the list of rsv_accepted
    private LinearLayout rsv_accepted;

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
        }

        if (!mIsUpdate) {
            mMealKey = getDB().child("meals").push().getKey();
            mBooked.setChecked(false);
            mNbrReservations = 0;
        }

        Meal m = new Meal(mTitle.getText().toString(), mDescription.getText().toString(),
                mParent.getUid(), mMealKey, mDaysFree, getSelectedLocation(), mNbrPersons, mNbrReservations, mBooked.isChecked());

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
        //for the reservation part
        mNbrRsvView = (TextView) view.findViewById(R.id.nbr_reservations);
        mNbrPersonsView = (EditText) view.findViewById(R.id.set_nbr_persons);

        mBooked = (CheckBox) view.findViewById(R.id.set_is_booked);
        rsv_demands = (LinearLayout) view.findViewById(R.id.rsv_demands);
        rsv_accepted = (LinearLayout) view.findViewById(R.id.rsv_accepted);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);


        mReservations = new ArrayList<>();
        mRsv_demands = new HashMap<Reservation, User>();
        mRsv_accepted = new HashMap<Reservation, User>();

        initNotFreeDays();
        mDaysFree = new ArrayList<Day>();

        mMealKey = ((HomeActivity) mParent).getMealKey();
        mIsUpdate = mMealKey != null && !mMealKey.isEmpty();
        if (mIsUpdate) {
            loadMeal();
        }
    }

    private void updateRsvDemands() {
        rsv_demands.removeAllViews();
        if (mRsv_demands.isEmpty())
            rsv_demands.getRootView().findViewById(R.id.rsv_demands_text).setVisibility(View.GONE);
        else
            rsv_demands.getRootView().findViewById(R.id.rsv_demands_text).setVisibility(View.VISIBLE);
        for (Reservation rsv_d : mRsv_demands.keySet()) {
            View rsv_demandWrapper = mParent.getLayoutInflater().inflate(R.layout.item_rsv_demand, rsv_demands, false);
            rsv_demandWrapper.setTag(R.id.TAG_RSV_DEMAND, rsv_d);

            CircleImageView userPic = (CircleImageView) rsv_demandWrapper.findViewById(R.id.profile_pic);
            new UploadPicture(getContext(), mRsv_demands.get(rsv_d), userPic, null , getRootRef(), getDB()).loadPicture();

            ((TextView) rsv_demandWrapper.findViewById(R.id.user_name)).setText(mRsv_demands.get(rsv_d).getUserName());
            ((CheckBox) rsv_demandWrapper.findViewById(R.id.accept_rsv_demand)).setChecked(false);
            rsv_demandWrapper.findViewById(R.id.accept_rsv_demand).setOnClickListener(this);
            rsv_demandWrapper.findViewById(R.id.refuse_rsv_demand).setOnClickListener(this);
            rsv_demands.addView(rsv_demandWrapper);
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

                    mNbrPersons = meal.getNbrPersons();
                    progressBar.setMax(mNbrPersons);
                    mNbrPersonsView.setText(String.valueOf(mNbrPersons));

                    mNbrReservations = meal.getNbrReservations();
                    progressBar.setProgress(mNbrReservations);
                    mNbrRsvView.setText(mNbrReservations + "/" + mNbrPersons + "reservations");

                    mBooked.setChecked(meal.getBooked());

                    getDB().child("reservations").orderByChild("mealKey").equalTo(mMealKey).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            final Reservation rsv = Reservation.parseSnapshot(dataSnapshot);
                            if (!mReservations.contains(rsv))
                                mReservations.add(rsv);
                            if (StatusEnum.valueOf(rsv.getStatus().toUpperCase()).equals(StatusEnum.WAITING) || StatusEnum.valueOf(rsv.getStatus().toUpperCase()).equals(StatusEnum.ACCEPTED)) {

                                getDB().child("users").child(rsv.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = User.parseSnapshot(dataSnapshot);
                                        if(StatusEnum.valueOf(rsv.getStatus().toUpperCase()).equals(StatusEnum.WAITING)) {
                                            mRsv_demands.put(rsv, user);
                                            updateRsvDemands();
                                        }
                                        else{
                                            mRsv_accepted.put(rsv, user);
                                            updateRsvAccepted(rsv);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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

                    initNotFreeDays();
                    for (Day d : meal.getFreeDays()) {
                        mDaysNotFree.remove(d);
                        mDaysFree.add(d);
                    }
                    updateFreeDaysLayout();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load meal.", Toast.LENGTH_LONG).show();
                        ((HomeActivity) mParent).loadDefaultScreen();
                    }
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
            case R.id.accept_rsv_demand:
                removeRsvDemand(v);
                break;
            case R.id.refuse_rsv_demand:
                removeRsvDemand(v);
                break;
            case R.id.contact_user_btn:
                contact(v);
                break;
        }
    }

    private void contact(View v) {
        View parent = (View) (v.getParent()).getParent();
        Reservation rsv = (Reservation) parent.getTag(R.id.TAG_RSV_ACCEPTED);
        Query conversation = mParent.getDB().child("user-conversations").child(mParent.getUid()).child(rsv.getMealKey());//mParent.getDB().child("users").child(mParent.getUid()).orderByChild(mealKey).equals(true); wont work until all the users are added to the users table
        String conversationKey = rsv.getMealKey(); // here we consider that the conversation key is the meal key

        List<String> usersKeys = new ArrayList<String>();
        usersKeys.add(mParent.getUid());
        usersKeys.add(rsv.getUserKey());
        Conversation newConv = new Conversation(mTitle.getText().toString(), conversationKey, usersKeys);

        HashMap<String, Object> convMap = newConv.toHashMap();
        convMap.remove("messages"); //to not delete previous messages if any
        conversation.getRef().updateChildren(convMap);

        ((HomeActivity)mParent).goToConversation(conversationKey);
    }

    private void removeRsvDemand(View v) {
        View parent = (View) (v.getParent()).getParent();
        Reservation rsv = (Reservation) parent.getTag(R.id.TAG_RSV_DEMAND);
        rsv_demands.removeView(parent);
        User user = mRsv_demands.get(rsv);
        mRsv_demands.remove(rsv);

        //Update the database
        CheckBox accept = (CheckBox) parent.findViewById(R.id.accept_rsv_demand);

        DatabaseReference meal = getDB().child("meals").child(rsv.getMealKey());

        if (accept.isChecked()) {
            getDB().child("reservations").child(rsv.getReservationKey()).child("status").setValue(StatusEnum.ACCEPTED.getStatus());
            mRsv_accepted.put(rsv, user);
            updateRsvAccepted(rsv);
        } else {
            getDB().child("reservations").child(rsv.getReservationKey()).child("status").setValue(StatusEnum.REFUSED.getStatus());
            mNbrReservations--;
            meal.child("nbrReservations").setValue(mNbrReservations);
        }
        if (mNbrReservations == mNbrPersons)
            meal.child("booked").setValue(true);
        else
            meal.child("booked").setValue(false);
    }

    private void updateRsvAccepted(Reservation rsv) {
        rsv_accepted.getRootView().findViewById(R.id.rsv_accepted_text).setVisibility(View.VISIBLE);
        View rsvWrapper = mParent.getLayoutInflater().inflate(R.layout.item_rsv_accepted, rsv_accepted, false);
        rsvWrapper.setTag(R.id.TAG_RSV_ACCEPTED, rsv);
        ((TextView) rsvWrapper.findViewById(R.id.user_name)).setText(mRsv_accepted.get(rsv).getUserName());

        CircleImageView userPic = (CircleImageView) rsvWrapper.findViewById(R.id.profile_pic);
        new UploadPicture(getContext(), mRsv_accepted.get(rsv), userPic, null , getRootRef(), getDB()).loadPicture();

        rsvWrapper.findViewById(R.id.contact_user_btn).setOnClickListener(this);
        rsv_accepted.addView(rsvWrapper);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((HomeActivity) mParent).mMealKey = null;
    }
}
