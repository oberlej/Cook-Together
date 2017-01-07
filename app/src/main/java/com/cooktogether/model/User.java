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
    private boolean facebookImage;
    private String imageURI;
    private boolean facebookConnected;

    public User() {
    }

    public User(String userKey, String userName, String email, String birthDate, boolean facebookImage, String imageURI, boolean facebookConnected) {
        this.userKey = userKey;
        this.userName = userName;
        this.email = email;
        this.birthDate = birthDate;
        this.facebookImage = facebookImage;
        this.imageURI = imageURI;
        this.facebookConnected = facebookConnected;
    }

    public static User parseSnapshot(DataSnapshot snapshot) {
        return new User((String) snapshot.child("userKey").getValue(), (String) snapshot.child("userName").getValue(), (String) snapshot.child("email").getValue(), (String) snapshot.child("birthDate").getValue(), (boolean) snapshot.child("facebookImage").getValue(), (String) snapshot.child("imageURI").getValue(), (boolean) snapshot.child("facebookConnected").getValue());
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

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public boolean isFacebookConnected() {
        return facebookConnected;
    }

    public void setFacebookConnected(boolean facebookConnected) {
        this.facebookConnected = facebookConnected;
    }

    public boolean isFacebookImage() {
        return facebookImage;
    }

    public void setFacebookImage(boolean facebookImage) {
        this.facebookImage = facebookImage;
    }
}
