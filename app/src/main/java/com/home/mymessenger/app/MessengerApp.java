package com.home.mymessenger.app;

import android.app.Application;
import android.util.Log;

import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;

public class MessengerApp extends Application {

    private final static String TAG = "MessengerApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: app started");

        RealmHelper.init(this);
        FireBaseDBHelper.init();
    }
}
