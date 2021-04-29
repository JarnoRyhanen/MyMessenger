package com.home.mymessenger;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserMainScreenMessageData extends RealmObject {

    @PrimaryKey
    private String chatID;
    private String userName;
    private String profilePicture;
    private String latestMessage;
    private String latestActive;

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
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
}
