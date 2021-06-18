package com.home.mymessenger.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatData extends RealmObject {

    @PrimaryKey
    private String chatID;
    private String receiver;
    private String receiverID;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
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
