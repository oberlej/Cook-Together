package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hela on 06/01/17.
 */

public class Conversation {
    private String Title;
    private ArrayList<Message> messages;
    private String conversationKey;
    private List<String> usersKeys; //to change later to users
    //private String userKey2; //to change later to users

    public Conversation(String title, String conversationKey, List<String> usersKeys) {
        Title = title;
        this.messages = new ArrayList<Message>();
        this.conversationKey = conversationKey;
        this.usersKeys = usersKeys;
    }

    public Conversation(String title, ArrayList<Message> messages, String conversationKey, List<String> usersKeys) {
        Title = title;
        this.messages = messages;
        this.conversationKey = conversationKey;
        this.usersKeys = usersKeys;
    }

    public List<String> getUsersKeys() {
        return this.usersKeys;
    }

    public void setUsersKeys(List usersKeys) {
        this.usersKeys = usersKeys;
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
        List<String> usersKeys = new ArrayList<>();
        for(DataSnapshot userKey : snapshot.child("usersKeys").getChildren()){
            usersKeys.add((String)userKey.getValue());
        }

        return new Conversation((String)snapshot.child("title").getValue(), messages,(String) snapshot.child("conversationKey").getValue(),usersKeys );
    }
}
