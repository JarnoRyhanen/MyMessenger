package com.home.mymessenger.app;

import android.app.Application;
import android.util.Log;

public class MessengerApp extends Application {

    private final static String TAG = "MessengerApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: app started");


    }
}
