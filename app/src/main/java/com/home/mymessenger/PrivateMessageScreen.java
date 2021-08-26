package com.home.mymessenger;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.MessageData;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.Sort;

public class PrivateMessageScreen extends AppCompatActivity implements FireBaseDBHelper.onDatabaseUpdateListener {

    private static final String TAG = "PrivateMessageScreen";
    private ShapeableImageView shapeableImageView;
    private TextView userNameTextView;
    private TextView userStatusTextView;

    private final Realm realm = RealmHelper.getInstance().getRealm();
    private ChatData chatData;

    private String chatID;

    private EditText sendMessageEditText;

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView privateMessageRecycler;
    private PrivateMessageRecyclerAdapter adapter;
    private List<MessageData> messageDataList;
    private boolean isRan = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FireBaseDBHelper.getInstance().removeListenerFromLoadChatContent();
        Log.d(TAG, "onDestroy: value event listener removed from load chat content");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_screen);

        userNameTextView = findViewById(R.id.toolbar_user_name_text_view);
        userStatusTextView = findViewById(R.id.toolbar_user_status_text_view);
        shapeableImageView = findViewById(R.id.toolbar_shapeable_image_view);
        sendMessageEditText = findViewById(R.id.send_message_edit_text);
        ImageButton sendMessageImageButton = findViewById(R.id.send_message_image_button);

        privateMessageRecycler = findViewById(R.id.private_message_screen_recycler_view);
        privateMessageRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        privateMessageRecycler.setLayoutManager(linearLayoutManager);

        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendMessageEditText.getText().toString();
                if (!message.equals("")) {
                    sendMessage(message, user.getUid(), chatData.getReceiverID(), getDate());
                }
                sendMessageEditText.setText("");
            }
        });
        loadChat();
        setToolBar();
        addChatToReceiver(chatData.getReceiverID());
        isRan = true;
        Log.d(TAG, "onCreate: " + isRan);
    }

    private void addChatToReceiver(String receiverID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("user_chats").child(receiverID).child(chatID);
    }

    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG, "onCreate: " + c);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.getDefault());
        String formattedDate = df.format(c);
        Log.d(TAG, "onCreate: " + formattedDate);
        return formattedDate;
    }

    private void sendMessage(String messageContent, String sender, String receiver, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message_content", messageContent.trim());
        messageMap.put("sender", sender);
        messageMap.put("receiver", receiver);
        messageMap.put("date", date);
        reference.child("chats").child(chatID).child("messages").push().setValue(messageMap);

        updateLatestMessageAndDate(messageContent, date);
    }

    private void updateLatestMessageAndDate(String messageContent, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> latestMessageAndDateMap = new HashMap<>();
        latestMessageAndDateMap.put("latest_message", messageContent);
        latestMessageAndDateMap.put("latest_message_date", date);

        reference.child("chats").child(chatID).child("latest_message_and_date").updateChildren(latestMessageAndDateMap);
    }

    private void getMessages(String contactID) {
        messageDataList = new ArrayList<>();
        messageDataList.clear();
        for (MessageData message : chatData.getMessages().sort("messageID", Sort.ASCENDING)) {
            if (message != null) {
                if (message.getReceiver().equals(user.getUid()) && message.getSender().equals(contactID) ||
                        message.getReceiver().equals(contactID) && message.getSender().equals(user.getUid())) {
                    messageDataList.add(message);
                    Log.d(TAG, "getMessages: messages added to the list: " + message.getMessageContent() + "      " + message);
                }
            }
            adapter = new PrivateMessageRecyclerAdapter(this, messageDataList);
            privateMessageRecycler.setAdapter(adapter);
        }
    }

    @Override
    public void onDatabaseUpdate() {
        Log.d(TAG, "onDatabaseUpdate: load chat: " + chatID);
        Log.d(TAG, "onDatabaseUpdate: " + chatData.getReceiverID());
        getMessages(chatData.getReceiverID());
//        PrivateMessageAsyncTask task = new PrivateMessageAsyncTask(this);
//        task.execute(chatData.getReceiverID());
    }

    private void loadChat() {
        chatID = getIntent().getStringExtra(IntentKeys.CHAT_ID);
        chatData = realm.where(ChatData.class).equalTo("chatID", chatID).findFirst();
        userNameTextView.setText(chatData.getReceiver());
        UserData userData = realm.where(UserData.class).equalTo("userID", chatData.getReceiverID()).findFirst();
        if (userData != null) {
            userStatusTextView.setText(userData.getUserStatus());
            Picasso.get().load(chatData.getProfilePicture()).fit().centerInside().into(shapeableImageView);
        }
        if (!isRan) {
            Log.d(TAG, "loadChat: " + isRan);
            startFireBaseListening();
        }

    }
    private void startFireBaseListening() {
        FireBaseDBHelper helper = FireBaseDBHelper.getInstance();
        helper.setListener(PrivateMessageScreen.this);
        helper.listenForChatDataChange(chatID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private static class PrivateMessageAsyncTask extends AsyncTask<String, Void, List<MessageData>> {

        private WeakReference<PrivateMessageScreen> weakReference;

        PrivateMessageAsyncTask(PrivateMessageScreen activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected List<MessageData> doInBackground(String... strings) {
            PrivateMessageScreen activity = weakReference.get();
//            activity.getMessages(strings[0]);
            return activity.messageDataList;
        }

        @Override
        protected void onPostExecute(List<MessageData> list) {
            super.onPostExecute(list);
            PrivateMessageScreen activity = weakReference.get();
//
//            activity.adapter = new PrivateMessageRecyclerAdapter(activity, activity.messageDataList);
//            activity.privateMessageRecycler.setAdapter(activity.adapter);
        }
    }
}
