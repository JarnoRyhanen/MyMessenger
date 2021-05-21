package com.home.mymessenger.contacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.home.mymessenger.R;
import com.home.mymessenger.data.ContactData;

import java.util.ArrayList;
import java.util.List;

public class SearchForContactsActivity extends AppCompatActivity {

    private final static int REQUEST_CONTACT = 1;

    private static final String TAG = "SearchForContactsActivi";
    private RecyclerView contactsRecycler;
    private List<ContactData> contactDataList = new ArrayList<>();
    private ContactRecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list_view);

        contactsRecycler = findViewById(R.id.contacts_recycler_view);

        checkForContactPermission();

        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactRecyclerAdapter(this, contactDataList);
        contactsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void checkForContactPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT);
        } else {
            getContactList();
        }
    }

    private void getContactList() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        String sortAscending = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor cursor = getContentResolver().query(uri,
                null,
                null,
                null,
                null
        );

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                Cursor phoneCursor = getContentResolver().query(
                        phoneUri,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
                        new String[]{contactID},
                        null);

                if (phoneCursor.moveToNext()) {
                    String contactPhoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    ContactData data = new ContactData();
                    data.setContactName(contactName);
                    data.setContactPhoneNumber(contactPhoneNumber);
                    contactDataList.add(data);

                    phoneCursor.close();
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContactList();
        } else {
            Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            checkForContactPermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

//        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void onClick(View view) {
    }
}
