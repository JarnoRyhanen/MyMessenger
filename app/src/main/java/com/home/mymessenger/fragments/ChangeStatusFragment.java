package com.home.mymessenger.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.RealmHelper;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

public class ChangeStatusFragment extends Fragment {
    private static final String TAG = "ChangeStatusFragment";
    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private TextInputEditText statusEditText;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_status_fragment, container, false);

        statusEditText = view.findViewById(R.id.change_status_fragment_text_input_edit_text);
        saveButton = view.findViewById(R.id.change_status_fragment_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });

        if (user != null) {
            UserData userData = realm.where(UserData.class).equalTo("userID", user.getUid()).findFirst();
            if (userData != null) {
                String status = userData.getUserStatus();
                statusEditText.setText(status);
            }
        }

        return view;
    }

    private void saveStatus(){
        DatabaseReference statusRef = ref.child("users").child(user.getUid());
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("current_status", statusEditText.getText().toString());
        statusRef.updateChildren(statusMap);

        returnToPreviousFragment();
    }

    private void returnToPreviousFragment() {
        Log.d(TAG, "goBack: ");
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
        transaction.replace(R.id.user_profile_activity_container, UserProfileFragment.class, null);
        transaction.addToBackStack(null);
        transaction.setReorderingAllowed(true);
        transaction.commit();
    }

}
