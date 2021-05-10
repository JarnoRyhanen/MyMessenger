package com.home.mymessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UserProfileActivity extends AppCompatActivity {

    private final static String TAG = "UserProfileActivity";
    private static final int PICK_IMAGE = 100;

    private RelativeLayout parentLayout;

    private ShapeableImageView profilePicture;
    private TextInputLayout userNameText;
    private TextInputLayout statusText;
    private FloatingActionButton floatingActionButton;

    private Uri imageUri;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Realm realm = RealmHelper.getInstance().getRealm();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        profilePicture = findViewById(R.id.user_profile_activity_profile_picture);
        userNameText = findViewById(R.id.user_profile_activity_user_name_layout);
        statusText = findViewById(R.id.user_profile_activity_status);

        floatingActionButton = findViewById(R.id.user_profile_activity_fab);

        userNameText.getEditText().setText(user.getDisplayName());

        UserData userData = realm.where(UserData.class).equalTo("userName", user.getDisplayName()).findFirst();
        statusText.getEditText().setText(userData.getUserStatus());

        loadImage();
    }

    private void loadImage() {
        DatabaseReference getImage = ref.child("users").child(user.getUid()).child("profile_picture");

        getImage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String link = snapshot.getValue(String.class);
                Picasso.get().load(link).into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Error Loading Image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageGallery() {
        Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imageGalleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri);
            Log.d(TAG, "onActivityResult: " + imageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            String imageID = UUID.randomUUID().toString();

            StorageReference strReference = storageReference.child("images/" + imageID);

            strReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(UserProfileActivity.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        downloadImage(imageID);
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        }
    }

    private void downloadImage(String imageID) {
        StorageReference reference = storageReference.child("images/" + imageID);

        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "onSuccess: " + uri);
            updateProfilePicture(uri);
            Toast.makeText(UserProfileActivity.this, "Profile picture changed successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> e.printStackTrace());
    }

    private void updateProfilePicture(Uri uri) {
        DatabaseReference databaseReference = ref.child("users").child(user.getUid());

        Map<String, Object> userObjectMap = new HashMap<>();
        userObjectMap.put("profile_picture", uri.toString());
        databaseReference.updateChildren(userObjectMap);
    }

    public void onClick(View view) {
        if (view == floatingActionButton) {
            openImageGallery();
        } else {
            Log.d(TAG, "onClick: " + userNameText.getEditText().getText() + " " + statusText.getEditText().getText());
        }
    }
}
