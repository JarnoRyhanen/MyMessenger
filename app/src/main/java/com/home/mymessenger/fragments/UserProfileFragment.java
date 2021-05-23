package com.home.mymessenger.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.home.mymessenger.R;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment{

    private final static String TAG = "UserProfileActivity";
    private static final int PICK_IMAGE = 100;

    private ShapeableImageView profilePicture;
    private TextInputLayout userNameLayout;
    private TextInputLayout statusTextLayout;
    private TextInputEditText userNameEditText;
    private TextInputEditText statusEditText;
    private FloatingActionButton floatingActionButton;

    private Uri imageUri;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = storage.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final Realm realm = RealmHelper.getInstance().getRealm();

    private UserData userData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);

//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        profilePicture = view.findViewById(R.id.user_profile_fragment_profile_picture);
        userNameLayout = view.findViewById(R.id.user_profile_fragment_user_name_layout);
        userNameEditText = view.findViewById(R.id.user_profile_fragment_user_name_edit_text);
        statusTextLayout = view.findViewById(R.id.user_profile_fragment_status);
        statusEditText = view.findViewById(R.id.user_profile_fragment_status_edit_text);
        floatingActionButton = view.findViewById(R.id.user_profile_activity_fab);

        setOnClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserStatusAndUserName();
        loadImage();
    }

    private void loadUserStatusAndUserName() {
        Log.d(TAG, "updateUserStatus: e");
        if (user != null) {
            userNameEditText.setText(user.getDisplayName());
            userData = realm.where(UserData.class).equalTo("userID", user.getUid()).findFirst();
            if (userData != null) {
                String status = userData.getUserStatus();
                Log.d(TAG, "updateUserStatus new status is: " + status);
                statusEditText.setText(status);
            }
        }
    }

    public void updateStatus(CharSequence newStatus) {
        try {
            statusEditText.setText(newStatus);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setOnClickListeners() {
        floatingActionButton.setOnClickListener(onClickListener);
        userNameEditText.setOnClickListener(onClickListener);
        statusEditText.setOnClickListener(onClickListener);
    }

    private void loadImage() {
//        DatabaseReference getImage = ref.child("users").child(user.getUid()).child("profile_picture");

//        userData.getUserProfilePicture();
        Picasso.get().load(userData.getUserProfilePicture()).into(profilePicture);
//        getImage.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String link = snapshot.getValue(String.class);
//                Log.d(TAG, "onDataChange: " + link);
//                if (link != null) {
//                    Picasso.get().load(link).into(profilePicture);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(), "Error Loading Image", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void openImageGallery() {
        Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imageGalleryIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            String imageID = UUID.randomUUID().toString();

            StorageReference strReference = storageReference.child("images/" + imageID);

            strReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        downloadImage(imageID);
                    }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Profile picture changed successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateProfilePicture(Uri uri) {
        if (user != null) {
            DatabaseReference databaseReference = ref.child("user_specific_info").child(user.getUid());

            Map<String, Object> userObjectMap = new HashMap<>();
            userObjectMap.put("profile_picture", uri.toString());
            databaseReference.updateChildren(userObjectMap);
        }
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == floatingActionButton) {
                openImageGallery();
            } else if (v == userNameEditText) {
                replaceFragment(new ChangeUserNameFragment());
            } else if (v == statusEditText) {
                replaceFragment(new ChangeStatusFragment());
            }
        }
    };

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.replace(R.id.user_profile_activity_container, fragment);
        transaction.addToBackStack(null);
        transaction.setReorderingAllowed(true);
        transaction.commit();
    }

}
