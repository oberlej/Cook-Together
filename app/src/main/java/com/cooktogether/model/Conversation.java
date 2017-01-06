package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by hela on 06/01/17.
 */

public class Conversation {
    private String Title;
    private ArrayList<Message> messages;
    private String conversationKey;
    private String userKey; //to change later to users

    public Conversation(String title, String conversationKey, String userKey) {
        Title = title;
        this.messages = new ArrayList<Message>();
        this.conversationKey = conversationKey;
        this.userKey = userKey;
    }

    public Conversation(String title, ArrayList<Message> messages, String conversationKey, String userKey) {
        Title = title;
        this.messages = messages;
        this.conversationKey = conversationKey;
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message){
        this.messages.add(message);
    }

    public String getTitle() {
        return Title;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }


    public String getLastMessage() {
        if(this.messages.isEmpty())
            return "no messages";
        return this.messages.get(this.messages.size()-1).getContent();
    }

    public String getConversationKey() {
        return conversationKey;
    }

    public void setConversationKey(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    public static Conversation parseSnapshot(DataSnapshot snapshot) {
        ArrayList<Message> messages = new ArrayList<Message>();
        if (snapshot.hasChild("messages")) {
            for(DataSnapshot messageSnap : snapshot.child("messages").getChildren()){
                Message message = Message.parseSnapshot(messageSnap);
                messages.add(message);
            }
        }
        return new Conversation((String)snapshot.child("title").getValue(), messages,(String) snapshot.child("conversationKey").getValue(), (String) snapshot.child("userKey").getValue());
    }
}
