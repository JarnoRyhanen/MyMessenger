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

import com.home.mymessenger.R;
import com.home.mymessenger.data.InboxData;

import java.util.List;

public class InboxRecyclerViewAdapter extends RecyclerView.Adapter<InboxRecyclerViewAdapter.InboxViewHolder> {

    private static final String TAG = "InboxRecyclerViewAdapte";
    private final Context context;
    private final List<InboxData> itemList;

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

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cancel pressed on " + inboxData.getMessage());
            }
        });

        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: chatting accepted with user " + inboxData.getSenderID());
            }
        });

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
