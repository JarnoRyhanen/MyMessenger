package com.home.mymessenger.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatData extends RealmObject {

    @PrimaryKey
    private String chatID;
    private String chatName;
    private String userName;
    private String profilePicture;
    private RealmList<MessageData> messages = new RealmList<>();
    private String latestMessage;
    private String latestActive;

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }


    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getLatestActive() {
        return latestActive;
    }

    public void setLatestActive(String latestActive) {
        this.latestActive = latestActive;
    }


    public RealmList<MessageData> getMessages() {
        return messages;
    }

    public void setMessages(RealmList<MessageData> messages) {
        this.messages = messages;
    }
}
