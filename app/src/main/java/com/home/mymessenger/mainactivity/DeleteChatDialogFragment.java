package com.home.mymessenger.mainactivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.dp.RealmHelper;

import io.realm.Realm;

public class DeleteChatDialogFragment extends AppCompatDialogFragment {

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private RecyclerAdapter adapter;
    private int mPosition;


    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public DeleteChatDialogFragment(RecyclerAdapter adapter, int mPosition) {
        this.adapter = adapter;
        this.mPosition = mPosition;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete chat?");
        builder.setMessage("Are you sure you want to delete this chat?")
                .setPositiveButton("Yes", (dialog, id) -> delete())
                .setNegativeButton("No", (dialog, id) -> {
                });
        return builder.create();

    }

    private void delete() {
        String chatID = adapter.chatList.get(mPosition).getChatID();
        String contactID = adapter.chatList.get(mPosition).getReceiverID();

        deleteChat(chatID);
        deleteContact(contactID);

        deleteDataFromRealm(chatID);

        adapter.delete(mPosition);
    }

    private void deleteDataFromRealm(String chatID) {
        ChatData chatData = realm.where(ChatData.class).equalTo("chatID", chatID).findFirst();
        if (chatData != null) {
            realm.executeTransaction(realm1 -> chatData.deleteFromRealm());
        }
    }

    private void deleteChat(String chatID) {
        DatabaseReference deleteFromChatRef = FirebaseDatabase.getInstance().getReference()
                .child(getResources().getString(R.string.user_chats))
                .child(user.getUid())
                .child(chatID);

        deleteFromChatRef.removeValue();
    }

    private void deleteContact(String contactID) {
        DatabaseReference deleteFromContactRef = FirebaseDatabase.getInstance().getReference()
                .child(getResources().getString(R.string.user_specific_info))
                .child(user.getUid())
                .child(getResources().getString(R.string.contacts))
                .child(contactID);

        deleteFromContactRef.removeValue();
    }
}
