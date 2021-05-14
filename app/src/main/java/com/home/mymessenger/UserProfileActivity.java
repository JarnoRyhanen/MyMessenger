package com.home.mymessenger;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.home.mymessenger.fragments.UserProfileFragment;

public class UserProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);
        if (savedInstanceState == null) {

            openFragment();
        }

    }

    private void openFragment() {
        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .setReorderingAllowed(true)
                .add(R.id.user_profile_activity_container, UserProfileFragment.class, null)
                .commit();
    }

}
