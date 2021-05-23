package com.home.mymessenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.home.mymessenger.mainactivity.MainActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final int RC_LOG_IN = 100;
    private static final String TAG = "SignInActivity";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final Realm realm = RealmHelper.getInstance().getRealm();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                createSignInIntent();
            } else {
                Log.d(TAG, "onCreate: user exists   " + user.getDisplayName() + "     " + user.getEmail());
                startMainActivity();
            }
        }
    }

    public void createSignInIntent() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {

            FireBaseDBHelper.getInstance().listerForUserChatChange();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userID = user.getUid();
            String userName = user.getDisplayName();

            UserData userData = realm.where(UserData.class).equalTo("userID", userID).findFirst();

            Log.d(TAG, "onActivityResult:  user id: " + userID + " , username: " + userName);

            DatabaseReference userSpecificInfoRef = ref.child("user_specific_info").child(userID);

            Map<String, Object> userSpecificInfoObjectMap = new HashMap<>();
            userSpecificInfoObjectMap.put("user_name", userName);
            try {
                userSpecificInfoObjectMap.put("current_status", userData.getUserStatus());
                userSpecificInfoObjectMap.put("profile_picture", userData.getUserProfilePicture());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            userSpecificInfoRef.updateChildren(userSpecificInfoObjectMap);

            DatabaseReference userRef = ref.child("users");

            Map<String, Object> userObjectMap = new HashMap<>();
            userObjectMap.put(userID, userName);
            userRef.updateChildren(userObjectMap);

            startMainActivity();

        } else {
            Log.d(TAG, "onActivityResult: sign in cancelled");
        }
    }

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
