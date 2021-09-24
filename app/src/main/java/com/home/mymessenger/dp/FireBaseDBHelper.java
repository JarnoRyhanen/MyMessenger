package com.home.mymessenger.dp;

import android.content.Context;
import android.content.res.Resources;
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
import com.home.mymessenger.R;
import com.home.mymessenger.contacts.SearchForContactsActivity;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.ContactData;
import com.home.mymessenger.data.InboxData;
import com.home.mymessenger.data.MessageData;
import com.home.mymessenger.data.UserData;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class FireBaseDBHelper {

    private static final String TAG = "FireBaseDBHelper";
    private static FireBaseDBHelper instance;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();
    private final Realm realm = RealmHelper.getInstance().getRealm();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Context context;
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

    public void initContext(Context context) {
        this.context = context;
    }

    public Context getApplicationContext() {
        return context;
    }

    private Resources getLocalResources() {
        return getApplicationContext().getResources();
    }

    public void listenForUserSpecificInfoChange() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserID = currentUser.getUid();
            DatabaseReference databaseReference = database.getReference(getLocalResources().getString(R.string.user_specific_info))
                    .child(currentUserID);

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
                userData.setUserName((String) userMap.get(getLocalResources().getString(R.string.user_name)));
                userData.setUserProfilePicture((String) userMap.get(getLocalResources().getString(R.string.profile_picture)));
                userData.setUserStatus((String) userMap.get(getLocalResources().getString(R.string.current_status)));
                userData.setUserPhoneNumber((String) userMap.get(getLocalResources().getString(R.string.phone_number)));
                realm.copyToRealmOrUpdate(userData);
            });
            if (listener != null) {
                Log.d(TAG, "updateContent: LISTENER CALLED IN UPDATE USER");
                listener.onDatabaseUpdate();
            }
        }
    }

    public void listenToContactUsersDataChange() {
        if (user != null) {
            DatabaseReference databaseReference = database.getReference(getLocalResources().getString(R.string.user_specific_info))
                    .child(user.getUid())
                    .child(getLocalResources().getString(R.string.contacts));
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        readContactUsersData(dataSnapshot.getKey());
                    }
                    if (listener != null) {
                        Log.d(TAG, "updateContent: LISTENER CALLED IN LISTEN TO CONTACT USERS DATA CHANGE");
                        listener.onDatabaseUpdate();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void readContactUsersData(String userID) {
        DatabaseReference databaseReference = ref.child(getLocalResources().getString(R.string.user_specific_info)).child(userID);
        Log.d(TAG, "readOtherUsersData: " + userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final Object contactSnapShotObject = snapshot.getValue();
                    final Map<String, Object> userMap = (Map<String, Object>) contactSnapShotObject;
                    Log.d(TAG, "different user: " + userMap);
                    realm.executeTransaction(realm1 -> {
                        UserData data = new UserData();
                        data.setUserID(userID);
//                        Log.d(TAG, "onDataChange: " +userMap.get(getLocalResources().getString(R.string.user_name)));
                        data.setUserName((String) userMap.get(getLocalResources().getString(R.string.user_name)));
                        data.setUserProfilePicture((String) userMap.get(getLocalResources().getString(R.string.profile_picture)));
                        data.setUserStatus((String) userMap.get(getLocalResources().getString(R.string.current_status)));
                        data.setUserPhoneNumber((String) userMap.get(getLocalResources().getString(R.string.phone_number)));
                        realm.copyToRealmOrUpdate(data);
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
            DatabaseReference myRef = database.getReference(getLocalResources().getString(R.string.user_chats))
                    .child(currentUserID);

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        final Object changedData = snapshot.getValue();
                        updateUserChats(changedData);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.getMessage();
                }
            });
        }
    }

    private void updateUserChats(Object userChat) {
        if (userChat instanceof Map) {
            final Map<String, Object> userChatMap = (Map<String, Object>) userChat;
            if (userChatMap != null) {
                realm.beginTransaction();
                for (String key : userChatMap.keySet()) {
                    Log.d(TAG, "updateUserChats: key: " + key);

                    Object chatObject = userChatMap.get(key);
                    if (chatObject != null) {
                        final Map<String, Object> chatObjectMap = (Map<String, Object>) chatObject;
                        if (chatObjectMap != null) {
//                            Log.d(TAG, "updateUserChats: " + chatObjectMap.get("chat_name"));
//                            Log.d(TAG, "updateUserChats: " + chatObjectMap.get("date"));
//                            Log.d(TAG, "updateUserChats: " + chatObjectMap.get("latest_message"));
//                            Log.d(TAG, "updateUserChats: " + chatObjectMap.get("user_name"));
//                            Log.d(TAG, "updateUserChats: user profile pic: " + chatObjectMap.get("user_profile_pic"));
                            ChatData chatData = new ChatData();
                            if (chatData != null) {
                                chatData.setChatID(key);
                                chatData.setLatestActive((String) chatObjectMap.get(getLocalResources().getString(R.string.latest_message_date)));
                                chatData.setLatestMessage((String) chatObjectMap.get(getLocalResources().getString(R.string.latest_message)));
                                chatData.setProfilePicture((String) chatObjectMap.get(getLocalResources().getString(R.string.user_profile_pic)));
                                chatData.setReceiver((String) chatObjectMap.get(getLocalResources().getString(R.string.receiver)));
                                chatData.setReceiverID((String) chatObjectMap.get(getLocalResources().getString(R.string.receiverID)));
                                chatData.setMessages(new RealmList<>());
                                realm.insertOrUpdate(chatData);
                            }
                        }
                    }
                }

                realm.commitTransaction();
                if (listener != null) {
                    Log.d(TAG, "updateC: LISTENER CALLED IN UPDATE USER CHATS");
                    listener.onDatabaseUpdate();
                }
            }
        }
    }

    private ValueEventListener valueEventListener;
    private DatabaseReference chatRef;

    public void listenForChatDataChange(String chatID) {
        chatRef = database.getReference(getLocalResources().getString(R.string.chats))
                .child(chatID);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Object changedData = snapshot.getValue();
                loadChatContent(changedData, chatID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        chatRef.addValueEventListener(valueEventListener);
    }

    @SuppressWarnings("unchecked")
    private void loadChatContent(Object chat, String chatID) {
        if (chat instanceof Map) {
            final Map<String, Object> chatContentMap = (Map<String, Object>) chat;

            ChatData chatData = realm.where(ChatData.class).equalTo("chatID", chatID).findFirst();
            realm.beginTransaction();
            Object messagesObject = chatContentMap.get(getLocalResources().getString(R.string.messages));
            final Map<String, Object> messagesMap = (Map<String, Object>) messagesObject;

            if (messagesMap != null) {
                RealmList<MessageData> messageDataRealmList = new RealmList<>();
                for (String messageID : messagesMap.keySet()) {
                    Object messageObject = messagesMap.get(messageID);
                    final Map<String, Object> messageMap = (Map<String, Object>) messageObject;
                    if (messageMap != null) {
                        MessageData messageData = new MessageData();
                        messageData.setMessageID(messageID);
                        messageData.setMessageContent((String) messageMap.get(getLocalResources().getString(R.string.message_content)));
                        messageData.setSender((String) messageMap.get(getLocalResources().getString(R.string.sender)));
                        messageData.setDate((String) messageMap.get(getLocalResources().getString(R.string.date)));
                        messageData.setReceiver((String) messageMap.get(getLocalResources().getString(R.string.receiver)));

                        if (!messageMap.get(getLocalResources().getString(R.string.message_image)).equals("null")) {
                            messageData.setImage((String) messageMap.get(getLocalResources().getString(R.string.message_image)));
                        }
                        messageDataRealmList.add(realm.copyToRealmOrUpdate(messageData));
                    }
                }
                if (chatData != null) {
                    chatData.setMessages(messageDataRealmList);
                    realm.copyToRealmOrUpdate(chatData);
                }
            }
            realm.commitTransaction();
            if (onMessageAddedListener != null) {
                Log.d(TAG, "LOAD CHAT CONTENT LISTENER CALLED: ");
                onMessageAddedListener.onMessageAdded();
            }
        }
    }

    public void removeListenerFromLoadChatContent() {
        chatRef.removeEventListener(valueEventListener);
    }

    public void listenForLatestMessage(String chatID) {
        if (user != null) {
            DatabaseReference reference = ref.child(getLocalResources().getString(R.string.chats))
                    .child(chatID)
                    .child(getLocalResources().getString(R.string.latest_message_and_date));

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Map<String, Object> latestMessageAndDateMap = (Map<String, Object>) snapshot.getValue();
                        String latestMessage = (String) latestMessageAndDateMap.get(getLocalResources().getString(R.string.latest_message));
                        String latestMessageDate = (String) latestMessageAndDateMap.get(getLocalResources().getString(R.string.latest_message_date));

                        Map<String, Object> updaterMap = new HashMap<>();
                        updaterMap.put(getLocalResources().getString(R.string.latest_message), latestMessage);
                        updaterMap.put(getLocalResources().getString(R.string.latest_message_date), latestMessageDate);

                        DatabaseReference userChatReference = ref.child(getLocalResources().getString(R.string.user_chats))
                                .child(user.getUid())
                                .child(chatID);
                        userChatReference.updateChildren(updaterMap);

                        if (onLatestMessageAddedListener != null) {
                            onLatestMessageAddedListener.onLatestMessageAdded();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private onLatestMessageAddedListener onLatestMessageAddedListener;

    public void setOnLatestMessageAddedListener(onLatestMessageAddedListener messageAddedListener) {
        this.onLatestMessageAddedListener = messageAddedListener;
    }

    public interface onLatestMessageAddedListener {
        void onLatestMessageAdded();
    }

    public void listerForInboxDataChange() {
        if (user != null) {
            DatabaseReference inboxRef = ref.child(getLocalResources().getString(R.string.user_inbox))
                    .child(user.getUid());

            inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        realm.executeTransaction(realm1 -> {
                            final Map<String, Object> inboxMap = (Map<String, Object>) snapshot.getValue();

                            for (String child : inboxMap.keySet()) {
                                Object childObject = inboxMap.get(child);
                                Map<String, Object> childObjectMap = (Map<String, Object>) childObject;

                                if (childObjectMap != null) {
                                    InboxData inboxData = new InboxData();
                                    inboxData.setChatID((String) childObjectMap.get(getLocalResources().getString(R.string.chatID)));
                                    inboxData.setMessage((String) childObjectMap.get(getLocalResources().getString(R.string.message_content)));
                                    inboxData.setSenderID((String) childObjectMap.get(getLocalResources().getString(R.string.senderID)));
                                    inboxData.setSenderName((String) childObjectMap.get(getLocalResources().getString(R.string.sender_name)));
                                    inboxData.setSenderProfilePic((String) childObjectMap.get(getLocalResources().getString(R.string.sender_profile_pic)));
                                    inboxData.setMessageID(child);
                                    realm1.copyToRealmOrUpdate(inboxData);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void addUserToChats(String userName, String contactID, String chatID, Context context) {
        DatabaseReference databaseReference = database.getReference(context.getResources().getString(R.string.user_specific_info))
                .child(contactID);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Object picture = snapshot.getValue();
                final Map<String, Object> picMap = (Map<String, Object>) picture;

                String pictureUrl = (String) picMap.get(context.getResources().getString(R.string.profile_picture));
                DatabaseReference userRef = database.getReference(context.getResources().getString(R.string.user_chats))
                        .child(user.getUid())
                        .child(chatID);

                Map<String, Object> userChatMap = new HashMap<>();
                userChatMap.put(context.getResources().getString(R.string.receiver), userName);
                userChatMap.put(context.getResources().getString(R.string.receiverID), contactID);
                userChatMap.put(context.getResources().getString(R.string.user_profile_pic), pictureUrl);
                userRef.updateChildren(userChatMap);

                DatabaseReference chatRef = database.getReference(context.getResources().getString(R.string.chats))
                        .child(chatID)
                        .child(context.getResources().getString(R.string.users));

                Map<String, Object> chatMap = new HashMap<>();
                chatMap.put(user.getUid(), user.getDisplayName());
                chatRef.updateChildren(chatMap);
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
        DatabaseReference userRef = ref.child(activity.getResources().getString(R.string.users));
        userRef.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                    if (userMap != null) {
                        for (String key : userMap.keySet()) {
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

        DatabaseReference userSpecificInfoRef = ref.child(activity.getResources().getString(R.string.user_specific_info))
                .child(userID)
                .child(activity.getResources().getString(R.string.phone_number));

        userSpecificInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phoneNumber = snapshot.getValue().toString().trim();

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

    private onMessageAddedListener onMessageAddedListener;

    public void setOnMessageAddedListener(onMessageAddedListener listener) {
        this.onMessageAddedListener = listener;
    }

    public interface onMessageAddedListener {
        void onMessageAdded();
    }


}