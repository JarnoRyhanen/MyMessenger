package com.home.mymessenger.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
    public static final int DELETE_CHAT = 1;
    private static final String TAG = "MainActivity";

    private RecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final FireBaseDBHelper helper = FireBaseDBHelper.getInstance();

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final List<ChatData> chatDataList = new ArrayList<>();

    private boolean isActive = true;
    private boolean isRan = false;

    private int mPosition;

    //TODO  CONVERT ALL STRINGS TO RESOURCES

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildRecyclerView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        floatingActionButton = findViewById(R.id.fab);
        if (!isRan && isNetworkConnected()) {
            startFireBaseListening();
            isRan = true;
        } else {
            updateContent();
        }
    }

    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.main_activity_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerAdapter(MainActivity.this, isActive);
        recyclerView.setAdapter(adapter);

        adapter.setOnTouchListener(position -> mPosition = position);
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
        reference.child(getResources().getString(R.string.user_chats))
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
        adapter.clear();

        RealmResults<ChatData> data = realm.where(ChatData.class).findAll();
        for(ChatData chatData : data){
            adapter.add(chatData);
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "updateContent: called");
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
        }
    }

    private void updateUserActivityStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> activityStatusMap = new HashMap<>();
        activityStatusMap.put(getResources().getString(R.string.activity_status), status);
        reference.child(getResources().getString(R.string.user_specific_info))
                .child(user.getUid())
                .updateChildren(activityStatusMap);
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
        updateContent();
    }

    @Override
    public void onLatestMessageAdded() {
        updateContent();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == DELETE_CHAT) {
            openDialog();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void openDialog() {
        DeleteChatDialogFragment deleteChatDialogFragment = new DeleteChatDialogFragment(adapter, mPosition);
        deleteChatDialogFragment.show(getSupportFragmentManager(), "delete chat dialog");
    }
}
