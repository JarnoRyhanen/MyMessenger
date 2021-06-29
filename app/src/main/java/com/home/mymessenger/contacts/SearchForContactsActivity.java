package com.home.mymessenger.contacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;
import com.home.mymessenger.data.ContactData;
import com.home.mymessenger.dp.FireBaseDBHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchForContactsActivity extends AppCompatActivity {

    private final static int REQUEST_CONTACT = 1;

    private static final String TAG = "SearchForContactsActivi";
    private RecyclerView contactsRecycler;
    public List<ContactData> contactDataList = new ArrayList<>();
    private ContactRecyclerAdapter adapter;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FireBaseDBHelper fireBaseDBHelper = new FireBaseDBHelper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list_view);

        contactsRecycler = findViewById(R.id.contacts_recycler_view);
        contactsRecycler.setHasFixedSize(true);
        checkForContactPermission();
    }

    private void checkForContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT);
        } else {
            checkForUser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkForUser();
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


    private void checkForUser() {
        startAsyncTask();
    }


    private void startAsyncTask() {
        ContactDataAsyncTask task = new ContactDataAsyncTask(this);
        task.execute();
    }

    private static class ContactDataAsyncTask extends AsyncTask<Void, Void, String> {

        private WeakReference<SearchForContactsActivity> weakReference;

        ContactDataAsyncTask(SearchForContactsActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SearchForContactsActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            SearchForContactsActivity activity = weakReference.get();
//            DatabaseReference userRef = activity.ref.child("users");
//            userRef.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        final Map<String, Object> userMap = (Map<String, Object>) snapshot.getValue();
//                        if (userMap != null) {
//                            for (String userID : userMap.keySet()) {
//                                String foundUserName = userMap.get(userID).toString().trim();
//                                Log.d(TAG, "onDataChange: " + foundUserName);
////                                getContact(userID);
//
//                                DatabaseReference userSpecificInfoRef = activity.ref.child("user_specific_info").child(userID).child("phone_number");
//                                userSpecificInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        if (snapshot.exists()) {
//                                            String foundPhoneNumber = snapshot.getValue().toString().trim();
//
//                                            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(foundPhoneNumber));
//                                            Cursor cursor = activity.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
//                                                    null,
//                                                    null,
//                                                    null,
//                                                    null);
//
//                                            if (cursor.getCount() > 0) {
//                                                while (cursor.moveToNext()) {
//                                                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                                                    String contactNumber = cursor.getString(0);
////                                                Log.d(TAG, "onDataChange: contactnumber: " + contactNumber + " found number: " + foundPhoneNumber);
//                                                    Log.d(TAG, "name: " + contactName + " found username: " + foundUserName +
//                                                            " number: " + contactNumber + " found phonenumber: " + foundPhoneNumber);
//
//                                                    Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//                                                    Cursor phoneCursor = activity.getContentResolver().query(
//                                                            phoneUri,
//                                                            null,
//                                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
//                                                            new String[]{},
//                                                            null);
//                                                }
//                                                cursor.close();
//                                            }
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    error.getMessage();
//                }
//            });
            activity.fireBaseDBHelper.setActivity(activity);
            activity.fireBaseDBHelper.checkForUser1();
            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(1000);
                    Log.d(TAG, "doInBackground: " + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            SearchForContactsActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.d(TAG, "onPostExecute: returned");
                return;
            }

//            Log.d(TAG, "checkForUser1: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            weakReference.get().contactsRecycler.setLayoutManager(new LinearLayoutManager(activity));
            activity.adapter = new ContactRecyclerAdapter(activity, activity.contactDataList);
            activity.contactsRecycler.setAdapter(activity.adapter);
            for(ContactData a : activity.contactDataList){
                Log.d(TAG, "onPostExecute: " + a.getContactName());
            }
//            Toast.makeText(activity, "Finished", Toast.LENGTH_SHORT).show();
        }
    }
}
