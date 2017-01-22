package com.cooktogether.model;

import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;


/**
 * Created by hela on 06/01/17.
 */

public class Message {
    private String senderId;
    private String content;
    private Date date;

    public Message() {
    }

    public Message(String senderId, String content, Date date) {
        this.senderId = senderId;
        this.content = content;
        this.date = date;
    }

    public Message(String senderId, String content, java.util.Date date) {
        this.senderId = senderId;
        this.content = content;
        this.date = new Date(date);
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public static Message parseSnapshot(DataSnapshot snapshot) {
        DataSnapshot d = snapshot.child("date");

        int year = ((Long) d.child("year").getValue()).intValue();
        int month = ((Long) d.child("month").getValue()).intValue();
        int day = ((Long) d.child("day").getValue()).intValue();
        int hrs = ((Long) d.child("hrs").getValue()).intValue();
        int min = ((Long) d.child("min").getValue()).intValue();

        com.cooktogether.model.Date date = new com.cooktogether.model.Date(year, month, day, hrs, min);

        return new Message((String) snapshot.child("senderId").getValue(), (String) snapshot.child("content").getValue(), date);
    }
}
