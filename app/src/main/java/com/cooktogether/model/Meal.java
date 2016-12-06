package com.cooktogether.model;

/**
 * Created by jeremiaoberle on 12/5/16.
 */

public class Meal {
    private String title;
    private String description;
    private String userId;
    private String mealId;

    public Meal() {

    }

    public Meal(String title, String description, String userId, String mealId) {
        this.title = title;
        this.description = description;
        this.userId=userId;
        this.mealId=mealId;
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

}
