package com.home.mymessenger.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.mymessenger.IntentKeys;
import com.home.mymessenger.messaging.PrivateMessageScreen;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ChatViewHolder> {

    private final static String TAG = "RecyclerAdapter";
    private final Context context;
    private final List<ChatData> list;
    private boolean isActive;

    String chatID;

    private UserData userData;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final Realm realm = RealmHelper.getInstance().getRealm();

    public RecyclerAdapter(Context context, List<ChatData> list, boolean isActive) {
        this.context = context;
        this.list = list;
        this.isActive = isActive;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.main_activity_recycler_list_row, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        ChatData data = list.get(position);

        Log.d(TAG, "onBindViewHolder: " + data.getLatestMessage());
        holder.chatID = data.getChatID();
        chatID = holder.chatID;
        holder.userName.setText(data.getReceiver());
        holder.date.setText(data.getLatestActive());
        holder.latestMessage.setText(data.getLatestMessage());

        lastMessage(holder.latestMessage, holder.date);

        userData = realm.where(UserData.class).equalTo("userID", data.getReceiverID()).findFirst();
        if (userData != null) {
            Picasso.get().load(userData.getUserProfilePicture()).into(holder.image);

            if (isActive) {
                if (userData.getActivityStatus() != null && userData.getActivityStatus().equals("online")) {
                    holder.online.setVisibility(View.VISIBLE);
                    holder.offline.setVisibility(View.INVISIBLE);
                } else {
                    holder.online.setVisibility(View.INVISIBLE);
                    holder.offline.setVisibility(View.VISIBLE);
                }
            } else {
                holder.online.setVisibility(View.INVISIBLE);
                holder.offline.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void add(ChatData data) {
        list.add(data);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        public String chatID;
        public TextView date;
        public TextView userName;
        public TextView latestMessage;
        public ShapeableImageView image;
        public ShapeableImageView online;
        public ShapeableImageView offline;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(onRowClick);

            date = itemView.findViewById(R.id.latest_active_date);
            userName = itemView.findViewById(R.id.user_name);
            latestMessage = itemView.findViewById(R.id.latest_message);
            image = itemView.findViewById(R.id.profile_icon);
            online = itemView.findViewById(R.id.online_image);
            offline = itemView.findViewById(R.id.offline_image);
        }

        private final View.OnClickListener onRowClick = view -> {
            Context ctx = itemView.getContext();
            Intent intent = new Intent(ctx, PrivateMessageScreen.class);
            Log.d(TAG, "chat id: " + chatID);
            intent.putExtra(IntentKeys.CHAT_ID, chatID);
            ctx.startActivity(intent);
        };
    }

    private String lastMessageString;

    private void lastMessage(TextView latestMessage, TextView latestMessageDate) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("user_chats").child(user.getUid()).child(chatID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    final Object snapShotValue = snapshot.getValue();

                    Map<String, Object> lastMessageDataMap = (Map<String, Object>) snapShotValue;
                    lastMessageString = (String) lastMessageDataMap.get("latest_message");
                    latestMessage.setText(lastMessageString);

                    String lastMsgDate = (String) lastMessageDataMap.get("latest_message_date");
                    latestMessageDate.setText(lastMsgDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
