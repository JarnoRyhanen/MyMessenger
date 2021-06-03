package com.home.mymessenger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;


public class CustomDialog extends AppCompatDialogFragment {
    private static final String TAG = "CustomDialog";
    private TextView textView;

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FireBaseDBHelper fireBaseDBHelper = new FireBaseDBHelper();

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view)
                .setTitle("")
                .setNegativeButton("No", (dialog, which) -> {

                })
                .setPositiveButton("Yes", (dialog, which) -> {
                    //todo check if the user already has the selected person in their chats
                    performUserQuery();
                });
        textView = view.findViewById(R.id.layout_dialog_text_view);
        textView.setText(String.format("Do you want to start a chat with %s", getTag()));

        return builder.create();
    }

    private void performUserQuery() {
        Log.d(TAG, "searchForUsers:" + getTag());

        String userName = getTag() != null ? getTag().trim() : null;
        Query userSearchQuery = FirebaseDatabase.getInstance().getReference().
                child("users")
                .orderByValue()
                .startAt(userName)
                .endAt(userName);
        userSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
                addUserToContacts(userMap);
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
            fireBaseDBHelper.addUserToChats(userName, userID);
        }
    }
}