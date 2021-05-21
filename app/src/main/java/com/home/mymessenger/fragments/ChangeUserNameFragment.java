package com.home.mymessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;

import java.util.HashMap;
import java.util.Map;

public class ChangeUserNameFragment extends Fragment {

    private TextInputEditText userNameEditText;
    private Button saveButton;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_user_name_fragment, container, false);
        userNameEditText = view.findViewById(R.id.change_user_name_text_input_edit_text);
        userNameEditText.setText(user.getDisplayName());

        saveButton = view.findViewById(R.id.change_user_name_fragment_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });


        return view;
    }

    private void save() {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userNameEditText.getText().toString())
                .build();

        if (user != null) {
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                DatabaseReference statusRef = ref.child("users").child(user.getUid());
                Map<String, Object> userObjectMap = new HashMap<>();
                userObjectMap.put("user_name", user.getDisplayName());
                statusRef.updateChildren(userObjectMap);

            });
        }
        getParentFragmentManager().popBackStack();
    }
}
