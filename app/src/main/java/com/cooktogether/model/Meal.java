package com.cooktogether.model;

import android.location.Location;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeremiaoberle on 12/5/16.
 */

public class Meal {
    private String title;
    private String description;
    private String userKey;
    private String mealKey;
    private Location location;


    private List<Day> freeDays;

    public Meal() {

    }

    public Meal(String title, String description, String userKey, String mealKey, List<Day> freeDays, Location location) {
        this.title = title;
        this.description = description;
        this.userKey = userKey;
        this.mealKey = mealKey;
        this.freeDays = freeDays;
        this.location = location;
    }

    public List<Day> getFreeDays() {
        return freeDays;
    }

    public void setFreeDays(List<Day> freeDays) {
        this.freeDays = freeDays;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getMealKey() {
        return mealKey;
    }

    public void setMealKey(String mealKey) {
        this.mealKey = mealKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.location = location;
        }
    }

    public static Meal parseSnapshot(DataSnapshot snapshot) {
        List<Day> freeDays = new ArrayList<Day>();
        for(DataSnapshot day : snapshot.child("freeDays").getChildren()){
            freeDays.add(new Day((String)day.child("name").getValue(),(Boolean)day.child("lunch").getValue(),(Boolean)day.child("dinner").getValue()));
        }
        return new Meal((String) snapshot.child("title").getValue(), (String) snapshot.child("description").getValue(), (String) snapshot.child("userKey").getValue(), (String) snapshot.child("mealKey").getValue(), freeDays,null);
    }
}
