package com.cooktogether.model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hela on 06/01/17.
 */

public class Conversation {
    private String title;
    private ArrayList<Message> messages;
    private String conversationKey;
    private String mealKey;
    private List<String> usersKeys;

    public Conversation(String title, String conversationKey, String mealKey, List<String> usersKeys) {
        this.title = title;
        this.messages = new ArrayList<Message>();
        this.conversationKey = conversationKey;
        this.mealKey = mealKey;
        this.usersKeys = usersKeys;
    }

    public Conversation(String title, ArrayList<Message> messages, String conversationKey, String mealKey, List<String> usersKeys) {
        this.title = title;
        this.messages = messages;
        this.conversationKey = conversationKey;
        this.mealKey = mealKey;
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

    public String getTitle() {
        return title;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }


    public String getConversationKey() {
        return this.conversationKey;
    }

    public void setConversationKey(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    public String getMealKey() {
        return mealKey;
    }

    public void setMealKey(String mealKey) {
        this.mealKey = mealKey;
    }

    public static Conversation parseSnapshot(DataSnapshot snapshot) {
        ArrayList<Message> messages = new ArrayList<Message>();
        if (snapshot.hasChild("messages")) {
            for (DataSnapshot messageSnap : snapshot.child("messages").getChildren()) {
                Message message = Message.parseSnapshot(messageSnap);
                messages.add(message);
            }
        }
        List<String> usersKeys = new ArrayList<>();
        for (DataSnapshot userKey : snapshot.child("usersKeys").getChildren()) {
            usersKeys.add((String) userKey.getValue());
        }

        return new Conversation((String) snapshot.child("title").getValue(), messages, (String) snapshot.child("conversationKey").getValue(), (String) snapshot.child("mealKey").getValue(), usersKeys);
    }

    /*
    Convert the conversation model into a HashMap
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put("title", title);
        conversation.put("messages", messages);
        conversation.put("conversationKey", conversationKey);
        conversation.put("mealKey", mealKey);
        conversation.put("usersKeys", usersKeys);
        return conversation;
    }
}
