package com.home.mymessenger.userProfile;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.home.mymessenger.R;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.userProfile.ChangeStatusFragment;
import com.home.mymessenger.userProfile.UserProfileFragment;

public class UserProfileActivity extends AppCompatActivity{

    private static final String TAG = "UserProfileActivity";
    private ChangeStatusFragment changeStatusFragment;
    private UserProfileFragment userProfileFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        if (savedInstanceState == null) {

            userProfileFragment = new UserProfileFragment();
            startFireBaseListening();
            openFragment();
        }
    }

    private void startFireBaseListening() {
        FireBaseDBHelper helper = FireBaseDBHelper.getInstance();
        helper.listenForUserChange();
    }

    private void openFragment() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.user_profile_activity_container, UserProfileFragment.class, null)
                .commit();
    }
}
