package com.home.mymessenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignInActivity extends Activity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "SignInActivity";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            createSignInIntent();
        } else {
            Log.d(TAG, "onCreate: user exists   " + user.getDisplayName() + "     " + user.getEmail());
            startMainActivity();
        }
    }

    public void createSignInIntent() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                DatabaseReference databaseReference = ref.child("users");

                Map<String, Object> userMap = new HashMap<>();

                Map<String, Object> userObjectMap = new HashMap<>();
                userObjectMap.put("user_name", user.getDisplayName());
                userObjectMap.put("current_status", "placeholder status");
                userObjectMap.put("profile_picture", "placeholder profile picture");

                userMap.put(user.getUid(), userObjectMap);
                databaseReference.updateChildren(userMap);

                startMainActivity();

            } else {
                Log.d(TAG, "onActivityResult: sign in cancelled");
            }
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
