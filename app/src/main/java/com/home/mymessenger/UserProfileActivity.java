package com.home.mymessenger;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.home.mymessenger.fragments.ChangeStatusFragment;
import com.home.mymessenger.fragments.UserProfileFragment;

public class UserProfileActivity extends AppCompatActivity implements ChangeStatusFragment.ChangeStatusFragmentListener {

    private static final String TAG = "UserProfileActivity";
    private ChangeStatusFragment changeStatusFragment;
    private UserProfileFragment userProfileFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        if (savedInstanceState == null) {

            userProfileFragment = new UserProfileFragment();
            openFragment();
        }
    }

    private void openFragment() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.user_profile_activity_container, UserProfileFragment.class, null)
                .commit();
    }

    @Override
    public void onStatusChanged(CharSequence status) {
        Log.d(TAG, "onStatusChanged: " + status);

//        userProfileFragment.updateStatus(status);
    }

}
