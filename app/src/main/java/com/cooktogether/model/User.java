package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeremiaoberle on 1/6/17.
 */

public class User {
    private String userKey;
    private String userName;
    private String email;
    private String birthDate;
    private String description;
    private boolean facebookPicture;
    private boolean facebookConnected;
    private List<Review> reviews;
    private List<String> reservations;

    public User() {
    }

    public User(String userKey, String userName, String email, String birthDate, String description,
                boolean facebookPicture, boolean facebookConnected, ArrayList<Review> reviews) {
        this.userKey = userKey;
        this.userName = userName;
        this.email = email;
        this.birthDate = birthDate;
        this.description = description;
        this.facebookPicture = facebookPicture;
        this.facebookConnected = facebookConnected;
        this.reviews = reviews;
        this.reservations = new ArrayList<String>();

    }

    public User(String userKey, String userName, String email, String birthDate, String description,
                boolean facebookPicture, boolean facebookConnected, List<Review> reviews,
                List<String> reservations) {
        this.userKey = userKey;
        this.userName = userName;
        this.email = email;
        this.birthDate = birthDate;
        this.description = description;
        this.facebookPicture = facebookPicture;
        this.facebookConnected = facebookConnected;
        this.reviews = reviews;
        this.reservations = reservations;
    }

    public User(String userKey, String userName) {
        this.userKey = userKey;
        this.userName = userName;
    }

    public static User parseSnapshot(DataSnapshot snapshot) {

        List<String> reservations = new ArrayList<String>();
        if(snapshot.hasChild("reservations")) {
            for (DataSnapshot reservation : snapshot.child("reservations").getChildren()) {
                reservations.add(reservation.getKey());
            }
        }
        return new User((String) snapshot.child("userKey").getValue(),
                (String) snapshot.child("userName").getValue(), (String) snapshot.child("email").getValue(),
                (String) snapshot.child("birthDate").getValue(), (String) snapshot.child("description").getValue(),
                (boolean) snapshot.child("facebookPicture").getValue(), (boolean) snapshot.child("facebookConnected").getValue(),
                (ArrayList<Review>) snapshot.child("reviews").getValue(),reservations);
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isFacebookConnected() {
        return facebookConnected;
    }

    public void setFacebookConnected(boolean facebookConnected) {
        this.facebookConnected = facebookConnected;
    }

    public boolean isFacebookPicture() {
        return facebookPicture;
    }

    public void setFacebookPicture(boolean facebookPicture) {
        this.facebookPicture = facebookPicture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    public List<String> getReservations() {
        return reservations;
    }

    public void setReservations(List<String> reservations) {
        this.reservations = reservations;
    }
}
