package com.home.mymessenger.userProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.home.mymessenger.data.InboxData;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment {

    private final static String TAG = "UserProfileActivity";
    private static final int PICK_IMAGE = 100;

    private ShapeableImageView profilePicture;
    private TextInputLayout userNameLayout;
    private TextInputLayout statusTextLayout;
    private TextInputEditText userNameEditText;
    private TextInputEditText statusEditText;
    private FloatingActionButton floatingActionButton;

    private RecyclerView inboxRecycler;
    private InboxRecyclerViewAdapter adapter;

    private Uri imageUri;

    private final FireBaseDBHelper helper = FireBaseDBHelper.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = storage.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final Realm realm = RealmHelper.getInstance().getRealm();

    private UserData userData;
    private Button signOutButton;

    private List<InboxData> inboxDataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);

        userData = realm.where(UserData.class).equalTo("userID", user.getUid()).findFirst();

        profilePicture = view.findViewById(R.id.user_profile_fragment_profile_picture);
        userNameLayout = view.findViewById(R.id.user_profile_fragment_user_name_layout);
        userNameEditText = view.findViewById(R.id.user_profile_fragment_user_name_edit_text);
        statusTextLayout = view.findViewById(R.id.user_profile_fragment_status);
        statusEditText = view.findViewById(R.id.user_profile_fragment_status_edit_text);
        floatingActionButton = view.findViewById(R.id.user_profile_activity_fab);

        inboxRecycler = view.findViewById(R.id.user_profile_inbox_recycler);
        inboxRecycler.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        inboxRecycler.setLayoutManager(linearLayoutManager);

        signOutButton = view.findViewById(R.id.sign_out_button);

        helper.listerForInboxDataChange();

        setOnClickListeners();
        if (userData != null) {
            loadUserStatusAndUserName();
            loadImage();
            updateInbox();
        }
        return view;
    }

    private void updateInbox() {
        inboxDataList.clear();

        RealmResults<InboxData> inboxData = realm.where(InboxData.class).findAll();

        inboxDataList.addAll(inboxData);

        adapter = new InboxRecyclerViewAdapter(getActivity(), inboxDataList);
        inboxRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void loadUserStatusAndUserName() {
        if (user != null) {
            userNameEditText.setText(user.getDisplayName());
            if (userData != null) {
                String status = userData.getUserStatus();
                statusEditText.setText(status);
            }
        }
    }

    private void setOnClickListeners() {
        signOutButton.setOnClickListener(onClickListener);
        floatingActionButton.setOnClickListener(onClickListener);
        userNameEditText.setOnClickListener(onClickListener);
        statusEditText.setOnClickListener(onClickListener);
    }

    private void loadImage() {
        Picasso.get().load(userData.getUserProfilePicture()).fit().centerInside().into(profilePicture);
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
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            String imageID = UUID.randomUUID().toString();

            StorageReference strReference = storageReference.child(getResources().getString(R.string.users) + "/" + user.getUid()
                    + "/profile/" + imageID);

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
        StorageReference reference = storageReference.child(getResources().getString(R.string.users) + "/" + user.getUid() + "/profile/" + imageID);

        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "onSuccess: " + uri);
            updateProfilePicture(uri);
            Toast.makeText(getActivity(), "Profile picture changed successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateProfilePicture(Uri uri) {
        if (user != null) {
            DatabaseReference databaseReference = ref.child(getResources().getString(R.string.user_specific_info))
                    .child(user.getUid());

            Map<String, Object> userObjectMap = new HashMap<>();
            userObjectMap.put(getResources().getString(R.string.profile_picture), uri.toString());
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
            } else if (v == signOutButton) {
                openDialog();
            }
        }
    };

    private void openDialog() {
        SignOutDialogFragment signOutDialogFragment = new SignOutDialogFragment();
        signOutDialogFragment.show(getActivity().getSupportFragmentManager(), "sign out dialog");
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.replace(R.id.user_profile_activity_container, fragment);
        transaction.addToBackStack(null);
        transaction.setReorderingAllowed(true);
        transaction.commit();
    }

}
