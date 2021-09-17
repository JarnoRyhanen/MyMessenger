package com.home.mymessenger.messaging;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.home.mymessenger.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EditImageActivity extends AppCompatActivity {

    private static final String TAG = "EditImageActivity";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = storage.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private EditText messageText;
    private ImageButton sendMessageButton;
    private ImageView messageImage;

    private Uri imageUri;
    private String chatID;
    private String receiverID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image_activity);
        chatID = getIntent().getStringExtra("chatID");
        receiverID = getIntent().getStringExtra("receiverID");

        instantiateViews();
        imageUri = Uri.parse(getIntent().getStringExtra("uri"));
        Picasso.get().load(imageUri).into(messageImage);


    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == sendMessageButton) {
                uploadImage();
            }
        }
    };

    private void instantiateViews() {
        messageImage = findViewById(R.id.edit_image_activity_image_view);
        sendMessageButton = findViewById(R.id.edit_image_activity_send_message);
        messageText = findViewById(R.id.edit_image_activity_edit_text);

        sendMessageButton.setOnClickListener(onClickListener);
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            String imageID = UUID.randomUUID().toString();

            StorageReference strReference = storageReference.child("chats/" + chatID + "/" + imageID);

            strReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        downloadImage(imageID);

                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        }
    }

    private void downloadImage(String imageID) {
        StorageReference reference = storageReference.child("chats/" + chatID + "/" + imageID);

        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "onSuccess: " + uri);
            sendMessage(uri);
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void sendMessage(Uri uri) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String messageContent = messageText.getText().toString();
        String sender = user.getUid();
        String receiver = receiverID;
        String date = getDate();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message_content", messageContent.trim());
        messageMap.put("sender", sender);
        messageMap.put("receiver", receiver);
        messageMap.put("date", date);
        messageMap.put("message_image", uri.toString());
        reference.child("chats").child(chatID).child("messages").push().setValue(messageMap);

        updateLatestMessageAndDate(messageContent, date);

        finish();
    }
    private void updateLatestMessageAndDate(String messageContent, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> latestMessageAndDateMap = new HashMap<>();
        latestMessageAndDateMap.put("latest_message", messageContent);
        latestMessageAndDateMap.put("latest_message_date", date);

        reference.child("chats").child(chatID).child("latest_message_and_date").updateChildren(latestMessageAndDateMap);
    }


    private String getDate() {
        Date c = Calendar.getInstance().getTime();
        Log.d(TAG, "onCreate: " + c);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.getDefault());
        String formattedDate = df.format(c);
        Log.d(TAG, "onCreate: " + formattedDate);
        return formattedDate;
    }

}
