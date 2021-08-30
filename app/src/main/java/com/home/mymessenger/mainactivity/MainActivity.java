package com.home.mymessenger.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.R;
import com.home.mymessenger.contacts.SearchForContactsActivity;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;
import com.home.mymessenger.userProfile.UserProfileActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements FireBaseDBHelper.onDatabaseUpdateListener, FireBaseDBHelper.onLatestMessageAddedListener {
    private static final int REQUEST_CALL = 1;
    private static final String TAG = "MainActivity";

    private RecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button button;
    private EditText editText;

    private final FireBaseDBHelper helper = FireBaseDBHelper.getInstance();

    private Realm realm = RealmHelper.getInstance().getRealm();

    private final List<ChatData> chatDataList = new ArrayList<>();

    private boolean isActive = true;
    private boolean isRan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.main_activity_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.d(TAG, "onCreate: internet connection: " + isNetworkConnected());
        Log.d(TAG, "onCreate: USER ID   " + user.getUid());
//        button = findViewById(R.id.soita);
//        editText = findViewById(R.id.numero);

        Log.d(TAG, "onCreate: CURRENT USER IS: " + user.getDisplayName());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        floatingActionButton = findViewById(R.id.fab);
        if (!isRan && isNetworkConnected()) {
            Log.d(TAG, "onCreate: i am called");
            startFireBaseListening();
            isRan = true;
        }else {
            updateContent();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void startFireBaseListening() {
        helper.setListener(this);
        helper.setOnLatestMessageAddedListener(this);
        helper.listenForUserSpecificInfoChange();
        helper.listerForUserChatChange();
        helper.listenToContactUsersDataChange();
        getUserChats();
    }

    private void getUserChats() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("user_chats").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        helper.listenForLatestMessage(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });
    }

    private void updateContent() {
        chatDataList.clear();

        RealmResults<ChatData> data = realm.where(ChatData.class).findAll();
        for(ChatData chatData : data){
            chatDataList.add(chatData);
            Log.d(TAG, "updateContent: " + chatData.getLatestMessage());
        }
        adapter = new RecyclerAdapter(MainActivity.this, chatDataList, isActive);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        if (view == floatingActionButton) {
            Intent intent = new Intent(this, SearchForContactsActivity.class);
            startActivity(intent);
        } else if (view == button) {
//            callPhone();
            Log.d(TAG, "onClick: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2");
        }
    }

    private void updateUserActivityStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> activityStatusMap = new HashMap<>();
        activityStatusMap.put("activity_status", status);
        reference.child("user_specific_info").child(user.getUid()).updateChildren(activityStatusMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        updateUserActivityStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + isRan);
//        updateUserActivityStatus("online");
    }


    @Override
    public void onDatabaseUpdate() {
        Log.d(TAG, "onDatabaseUpdate: mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
        updateContent();
    }

    @Override
    public void onLatestMessageAdded() {
        Log.d(TAG, "onLatestMessageAdded: NEW MESSAGE ADDED");
        updateContent();
    }
}
