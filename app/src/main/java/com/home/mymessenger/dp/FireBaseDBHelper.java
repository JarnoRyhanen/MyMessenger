package com.home.mymessenger.dp;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.contacts.SearchForContactsActivity;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.ContactData;
import com.home.mymessenger.data.MessageData;
import com.home.mymessenger.data.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;

public class FireBaseDBHelper {

    private static final String TAG = "FireBaseDBHelper";
    private static FireBaseDBHelper instance;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();
    private final Realm realm = RealmHelper.getInstance().getRealm();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private onDatabaseUpdateListener listener;

    public FireBaseDBHelper() {

    }

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException(TAG + " is already initialized");
        }

        instance = new FireBaseDBHelper();
    }

    public static FireBaseDBHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(TAG + " is already initialized");
        }
        return instance;
    }

    public void listenForUserChange() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserID = currentUser.getUid();
            DatabaseReference databaseReference = database.getReference("user_specific_info").child(currentUserID);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final Object changedData = snapshot.getValue();
                    updateUser(changedData, currentUserID);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateUser(Object changedData, String currentUserID) {
        if (changedData instanceof Map) {

            final Map<String, Object> userMap = (Map<String, Object>) changedData;
            realm.executeTransaction(realm1 -> {
                UserData userData = new UserData();
                userData.setUserID(currentUserID);
                userData.setUserName((String) userMap.get("user_name"));
                userData.setUserProfilePicture((String) userMap.get("profile_picture"));
                userData.setUserStatus((String) userMap.get("current_status"));
                realm.copyToRealmOrUpdate(userData);
            });
            Object userContactObject = userMap.get("contacts");
            final Map<String, Object> userContactObjectMap = (Map<String, Object>) userContactObject;
            if (userContactObjectMap != null) {
                for (String userID : userContactObjectMap.keySet()) {
                    Log.d(TAG, "updateUser: " + userID);
                    readOtherUsersData(userID);
                }
            }
        }
        if (listener != null) {
            listener.onDatabaseUpdate();
        }
    }

    private void readOtherUsersData(String userID) {
        DatabaseReference databaseReference = database.getReference("user_specific_info").child(userID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Object changedData = snapshot.getValue();
                if (changedData instanceof Map) {
                    final Map<String, Object> userMap = (Map<String, Object>) changedData;
                    Log.d(TAG, "different user: " + userMap);
                    realm.executeTransaction(realm1 -> {
                        UserData userData = new UserData();
                        userData.setUserID(userID);
                        userData.setUserName((String) userMap.get("user_name"));
                        userData.setUserProfilePicture((String) userMap.get("profile_picture"));
                        userData.setUserStatus((String) userMap.get("current_status"));
                        realm.copyToRealmOrUpdate(userData);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });

    }

    public void listerForUserChatChange() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserID = currentUser.getUid();
            DatabaseReference myRef = database.getReference("user_chats").child(currentUserID);

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final Object changedData = snapshot.getValue();
                    updateUserChats(changedData);
                    Log.d(TAG, "onDataChange " + changedData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void updateUserChats(Object userChat) {
        if (userChat instanceof Map) {
            final Map<String, Object> userChatMap = (Map<String, Object>) userChat;

            realm.executeTransaction(realm1 -> {
                for (String key : userChatMap.keySet()) {
                    Log.d(TAG, "updateUserChats: key: " + key);

                    Object chatObject = userChatMap.get(key);
                    final Map<String, Object> chatObjectMap = (Map<String, Object>) chatObject;
//                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("chat_name"));
//                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("date"));
//                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("latest_message"));
//                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("user_name"));
//                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("user_profile_pic"));
                    ChatData chatData = new ChatData();
                    chatData.setChatID(key);
                    chatData.setLatestActive((String) chatObjectMap.get("latest_message_date"));
                    chatData.setLatestMessage((String) chatObjectMap.get("latest_message"));
                    chatData.setProfilePicture((String) chatObjectMap.get("user_profile_pic"));
                    chatData.setReceiver((String) chatObjectMap.get("receiver"));
                    chatData.setReceiverID((String) chatObjectMap.get("receiverID"));

                    realm.copyToRealmOrUpdate(chatData);
                }
            });
        }

        if (listener != null) {
            listener.onDatabaseUpdate();
        }
    }

    public void listenForChatDataChange(String chatID) {

        DatabaseReference chatRef = database.getReference("chats").child(chatID);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                final Object changedData = snapshot.getValue();
                loadChatContent(changedData);
                Log.d(TAG, "onDataChange: " + changedData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressWarnings("unchecked")
    private void loadChatContent(Object chat) {
        if (chat instanceof Map) {
            final Map<String, Object> chatContentMap = (Map<String, Object>) chat;
            realm.executeTransaction(realm1 -> {

                Object messagesObject = chatContentMap.get("messages");
                final Map<String, Object> messagesMap = (Map<String, Object>) messagesObject;
                if (messagesMap != null) {
                    for (String messageID : messagesMap.keySet()) {
                        Object messageObject = messagesMap.get(messageID);
                        final Map<String, Object> messageMap = (Map<String, Object>) messageObject;
                        if (messageMap != null) {
                            Log.d(TAG, "loadChatContent: message content " + messageMap.get("message_content"));
                            Log.d(TAG, "loadChatContent: messageid " + messageID);
                            Log.d(TAG, "loadChatContent: sender " + messageMap.get("sender"));
                            Log.d(TAG, "loadChatContent: date " + messageMap.get("date"));
                            Log.d(TAG, "loadChatContent: " + messageID);
                            MessageData messageData = new MessageData();
                            messageData.setMessageID(messageID);
                            messageData.setMessageContent((String) messageMap.get("message_content"));
                            messageData.setSender((String) messageMap.get("sender"));
                            messageData.setDate((String) messageMap.get("date"));
                            messageData.setReceiver((String) messageMap.get("receiver"));
                            realm.copyToRealmOrUpdate(messageData);
                        }
                    }
                }
            });
            if (listener != null) {
                listener.onDatabaseUpdate();
            }
        }
    }

    public void addUserToChats(String userName, String contactID) {
        DatabaseReference databaseReference = database.getReference("user_specific_info").child(contactID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Object picture = snapshot.getValue();
                final Map<String, Object> picMap = (Map<String, Object>) picture;

                Log.d(TAG, "onData change" + picMap.get("profile_picture"));
                String pictureUrl = (String) picMap.get("profile_picture");

                DatabaseReference userRef = database.getReference("user_chats")
                        .child(user.getUid())
                        .child(UUID.randomUUID().toString());
                Map<String, Object> userChatMap = new HashMap<>();
                userChatMap.put("receiver", userName);
                userChatMap.put("receiverID", contactID);
                userChatMap.put("user_profile_pic", pictureUrl);
                userRef.updateChildren(userChatMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setListener(onDatabaseUpdateListener listener) {
        this.listener = listener;
    }

    public interface onDatabaseUpdateListener {
        void onDatabaseUpdate();
    }

    private SearchForContactsActivity activity;

    public void setActivity(SearchForContactsActivity activity) {
        this.activity = activity;
    }

    public void checkForUser1() {
        DatabaseReference userRef = ref.child("users");
        userRef.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                    if (userMap != null) {
                        for (String key : userMap.keySet()) {
//                            String foundUserName = userMap.get(key).toString().trim();
                            getContact(key);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });
    }

    private void getContact(String userID) {

        Log.d(TAG, "getContact: " + userID);
        DatabaseReference userSpecificInfoRef = ref.child("user_specific_info").child(userID).child("phone_number");
        userSpecificInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phoneNumber = snapshot.getValue().toString().trim();
                    Log.d(TAG, "onDataChange: userID: " + userID + " phone number: " + phoneNumber);

                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    Cursor cursor = activity.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                    String name = "INVALID";
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            name = cursor.getString(0).trim();
                        }
                    }

                    Cursor phoneCursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            , new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},
                            "DISPLAY_NAME ='" + name + "'", null, null);

                    if (phoneCursor != null) {
                        if (phoneCursor.moveToFirst()) {
                            String contactNumber = phoneCursor.getString(0);
                            Log.d(TAG, "getContactName: " + name + ", contact number " + contactNumber);
                            String finalName = name;
                            realm.executeTransaction(realm1 -> {
                                ContactData data = new ContactData();
                                data.setContactID(userID);
                                data.setContactName(finalName);
                                data.setContactPhoneNumber(contactNumber);
                                activity.contactDataList.add(data);
                                realm.copyToRealmOrUpdate(data);
                            });
                            phoneCursor.close();
                        }
                    }
                    cursor.close();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: ERROR", error.toException().fillInStackTrace());
            }
        });
    }
}