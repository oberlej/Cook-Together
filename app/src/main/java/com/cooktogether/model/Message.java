package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by hela on 06/01/17.
 */

public class Message {
    private String senderId;
    private String content;

    public Message(String senderId, String content) {
        this.senderId = senderId;
        this.content = content;
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

    public static Message parseSnapshot(DataSnapshot snapshot) {
        return new Message((String)snapshot.child("senderId").getValue(), (String)snapshot.child("content").getValue());
    }
}
