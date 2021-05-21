package com.home.mymessenger.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.home.mymessenger.IntentKeys;
import com.home.mymessenger.PrivateMessageScreen;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ChatData;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ChatViewHolder> {

    private final static String TAG = "RecyclerAdapter";
    private final Context context;
    private final List<ChatData> list = new ArrayList<>();

    public RecyclerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.main_activity_recycler_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        ChatData data = list.get(position);
        holder.chatID = data.getChatID();
        holder.userName.setText(data.getUserName());
        holder.date.setText(data.getLatestActive());
        holder.latestMessage.setText(data.getLatestMessage());

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

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(onRowClick);

            date = itemView.findViewById(R.id.latest_active_date);
            userName = itemView.findViewById(R.id.user_name);
            latestMessage = itemView.findViewById(R.id.latest_message);
            image = itemView.findViewById(R.id.profile_icon);
        }

        private final View.OnClickListener onRowClick = view -> {
            Context ctx = itemView.getContext();
            Intent intent = new Intent(ctx, PrivateMessageScreen.class);
            intent.putExtra(IntentKeys.CHAT_ID, chatID);
            ctx.startActivity(intent);
        };
    }

}
