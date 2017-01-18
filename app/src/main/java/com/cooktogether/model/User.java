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
    private boolean facebookPicture;
    private String pictureURI;
    private boolean facebookConnected;


    public User() {
    }

    public User(String userKey, String userName, String email, String birthDate, boolean facebookPicture, String pictureURI, boolean facebookConnected) {
        this.userKey = userKey;
        this.userName = userName;
        this.email = email;
        this.birthDate = birthDate;
        this.facebookPicture = facebookPicture;
        this.pictureURI = pictureURI;
        this.facebookConnected = facebookConnected;
    }

    public static User parseSnapshot(DataSnapshot snapshot) {
        return new User((String) snapshot.child("userKey").getValue(), (String) snapshot.child("userName").getValue(), (String) snapshot.child("email").getValue(), (String) snapshot.child("birthDate").getValue(), (boolean) snapshot.child("facebookPicture").getValue(), (String) snapshot.child("pictureURI").getValue(), (boolean) snapshot.child("facebookConnected").getValue());
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

    public String getPictureURI() {
        return pictureURI;
    }

    public void setPictureURI(String pictureURI) {
        this.pictureURI = pictureURI;
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
}
