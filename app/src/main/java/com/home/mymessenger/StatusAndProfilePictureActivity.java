package com.home.mymessenger;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class StatusAndProfilePictureActivity extends AppCompatActivity {

    private TextInputLayout statusText;
    private ShapeableImageView profilePicture;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_and_profile_picture_activity);

        profilePicture = findViewById(R.id.sign_in_profile_picture);
        statusText = findViewById(R.id.sign_in_status);
        floatingActionButton = findViewById(R.id.sign_in_fab);

    }

    public void onClick(View view) {
    }
}
