 package com.home.mymessenger.userProfile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;
import com.home.mymessenger.data.InboxData;
import com.home.mymessenger.dp.RealmHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class InboxRecyclerViewAdapter extends RecyclerView.Adapter<InboxRecyclerViewAdapter.InboxViewHolder> {

    private static final String TAG = "InboxRecyclerViewAdapte";
    private final Context context;
    private final List<InboxData> itemList;

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public InboxRecyclerViewAdapter(Context context, List<InboxData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InboxViewHolder(LayoutInflater.from(context).inflate(R.layout.inbox_recycler_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {

        InboxData inboxData = itemList.get(position);

        holder.textView.setText(inboxData.getMessage());

        holder.cancel.setOnClickListener(v -> {
            Log.d(TAG, "onClick: cancel pressed on " + inboxData.getMessage());

            deleteItem(position);
            deleteItemFromFireBase(inboxData.getMessageID());
            deleteItemFromRealm(inboxData.getMessageID());
        });

        holder.check.setOnClickListener(v -> {
            DatabaseReference userChatRef = ref.child(context.getResources().getString(R.string.user_chats))
                    .child(user.getUid())
                    .child(inboxData.getChatID());

            Map<String, Object> userChatMap = new HashMap<>();
            userChatMap.put(context.getResources().getString(R.string.receiverID), inboxData.getSenderID());
            userChatMap.put(context.getResources().getString(R.string.receiver), inboxData.getSenderName());
            userChatMap.put(context.getResources().getString(R.string.user_profile_pic), inboxData.getSenderProfilePic());
            userChatRef.updateChildren(userChatMap);

            DatabaseReference chatRef = database.getReference(context.getResources().getString(R.string.chats))
                    .child(inboxData.getChatID())
                    .child(context.getResources().getString(R.string.users));

            Map<String, Object> chatMap = new HashMap<>();
            chatMap.put(user.getUid(), user.getDisplayName());
            chatRef.updateChildren(chatMap);

            deleteItem(position);
            deleteItemFromFireBase(inboxData.getMessageID());
            deleteItemFromRealm(inboxData.getMessageID());
            addToContacts(inboxData.getSenderID(), inboxData.getSenderName());
        });

    }

    private void addToContacts(String senderID, String senderName) {
        DatabaseReference userRef = ref.child(context.getResources().getString(R.string.user_specific_info))
                .child(user.getUid())
                .child(context.getResources().getString(R.string.contacts));

        Map<String, Object> contactsMap = new HashMap<>();
        contactsMap.put(senderID, senderName);
        userRef.updateChildren(contactsMap);
    }

    private void deleteItemFromRealm(String messageID) {
        realm.executeTransaction(realm1 -> {
            InboxData inboxData = realm1.where(InboxData.class).equalTo("messageID", messageID).findFirst();
            inboxData.deleteFromRealm();
        });
    }

    private void deleteItemFromFireBase(String messageID) {
        DatabaseReference inboxRef = ref.child(context.getResources().getString(R.string.user_inbox))
                .child(user.getUid())
                .child(messageID);
        inboxRef.removeValue();
    }

    private void deleteItem(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class InboxViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public ImageButton check;
        public ImageButton cancel;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.inbox_recycler_list_row_text_view);
            check = itemView.findViewById(R.id.inbox_recycler_list_row_img_btn_check);
            cancel = itemView.findViewById(R.id.inbox_recycler_list_row_img_btn_cancel);

        }
    }
}
