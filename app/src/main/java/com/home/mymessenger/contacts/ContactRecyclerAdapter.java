package com.home.mymessenger.contacts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.home.mymessenger.CustomDialog;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ContactData;

import java.util.ArrayList;
import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ContactViewHolder> implements Filterable {

    private static final String TAG = "ContactRecyclerAdapter";

    private final Context context;
    private List<ContactData> contactDataList = new ArrayList<>();
    private List<ContactData> contactDataListFull = new ArrayList<>();

    public ContactRecyclerAdapter(Context context, List<ContactData> list) {
        this.context = context;
        this.contactDataList = list;
        notifyDataSetChanged();
        contactDataListFull = new ArrayList<>(contactDataList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contacts_list_item, parent, false);

        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactRecyclerAdapter.ContactViewHolder holder, int position) {
        ContactData data = contactDataList.get(position);
        holder.contactName.setText(data.getContactName());
        holder.contactPhoneNumber.setText(data.getContactPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return contactDataList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView contactName;
        TextView contactPhoneNumber;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(onRowClick);

            contactName = itemView.findViewById(R.id.contact_person_name);
            contactPhoneNumber = itemView.findViewById(R.id.contact_person_phone_number);
        }

        private final View.OnClickListener onRowClick = view -> {
            FragmentManager fragmentManager = ((FragmentActivity) view.getContext()).getSupportFragmentManager();

            String userName = contactName.getText().toString();
            String phoneNumber = contactPhoneNumber.getText().toString();

            CustomDialog dialog = new CustomDialog();
            dialog.show(fragmentManager, phoneNumber.trim());
        };
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ContactData> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactDataListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ContactData item : contactDataListFull) {
                    if (item.getContactName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactDataList.clear();
            contactDataList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
