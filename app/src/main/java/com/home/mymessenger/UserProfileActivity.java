package com.home.mymessenger;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UserProfileActivity extends AppCompatActivity {

    private final static String TAG = "UserProfileActivity";

    private ShapeableImageView profilePicture;
    private TextInputLayout userNameText;
    private TextInputLayout statusText;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Realm realm = RealmHelper.getInstance().getRealm();
//    private String userName = userNameText.getEditText().getText().toString();
//    private String status = statusText.getEditText().getText().toString();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        profilePicture = findViewById(R.id.user_profile_activity_profile_picture);
        userNameText = findViewById(R.id.user_profile_activity_user_name_layout);
        statusText = findViewById(R.id.user_profile_activity_status);


        userNameText.getEditText().setText(user.getDisplayName());

        UserData userData = realm.where(UserData.class).equalTo("userName", user.getDisplayName()).findFirst();

        statusText.getEditText().setText(userData.getUserStatus());

    }


    public void onClick(View view) {
        Log.d(TAG, "onClick: " + userNameText.getEditText().getText() + " " + statusText.getEditText().getText());
    }
}
