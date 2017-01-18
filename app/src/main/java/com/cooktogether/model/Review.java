package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by jeremiaoberle on 1/16/17.
 */

public class Review {
    private String fromUser;
    private String text;
    private double mark;

    public Review(){
    }

    public Review(String fromUser, String text, double mark) {
        this.fromUser = fromUser;
        this.text = text;
        this.mark = mark;
    }

    public static Review parseSnapshot(DataSnapshot snapshot) {
        return new Review((String) snapshot.child("fromUser").getValue(), (String) snapshot.child("text").getValue(), (Double) snapshot.child("mark").getValue());
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }
}
