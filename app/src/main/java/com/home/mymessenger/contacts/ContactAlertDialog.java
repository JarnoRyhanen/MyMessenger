package com.home.mymessenger.contacts;

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
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ContactData;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.Realm;


public class ContactAlertDialog extends AppCompatDialogFragment {
    private static final String TAG = "CustomDialog";
    private TextView textView;

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FireBaseDBHelper fireBaseDBHelper = new FireBaseDBHelper();

    private final List<String> contactIDList = new ArrayList<>();

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
                    isUserInContacts();
                });
        textView = view.findViewById(R.id.layout_dialog_text_view);
        textView.setText(String.format("Do you want to start a chat with %s", getTag()));

        return builder.create();
    }

    private void isUserInContacts() {
        DatabaseReference reference = ref.child(getResources().getString(R.string.user_specific_info))
                .child(user.getUid())
                .child(getResources().getString(R.string.contacts));

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    ContactData contactData = realm.where(ContactData.class).equalTo("contactPhoneNumber", getTag().trim()).findFirst();
                    if (contactData != null) {
                        String contactID = contactData.getContactID();
                        Map<String, Object> contactsMap = (Map<String, Object>) snapshot.getValue();

                        contactIDList.addAll(contactsMap.keySet());

                        if (contactIDList.contains(contactID)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.you_already_have_this_user),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "friend request sent", Toast.LENGTH_SHORT).show();
                            performUserQuery();
                        }

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void performUserQuery() {
        ContactData contact = realm.where(ContactData.class).equalTo("contactPhoneNumber", getTag().trim()).findFirst();
        if (contact != null) {
            Log.d(TAG, "performUserQuery: " + contact.getContactName() + " " + contact.getContactPhoneNumber());

            String chatID = UUID.randomUUID().toString();

            addToChats(contact, chatID);
            updateContactInbox(contact, chatID);
        }
    }

    private void addToChats(ContactData contact, String chatID) {
        DatabaseReference userRef = ref.child(getResources().getString(R.string.user_specific_info))
                .child(user.getUid())
                .child(getResources().getString(R.string.contacts));

        Map<String, Object> contactsMap = new HashMap<>();
        contactsMap.put(contact.getContactID(), contact.getContactName());
        userRef.updateChildren(contactsMap);

        fireBaseDBHelper.addUserToChats(contact.getContactName(), contact.getContactID(), chatID, getActivity());
    }

    private void updateContactInbox(ContactData contact, String chatID) {
        UserData userData = realm.where(UserData.class).equalTo("userID", user.getUid()).findFirst();
        DatabaseReference inboxRef = ref.child(getResources().getString(R.string.user_inbox))
                .child(contact.getContactID())
                .child(UUID.randomUUID().toString());

        Map<String, Object> inboxMap = new HashMap<>();
        inboxMap.put(getResources().getString(R.string.senderID), user.getUid());
        inboxMap.put(getResources().getString(R.string.message_content), String.format("Do you want to accept chatting offer from %s", user.getDisplayName()));
        inboxMap.put(getResources().getString(R.string.chatID), chatID);
        inboxMap.put(getResources().getString(R.string.sender_name), user.getDisplayName());
        inboxMap.put(getResources().getString(R.string.sender_profile_pic), userData.getUserProfilePicture());

        inboxRef.updateChildren(inboxMap);
    }
}
