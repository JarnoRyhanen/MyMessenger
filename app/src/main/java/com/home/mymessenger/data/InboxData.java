package com.home.mymessenger.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class InboxData extends RealmObject {

    @PrimaryKey
    private String messageID;
    private String chatID;
    private String message;
    private String cancelAcceptStatus;
    private String senderID;
    private String senderName;
    private String senderProfilePic;

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

    public String getCancelAcceptStatus() {
        return cancelAcceptStatus;
    }

    public void setCancelAcceptStatus(String cancelAcceptStatus) {
        this.cancelAcceptStatus = cancelAcceptStatus;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfilePic() {
        return senderProfilePic;
    }

    public void setSenderProfilePic(String senderProfilePic) {
        this.senderProfilePic = senderProfilePic;
    }
}
