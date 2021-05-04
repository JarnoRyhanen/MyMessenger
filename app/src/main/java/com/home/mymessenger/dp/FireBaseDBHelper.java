package com.home.mymessenger.dp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.MessageData;

import java.util.Map;

import io.realm.Realm;

public class FireBaseDBHelper {

    private static final String TAG = "FireBaseDBHelper";
    private static FireBaseDBHelper instance;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final Realm realm = RealmHelper.getInstance().getRealm();

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
            throw new IllegalStateException(TAG + " is already initalized");
        }
        return instance;
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
                    Log.d(TAG, "onDataChange: " + changedData);
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

                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("chat_name"));
                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("date"));
                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("latest_message"));
                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("user_name"));
                    Log.d(TAG, "updateUserChats: " + chatObjectMap.get("user_profile_pic"));

                    ChatData chatData = new ChatData();
                    chatData.setChatID(key);
                    chatData.setChatName((String) chatObjectMap.get("chat_name"));
                    chatData.setLatestActive((String) chatObjectMap.get("date"));
                    chatData.setLatestMessage((String) chatObjectMap.get("latest_message"));
                    chatData.setProfilePicture((String) chatObjectMap.get("user_profile_pic"));
                    chatData.setUserName((String) chatObjectMap.get("user_name"));
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
//                            Log.d(TAG, "loadChatContent: message content " + messageMap.get("message_content"));
//                            Log.d(TAG, "loadChatContent: messageid " + messageID);
//                            Log.d(TAG, "loadChatContent: sender " + messageMap.get("sender"));
//                            Log.d(TAG, "loadChatContent: date " + messageMap.get("date"));
//                            Log.d(TAG, "loadChatContent: " + messageID)
                            MessageData messageData = new MessageData();
                            messageData.setMessageID(messageID);
                            messageData.setMessageContent((String) messageMap.get("message_content"));
                            messageData.setSender((String) messageMap.get("sender"));
                            messageData.setDate((String) messageMap.get("date"));
                        }
                    }
                }
            });
        }
        if (listener != null) {
            listener.onDatabaseUpdate();
        }
    }

    public void setListener(onDatabaseUpdateListener listener) {
        this.listener = listener;
    }

    public interface onDatabaseUpdateListener {
        void onDatabaseUpdate();
    }
}
