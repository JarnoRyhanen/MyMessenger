package com.home.mymessenger;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen splashScreen = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(SignInActivity.class)
                .withSplashTimeOut(3000)
                .withHeaderText("header")
                .withFooterText("footer")
                .withBeforeLogoText("Before logo")
                .withAfterLogoText("after logo")
                .withBackgroundColor(Color.parseColor("#1a1b29"))
                .withLogo(R.drawable.download);


        View easySplashScreen = splashScreen.create();
        setContentView(easySplashScreen);
    }
}
