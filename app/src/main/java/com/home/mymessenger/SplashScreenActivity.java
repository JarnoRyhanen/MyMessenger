package com.home.mymessenger;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.home.mymessenger.loginsignin.LogInActivity;
import com.home.mymessenger.mainactivity.MainActivity;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen splashScreen = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withSplashTimeOut(1500)
                .withBackgroundColor(Color.parseColor("#FFFFFFFF"))
                .withLogo(R.drawable.message_icon);


        if (user == null) {
            splashScreen.withTargetActivity(LogInActivity.class);
        } else {
            splashScreen.withTargetActivity(MainActivity.class);
        }

        View easySplashScreen = splashScreen.create();
        setContentView(easySplashScreen);
    }
}
