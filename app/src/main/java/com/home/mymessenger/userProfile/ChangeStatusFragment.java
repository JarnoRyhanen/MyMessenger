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

import com.google.android.material.button.MaterialButton;
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
    private MaterialButton saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_status_fragment, container, false);

        statusEditText = view.findViewById(R.id.change_status_fragment_text_input_edit_text);
        saveButton = view.findViewById(R.id.change_status_fragment_save_button);

        saveButton.setOnClickListener(v -> saveStatus());

        if (user != null) {
            UserData userData = realm.where(UserData.class).equalTo("userID", user.getUid()).findFirst();
            if (userData != null) {
                String status = userData.getUserStatus();
                statusEditText.setText(status);
            }
        }
        return view;
    }

    private void saveStatus() {
        CharSequence newStatus = statusEditText.getText();

        DatabaseReference statusRef = ref.child(getResources().getString(R.string.user_specific_info))
                .child(user.getUid());

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put(getResources().getString(R.string.current_status), newStatus.toString());
        statusRef.updateChildren(statusMap).addOnCompleteListener(task -> {
            closeKeyboard(saveButton);
            getParentFragmentManager().popBackStack();
        });
    }

    private void closeKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }
}
