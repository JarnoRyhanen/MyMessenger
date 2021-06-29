package com.home.mymessenger;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.data.ContactData;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.dp.RealmHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        ContactData contact = realm.where(ContactData.class).equalTo("contactPhoneNumber", getTag().trim()).findFirst();
        if (contact != null) {
            Log.d(TAG, "performUserQuery: " + contact.getContactName() + " " + contact.getContactPhoneNumber());
//
//            DatabaseReference userRef = ref.child("user_specific_info").child(user.getUid()).child("contacts");
//            Map<String, Object> contactsMap = new HashMap<>();
//            contactsMap.put(contact.getContactID(), contact.getContactName());
//            userRef.updateChildren(contactsMap);
//
//            fireBaseDBHelper.addUserToChats(contact.getContactName(), contact.getContactID());
            updateContactInbox(contact);
        }

    }

    private void updateContactInbox(ContactData contact) {
        DatabaseReference inboxRef = ref.child("user_inbox").child(contact.getContactID()).child(UUID.randomUUID().toString());

        Map<String, Object> inboxMap = new HashMap<>();
        inboxMap.put("senderID", user.getUid());
        inboxMap.put("message_content", String.format("Do you want to accept chatting offer from %s", user.getDisplayName()));
        inboxMap.put("chatID", UUID.randomUUID().toString());

        inboxRef.updateChildren(inboxMap);
    }
}
