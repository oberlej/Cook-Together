package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeremiaoberle on 1/6/17.
 */

public class User {
    private String userName;
    private String email;
    private String birthDate;

    public User() {
    }

    public User(String userName,String email,String birthDate) {
        this.birthDate = birthDate;
        this.userName = userName;
    }

    public static User parseSnapshot(DataSnapshot snapshot) {
        return new User((String) snapshot.child("userName").getValue(), (String) snapshot.child("email").getValue(),(String) snapshot.child("birthDate").getValue());
    }
}
