package com.home.mymessenger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

public class SignInFragment extends Fragment {

    private ShapeableImageView profilePicture;
    private TextInputLayout statusText;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_in_fragment, container, false);

        profilePicture = view.findViewById(R.id.sign_in_profile_picture);
        statusText = view.findViewById(R.id.sign_in_status);
        floatingActionButton = view.findViewById(R.id.sign_in_fab);

        return view;
    }
}
