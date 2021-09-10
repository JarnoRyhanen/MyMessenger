package com.home.mymessenger.messaging;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.IntentKeys;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.MessageData;
import com.home.mymessenger.data.UserData;
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
import io.realm.Sort;

public class PrivateMessageScreen extends AppCompatActivity implements FireBaseDBHelper.onMessageAddedListener {

    private static final String TAG = "PrivateMessageScreen";
    private static final int REQUEST_CALL = 1;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ShapeableImageView shapeableImageView;
    private TextView userNameTextView;
    private TextView userStatusTextView;
    private EditText sendMessageEditText;


    private ImageButton sendMessageImageButton;
    private ImageButton addIcon;
    private ImageButton voiceMessageIcon;

    private UserData userData;

    private final Realm realm = RealmHelper.getInstance().getRealm();
    private ChatData chatData;

    private String chatID;

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

        instantiateViews();
        createRecyclerView();
        setOnClickListeners();
        loadChat();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    Log.d(TAG, "onActivityResult: " + uri.toString());
                    if (uri.toString().contains("video")) {
//                        Intent intent = new Intent(PrivateMessageScreen.this, EditVideoActivity.class);
//                        setIntentExtras(intent, uri);
                    } else {
                        Intent intent = new Intent(PrivateMessageScreen.this, EditImageActivity.class);
                        setIntentExtras(intent, uri);
                    }
                }
            }
        });

        setToolBar();
        addChatToReceiver(chatData.getReceiverID());
        isRan = true;
        Log.d(TAG, "onCreate: " + isRan);
    }

    private void setIntentExtras(Intent intent, Uri uri) {
        intent.putExtra("uri", uri.toString());
        intent.putExtra("chatID", chatID);
        intent.putExtra("receiverID", chatData.getReceiverID());
        startActivity(intent);
    }


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addIcon) {

                Log.d(TAG, "onClick: add icon pressed");
                Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK);
                imageGalleryIntent.setType("video/*, image/*");
                if (imageGalleryIntent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(imageGalleryIntent);
                }

            } else if (v == voiceMessageIcon) {
                Log.d(TAG, "onClick: voice pressed");
            } else if (v == sendMessageImageButton) {
                String message = sendMessageEditText.getText().toString();
                if (!message.equals("")) {
                    sendMessage(message, "null", user.getUid(), chatData.getReceiverID(), getDate());
                }
                sendMessageEditText.setText("");
            }
        }
    };

    private void setOnClickListeners() {
        addIcon.setOnClickListener(onClickListener);
        voiceMessageIcon.setOnClickListener(onClickListener);
        sendMessageImageButton.setOnClickListener(onClickListener);
    }

    private void instantiateViews() {
        userNameTextView = findViewById(R.id.toolbar_user_name_text_view);
        userStatusTextView = findViewById(R.id.toolbar_user_status_text_view);
        shapeableImageView = findViewById(R.id.toolbar_shapeable_image_view);
        sendMessageEditText = findViewById(R.id.send_message_edit_text);
        sendMessageImageButton = findViewById(R.id.send_message_image_button);
        addIcon = findViewById(R.id.add);
        voiceMessageIcon = findViewById(R.id.voice);
    }

    private void createRecyclerView() {
        privateMessageRecycler = findViewById(R.id.private_message_screen_recycler_view);
        privateMessageRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        privateMessageRecycler.setLayoutManager(linearLayoutManager);
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

    private void sendMessage(String messageContent, String messageImage, String sender, String receiver, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message_content", messageContent.trim());
        messageMap.put("sender", sender);
        messageMap.put("receiver", receiver);
        messageMap.put("date", date);
        messageMap.put("message_image", messageImage);
        messageMap.put("message_video", "null");
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

    private void getMessages() {
        messageDataList = new ArrayList<>();
        messageDataList.clear();
        for (MessageData message : chatData.getMessages().sort("messageID", Sort.ASCENDING)) {
            if (message != null) {
                Log.d(TAG, "getMessages: messages added to the list: " + message.getMessageContent() + "      " + message);
                messageDataList.add(message);
            }
        }
        adapter = new PrivateMessageRecyclerAdapter(this, messageDataList);
        privateMessageRecycler.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }

    private void loadChat() {
        chatID = getIntent().getStringExtra(IntentKeys.CHAT_ID);
        chatData = realm.where(ChatData.class).equalTo("chatID", chatID).findFirst();
        userNameTextView.setText(chatData.getReceiver());
        userData = realm.where(UserData.class).equalTo("userID", chatData.getReceiverID()).findFirst();
        if (userData != null) {
            Log.d(TAG, "loadChat: WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
            userStatusTextView.setText(userData.getUserStatus());
            Picasso.get().load(userData.getUserProfilePicture()).fit().centerInside().into(shapeableImageView);
        }
        if (!isRan) {
            Log.d(TAG, "loadChat: " + isRan);
            startFireBaseListening();
        }

    }

    private void startFireBaseListening() {
        FireBaseDBHelper helper = FireBaseDBHelper.getInstance();
        helper.setOnMessageAddedListener(PrivateMessageScreen.this);
        helper.listenForChatDataChange(chatID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.private_message_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_phone_call) {
//            callPhone();
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

    private void callPhone() {
        String number = userData.getUserPhoneNumber();

        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhone();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMessageAdded() {
        Log.d(TAG, "onDatabaseUpdate: load chat: " + chatID);
        Log.d(TAG, "onDatabaseUpdate: " + chatData.getReceiverID());
        Log.d(TAG, "onDatabaseUpdate: called");
        getMessages();
    }
}
