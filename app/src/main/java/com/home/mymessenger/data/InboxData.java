package com.home.mymessenger.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class InboxData extends RealmObject {

    @PrimaryKey
    private String messageID;
    private String chatID;
    private String message;
    private String senderID;

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
}
