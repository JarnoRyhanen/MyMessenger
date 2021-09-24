package com.home.mymessenger.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.home.mymessenger.R;
import com.home.mymessenger.data.ChatData;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;
import com.home.mymessenger.messaging.PrivateMessageScreen;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ChatViewHolder> {

    public static final int DELETE_CHAT = 1;
    private final static String TAG = "RecyclerAdapter";
    private final Context context;
    protected final List<ChatData> chatList = new ArrayList<>();
    private boolean isActive;

    String chatID;

    private UserData userData;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final Realm realm = RealmHelper.getInstance().getRealm();


    public RecyclerAdapter(Context context, boolean isActive) {
        this.context = context;
        this.isActive = isActive;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.main_activity_recycler_list_row, parent, false), onTouchListener);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        ChatData data = chatList.get(position);
        userData = realm.where(UserData.class).equalTo("userID", data.getReceiverID()).findFirst();

        holder.chatID = data.getChatID();
        chatID = holder.chatID;
        holder.userName.setText(userData.getUserName());
//        holder.date.setText(data.getLatestActive());
//        holder.latestMessage.setText(data.getLatestMessage());

        lastMessage(holder.latestMessage, holder.date);

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

    public void delete(int position){
        chatList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(ChatData data) {
        chatList.add(data);
    }

    public void clear() {
        chatList.clear();
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private OnTouchListener onTouchListener;

    public interface OnTouchListener {
        void onTouch(int position);
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.onTouchListener = listener;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public String chatID;
        public TextView date;
        public TextView userName;
        public TextView latestMessage;
        public ShapeableImageView image;
        public ShapeableImageView online;
        public ShapeableImageView offline;


        public ChatViewHolder(@NonNull View itemView, OnTouchListener listener) {
            super(itemView);
            itemView.setOnClickListener(onRowClick);
            itemView.setOnCreateContextMenuListener(this);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTouch(position);
                        }
                    }
                    return false;
                }
            });

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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Delete this chat?");
            menu.add(this.getBindingAdapterPosition(), DELETE_CHAT, 0, "Delete chat");
        }
    }

    private String lastMessageString;

    private void lastMessage(TextView latestMessage, TextView latestMessageDate) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(context.getResources().getString(R.string.user_chats))
                .child(user.getUid())
                .child(chatID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            final Object snapShotValue = snapshot.getValue();

                            Map<String, Object> lastMessageDataMap = (Map<String, Object>) snapShotValue;
                            lastMessageString = (String) lastMessageDataMap.get(context.getResources().getString(R.string.latest_message));
                            latestMessage.setText(lastMessageString);

                            String lastMsgDate = (String) lastMessageDataMap.get(context.getResources().getString(R.string.latest_message_date));
                    latestMessageDate.setText(lastMsgDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
