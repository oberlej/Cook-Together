package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by hela on 18/01/17.
 */

public class Reservation {
    private String reservationKey;
    private String userKey;
    private String mealKey;
    private String status; // waiting , accepted,  refused

    public Reservation(String reservationKey, String userKey, String mealKey, StatusEnum status) {
        this.reservationKey = reservationKey;
        this.userKey = userKey;
        this.mealKey = mealKey;
        this.status = status.getStatus();
    }

    public String getReservationKey() {
        return reservationKey;
    }

    public void setReservationKey(String reservationKey) {
        this.reservationKey = reservationKey;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status.getStatus();
    }

    public static Reservation parseSnapshot(DataSnapshot snapshot) {
        String reservationKey = (String) snapshot.child("reservationKey").getValue();
        String userKey = (String) snapshot.child("userKey").getValue();
        String mealKey = (String) snapshot.child("mealKey").getValue();
        StatusEnum status = StatusEnum.valueOf(((String) snapshot.child("status").getValue()));

        return new Reservation(reservationKey, userKey, mealKey, status);
    }
}
