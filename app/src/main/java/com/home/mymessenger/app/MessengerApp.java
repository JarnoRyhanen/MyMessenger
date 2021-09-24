package com.home.mymessenger.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;

public class MessengerApp extends Application {

    private final static String TAG = "MessengerApp";

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: app started");

        RealmHelper.init(this);
        FireBaseDBHelper.init();
        FireBaseDBHelper.getInstance().initContext(this);

        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
