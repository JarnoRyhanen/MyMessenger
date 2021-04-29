package com.home.mymessenger.dp;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {

    private static final String TAG = "RealmHelper";

    private static final String REALM_NAME = "testlibrary.realm";
    private static final int REALM_VERSION = RealmHistory.VERSION_1;

    private Realm realm;

    private static class RealmHistory {
        final static int VERSION_1 = 1;
    }

    private static RealmHelper instance;

    private RealmHelper() {

    }

    public static RealmHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(TAG + " is not initialized");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance != null) {
            throw new IllegalStateException(TAG + " is already initialized");
        }
        instance = new RealmHelper();

        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(REALM_NAME)
                .schemaVersion(REALM_VERSION)
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();
        instance.realm = Realm.getInstance(configuration);
    }

    public Realm getRealm() {
        return realm;
    }

}