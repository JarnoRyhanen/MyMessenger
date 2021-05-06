package com.home.mymessenger;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class UserProfileActivity extends AppCompatActivity {

    private final static String TAG = "UserProfileActivity";

    private ShapeableImageView profilePicture;
    private TextInputLayout userNameText;
    private TextInputLayout statusText;

//    private String userName = userNameText.getEditText().getText().toString();
//    private String status = statusText.getEditText().getText().toString();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        profilePicture = findViewById(R.id.user_profile_activity_profile_picture);
        userNameText = findViewById(R.id.user_profile_activity_user_name_layout);
        statusText = findViewById(R.id.user_profile_activity_status);


    }


    public void onClick(View view) {
        Log.d(TAG, "onClick: " + userNameText.getEditText().getText() + " " + statusText.getEditText().getText());
    }
}
