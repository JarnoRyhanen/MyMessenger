package com.home.mymessenger.messaging;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.home.mymessenger.R;
import com.home.mymessenger.data.MessageData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PrivateMessageRecyclerAdapter extends RecyclerView.Adapter<PrivateMessageRecyclerAdapter.PrivateMessageViewHolder> {
    private static final String TAG = "PrivateMessageRecyclerA";
    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final Context context;
    protected final List<MessageData> chatDataList = new ArrayList<>();

    public PrivateMessageRecyclerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PrivateMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == MESSAGE_TYPE_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.private_message_list_row_left, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.private_message_list_row_right, parent, false);
        }
        return new PrivateMessageViewHolder(view, onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateMessageViewHolder holder, int position) {

        MessageData messageData = chatDataList.get(position);
        if (messageData != null) {
            holder.messageContentView.setText(messageData.getMessageContent());
            holder.messageDateView.setText(messageData.getDate());

            if (messageData.getImage() != null) {
                if (!messageData.getImage().isEmpty()) {
                    Picasso.get()
                            .load(messageData.getImage())
                            .into(holder.messageImageView);
                }
            }
        }
    }

    public void addMessage(MessageData message) {
        chatDataList.add(message);
    }

    public void clear() {
        chatDataList.clear();
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (chatDataList.get(position).getSender().equals(user.getUid())) {
            return MESSAGE_TYPE_RIGHT;
        } else {
            return MESSAGE_TYPE_LEFT;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private OnTouchListener onItemLongClickListener;

    public interface OnTouchListener {
        void onTouch(int position);
    }

    public void setOnItemLongClickListener(OnTouchListener listener) {
        this.onItemLongClickListener = listener;
    }


    public static class PrivateMessageViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public TextView messageContentView;
        public TextView messageDateView;
        public ImageView messageImageView;

        public PrivateMessageViewHolder(@NonNull View itemView, OnTouchListener listener) {
            super(itemView);

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

            messageContentView = itemView.findViewById(R.id.private_message_content);
            messageDateView = itemView.findViewById(R.id.private_message_date);
            messageImageView = itemView.findViewById(R.id.private_message_image);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getBindingAdapterPosition(), R.id.delete_message, 0, "Delete message");
        }
    }
}