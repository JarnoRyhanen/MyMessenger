package com.home.mymessenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.home.mymessenger.mainactivity.MainActivity;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private FirebaseAuth auth;

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private TextInputLayout textInputEmail;
    private TextInputLayout textInputUserName;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputPhoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.sign_in_activity_layout);

            textInputEmail = findViewById(R.id.text_input_email);
            textInputUserName = findViewById(R.id.text_input_user_name);
            textInputPassword = findViewById(R.id.text_input_password);
            textInputPhoneNumber = findViewById(R.id.text_input_phone_number);
//                createSignInIntent();
            auth = FirebaseAuth.getInstance();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        Log.d(TAG, "onStart: " + currentUser);
        if (currentUser != null) {
            startMainActivity();
        }
    }

    public void onButtonClick(View view) {
        registerUser();
    }

    private boolean validateTextInput(TextInputLayout... textInputLayouts) {
        String textInput = textInputLayouts[0].getEditText().getText().toString().trim();
        if (textInput.isEmpty()) {
            textInputLayouts[0].setError("Field can't be empty");
            return false;
        } else if (textInputLayouts[0] == textInputEmail && !Patterns.EMAIL_ADDRESS.matcher(textInput).matches()) {
            Log.d(TAG, "validateTextInput: " + textInputLayouts[0]);
            textInputLayouts[0].setError("Please enter a valid email address");
            return false;
        } else {
            textInputLayouts[0].setError(null);
            return true;
        }
    }

    private void registerUser() {
        String userName = textInputUserName.getEditText().getText().toString().trim();
        String email = textInputEmail.getEditText().getText().toString().trim();
        String password = textInputPassword.getEditText().getText().toString().trim();
        String phoneNumber = textInputPhoneNumber.getEditText().getText().toString().trim();

        if (!validateTextInput(textInputUserName) | !validateTextInput(textInputEmail) |
                !validateTextInput(textInputPassword) | !validateTextInput(textInputPhoneNumber)) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addUserToDatabase(userName, phoneNumber);
                    } else {
                        Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUserToDatabase(String userName, String phoneNumber) {
        FireBaseDBHelper.getInstance().listerForUserChatChange();
        FirebaseUser user = auth.getCurrentUser();

        UserData userData = new UserData();
        userData.setUserName(userName);
        userData.setUserPhoneNumber(phoneNumber);

        Map<String, Object> userObjectMap = new HashMap<>();
        userObjectMap.put("user_name", userName);
        userObjectMap.put("phone_number", phoneNumber);
        userObjectMap.put("current_status", "test");


        DatabaseReference userRef = ref.child("users");
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(user.getUid(), userName);
        userRef.updateChildren(userMap);

        DatabaseReference userSpecificInfoRef = ref.child("user_specific_info").child(user.getUid());
        userSpecificInfoRef.setValue(userObjectMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                setFirebaseUserName(userName);
                startMainActivity();
            } else {
                Log.e(TAG, "onComplete: " + task.getException().getMessage(), null);
            }
        });
//        String userID = user.getUid();
//        String userName = user.getDisplayName();
//
//        UserData userData = realm.where(UserData.class).equalTo("userID", userID).findFirst();
//
//        Log.d(TAG, "onActivityResult:  user id: " + userID + " , username: " + userName);
//
//        DatabaseReference userSpecificInfoRef = ref.child("user_specific_info").child(userID);
//
//        Map<String, Object> userSpecificInfoObjectMap = new HashMap<>();
//        userSpecificInfoObjectMap.put("user_name", userName);
//        try {
//            userSpecificInfoObjectMap.put("current_status", userData.getUserStatus());
//            userSpecificInfoObjectMap.put("profile_picture", userData.getUserProfilePicture());
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//        userSpecificInfoRef.updateChildren(userSpecificInfoObjectMap);
//
//        DatabaseReference userRef = ref.child("users");
//
//        Map<String, Object> userObjectMap = new HashMap<>();
//        userObjectMap.put(userID, userName);
//        userRef.updateChildren(userObjectMap);

//        startMainActivity();

    }

    private void setFirebaseUserName(String userName) {
        FirebaseUser user = auth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
//
//
//        } else {
//            Log.d(TAG, "onActivityResult: sign in cancelled");
//        }
//    }

    public void signOut() {
        AuthUI.getInstance().delete(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //todo do something when the user signs out
            }
        });
    }

    private void startMainActivity() {
        FireBaseDBHelper.getInstance().setListener(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });
        FireBaseDBHelper.getInstance().listerForUserChatChange();
    }
}
