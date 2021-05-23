package com.home.mymessenger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;


public class CustomDialog extends AppCompatDialogFragment {
    private static final String TAG = "CustomDialog";
    private TextView textView;

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private UserData userData;

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view)
                .setTitle("")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<UserData> userData = realm.where(UserData.class).equalTo("userName", getTag()).findAll();
                        for (UserData id : userData) {

                            if (userData.isEmpty()) {
                                //user has the app but is not a contact
                                Toast.makeText(getContext(), "user has the app but is not a contact", Toast.LENGTH_SHORT).show();

                            }
                            if (realm.where(UserData.class).equalTo("userID", id.getUserID()).findFirst().getUserID().equals(id.getUserID())) {
                                //user has the app and is already a contact
                                Log.d(TAG, "onClick: ids matcg");
                                performUserQuery();
                                //todo open chat screen with this user
                            } else {
                                Log.d(TAG, "onClick: ");
                                //todo check if the user has profile, if no, show toast that says that the user doesn't have the app
                            }
                        }

                    }
                });
        textView = view.findViewById(R.id.layout_dialog_text_view);
        textView.setText(String.format("Do you want to start a chat with %s", getTag()));

        return builder.create();
    }

    private void performUserQuery() {
        Log.d(TAG, "searchForUsers:" + getTag().toString());

        Query userSearchQuery = FirebaseDatabase.getInstance().getReference().
                child("users")
                .orderByValue()
                .startAt(getTag())
                .endAt(getTag());

        userSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                if (userMap != null) {
                    Log.d(TAG, "onDataChange: Users found. Here's the data: " + snapshot.toString());
                    addUserToContacts(userMap);
                } else {
                    Log.d(TAG, "onDataChange: this person does not have this app");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });
    }

    private void addUserToContacts(Map<String, Object> userMap) {
        if (user != null) {
            String userName = "";
            String userID = "";
            for (String id : userMap.keySet()) {
                userName = (String) userMap.get(id);
                userID = id;
                DatabaseReference userRef = ref.child("user_specific_info").child(user.getUid()).child("contacts");
                Map<String, Object> contactsMap = new HashMap<>();
                contactsMap.put(id, userName);
                userRef.updateChildren(contactsMap);
            }
            addUserToChats(userName, userID);
        }
    }


    private void addUserToChats(String userName, String userID) {
        DatabaseReference databaseReference = database.getReference("user_specific_info").child(userID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Object pic = snapshot.getValue();
                final Map<String, Object> picMap = (Map<String, Object>) pic;

                Log.d(TAG, "onData change" + picMap.get("profile_picture"));
                String pictureUrl = (String) picMap.get("profile_picture");

                DatabaseReference userRef = ref.child("user_chats")
                        .child(user.getUid())
                        .child(UUID.randomUUID().toString());
                Map<String, Object> userChatMap = new HashMap<>();
                userChatMap.put("user_name", userName);
                userChatMap.put("user_profile_pic", pictureUrl);
                userRef.updateChildren(userChatMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
