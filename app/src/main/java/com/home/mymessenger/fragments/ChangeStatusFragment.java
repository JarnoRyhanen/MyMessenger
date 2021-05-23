package com.home.mymessenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.mymessenger.R;
import com.home.mymessenger.data.UserData;
import com.home.mymessenger.dp.FireBaseDBHelper;
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

    public void setListener(ChangeStatusFragmentListener listener) {
        this.listener = listener;
    }

    private ChangeStatusFragmentListener listener;

    public interface ChangeStatusFragmentListener {
        void onStatusChanged(CharSequence status);
    }

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

    private void saveStatus() {
        CharSequence newStatus = statusEditText.getText();

        DatabaseReference statusRef = ref.child("user_specific_info").child(user.getUid());
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("current_status", newStatus.toString());
        statusRef.updateChildren(statusMap);

        getParentFragmentManager().popBackStack();
        listener.onStatusChanged(newStatus);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ChangeStatusFragmentListener) {
            listener = (ChangeStatusFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChangeStatusFragmentListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
