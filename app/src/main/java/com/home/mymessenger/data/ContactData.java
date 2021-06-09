package com.home.mymessenger.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactData extends RealmObject {

    @PrimaryKey
    private String contactID;
    private String contactName;
    private String contactPhoneNumber;

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }
}
