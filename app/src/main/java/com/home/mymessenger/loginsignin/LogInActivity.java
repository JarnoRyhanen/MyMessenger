package com.home.mymessenger.loginsignin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.home.mymessenger.R;
import com.home.mymessenger.dp.FireBaseDBHelper;
import com.home.mymessenger.mainactivity.MainActivity;

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LogInActivity";
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;

    private TextView forgotPasswordTextView;
    private TextView registerTextView;

    private Button logInButton;
    private FirebaseAuth auth;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == logInButton) {
                Log.d(TAG, "onClick: ");
                logIn();
            } else if (v == forgotPasswordTextView) {
                forgotPassword();
            } else if (v == registerTextView) {
                registerUser();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_activity_layout);

        emailInputLayout = findViewById(R.id.text_input_email_log_in_activity);
        passwordInputLayout = findViewById(R.id.text_input_password_log_in_activity);

        logInButton = findViewById(R.id.button_log_in);

        registerTextView = findViewById(R.id.register);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        logInButton.setOnClickListener(onClickListener);
        forgotPasswordTextView.setOnClickListener(onClickListener);
        registerTextView.setOnClickListener(onClickListener);
    }

    private void logIn() {
        String emailAddress = emailInputLayout.getEditText().getText().toString().trim();
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        if (!validateTextInput(emailInputLayout) | !validateTextInput(passwordInputLayout)) {
            return;
        }

        auth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startMainActivity();
            } else {
                Toast.makeText(LogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean validateTextInput(TextInputLayout... textInputLayouts) {
        String textInput = textInputLayouts[0].getEditText().getText().toString().trim();
        if (textInput.isEmpty()) {
            textInputLayouts[0].setError(getResources().getString(R.string.fui_required_field));
            return false;
        } else if (textInputLayouts[0] == emailInputLayout && !Patterns.EMAIL_ADDRESS.matcher(textInput).matches()) {
            Log.d(TAG, "validateTextInput: " + textInputLayouts[0]);
            textInputLayouts[0].setError(getResources().getString(R.string.fui_invalid_email_address));
            return false;
        } else {
            textInputLayouts[0].setError(null);
            return true;
        }
    }

    private void forgotPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    private void registerUser() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        FireBaseDBHelper.getInstance().listerForUserChatChange();
    }
}
