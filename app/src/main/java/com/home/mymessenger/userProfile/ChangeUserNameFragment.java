package com.home.mymessenger.userProfile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

    private static final Object INPUT_METHOD_SERVICE = 1;
    private TextInputEditText userNameEditText;
    private MaterialButton saveButton;

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
                UserNameRunnable runnable = new UserNameRunnable();
                new Thread(runnable).start();
            }
        });
        return view;
    }

    private void closeKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    class UserNameRunnable implements Runnable {
        @Override
        public void run() {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userNameEditText.getText().toString())
                    .build();

            if (user != null) {
                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    DatabaseReference userRef = ref.child("user_specific_info").child(user.getUid());
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("user_name", user.getDisplayName());
                    userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            closeKeyboard(saveButton);
                            getParentFragmentManager().popBackStack();
                        }
                    });

                    DatabaseReference userSpecificInfoRef = ref.child("users");
                    Map<String, Object> userSpecificInfoMap = new HashMap<>();
                    userSpecificInfoMap.put(user.getUid(), user.getDisplayName());
                    userSpecificInfoRef.updateChildren(userSpecificInfoMap);
                });
            }
        }
    }
}
