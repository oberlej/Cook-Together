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
    private List<String> usersKeys;
    private int unread;
    private long rank;


    public Conversation(String title, String conversationKey, List<String> usersKeys, int unread, long rank) {
        this.title = title;
        this.messages = new ArrayList<Message>();
        this.conversationKey = conversationKey;
        this.usersKeys = usersKeys;
        this.unread = unread;
        this.rank = rank;
    }

    public Conversation(String title, ArrayList<Message> messages, String conversationKey, List<String> usersKeys, int unread, long rank) {
        this.title = title;
        this.messages = messages;
        this.conversationKey = conversationKey;
        this.usersKeys = usersKeys;
        this.unread = unread;
        this.rank = rank;
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
        int unread = 0;
        if (snapshot.child("unread").exists()) {
            unread = ((Long) snapshot.child("unread").getValue()).intValue();
        }
        return new Conversation((String) snapshot.child("title").getValue(), messages, (String) snapshot.child("conversationKey").getValue(), usersKeys, unread,(Long) snapshot.child("rank").getValue());
    }

    /*
    Convert the conversation model into a HashMap
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put("title", title);
        conversation.put("messages", messages);
        conversation.put("conversationKey", conversationKey);
        conversation.put("usersKeys", usersKeys);
        conversation.put("unread", unread);
        conversation.put("rank", rank);
        return conversation;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }
}
