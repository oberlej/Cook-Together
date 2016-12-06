package com.cooktogether.model;

import android.location.Location;

/**
 * Created by jeremiaoberle on 12/5/16.
 */

public class Meal {
    private String title;
    private String description;
    private String userId;
    private String mealId;
    private Location location;
    public Meal() {

    }

    public Meal(String title, String description, String userId, String mealId, Location location) {
        this.title = title;
        this.description = description;
        this.userId=userId;
        this.mealId=mealId;
        this.location = location;
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
        if(location != null){
            this.location = location;
        }
    }
}
