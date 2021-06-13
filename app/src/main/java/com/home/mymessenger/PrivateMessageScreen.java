package com.home.mymessenger;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.MessageData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;

public class PrivateMessageScreen extends AppCompatActivity implements FireBaseDBHelper.onDatabaseUpdateListener {

    private static final String TAG = "PrivateMessageScreen";
    private ShapeableImageView shapeableImageView;
    private TextView userNameTextView;

//    private FireBaseDBHelper.onDatabaseUpdateListener listener = new FireBaseDBHelper.onDatabaseUpdateListener() {
//        @Override
//        public void onDatabaseUpdate() {
//      getMessages(chatID);
//        }
//    };

    private Realm realm = RealmHelper.getInstance().getRealm();
    private ChatData chatData;

    private String chatID;

    private EditText sendMessageEditText;
    private ImageButton sendMessageImageButton;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView privateMessageRecycler;
    private PrivateMessageRecyclerAdapter adapter;
    private List<MessageData> messageDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_screen);

        userNameTextView = findViewById(R.id.toolbar_user_name_text_view);
        shapeableImageView = findViewById(R.id.toolbar_shapeable_image_view);
        sendMessageEditText = findViewById(R.id.send_message_edit_text);
        sendMessageImageButton = findViewById(R.id.send_message_image_button);

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
    }

    private void startFireBaseListening() {
        FireBaseDBHelper helper = FireBaseDBHelper.getInstance();
        helper.setListener(PrivateMessageScreen.this);
        helper.listenForChatDataChange(chatID);
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

        Map<String, Object> latestMessageAndDateMap = new HashMap<>();
        latestMessageAndDateMap.put("latest_message", messageContent);
        latestMessageAndDateMap.put("latest_message_date", date);

        reference.child("user_chats").child(user.getUid()).child(chatID).updateChildren(latestMessageAndDateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: updated");
            }
        });

    }

    private void getMessages(String contactID) {
        messageDataList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("chats").child(chatID).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    messageDataList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String messageID = dataSnapshot.getKey();
                        Log.d(TAG, "onDataChange: key: " + messageID);
                        MessageData messageData = realm.where(MessageData.class).equalTo("messageID", messageID).findFirst();
                        if (messageData != null) {
                            Log.d(TAG, "onDataChange: " + messageData.getSender() + messageData.getReceiver() + messageData.getDate() + messageData.getMessageContent());
                            if (messageData.getReceiver().equals(user.getUid()) && messageData.getSender().equals(contactID) ||
                                    messageData.getReceiver().equals(contactID) && messageData.getSender().equals(user.getUid())) {
                                messageDataList.add(messageData);
                            }
                        }
                    }
                    adapter = new PrivateMessageRecyclerAdapter(PrivateMessageScreen.this, messageDataList);
                    privateMessageRecycler.setAdapter(adapter);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadChat() {
        chatID = getIntent().getStringExtra(IntentKeys.CHAT_ID);
        chatData = realm.where(ChatData.class).equalTo("chatID", chatID).findFirst();


        userNameTextView.setText(chatData.getReceiver());
        Picasso.get().load(chatData.getProfilePicture()).fit().centerInside().into(shapeableImageView);
        startFireBaseListening();

//        Log.d(TAG, "onCreate: " + chatData.getUserName());
//        Log.d(TAG, "onCreate: " + chatData.getChatID());
//        Log.d(TAG, "onCreate: " + chatData.getProfilePicture());
//        Log.d(TAG, "onCreate: " + chatData.getLatestMessage());
//        Log.d(TAG, "onCreate: " + chatData.getLatestActive());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDatabaseUpdate() {
        Log.d(TAG, "onDatabaseUpdate: database updated");
        getMessages(chatData.getReceiverID());
    }
}
