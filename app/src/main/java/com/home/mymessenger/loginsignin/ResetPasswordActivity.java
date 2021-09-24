package com.home.mymessenger.loginsignin;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.home.mymessenger.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        emailEditText = findViewById(R.id.reset_password_edit_text);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void onResetPasswordClick(View view) {
        resetPassword();
    }

    private void resetPassword() {
        String emailAddress = emailEditText.getText().toString().trim();
        if (emailAddress.isEmpty()) {
            emailEditText.setError(getResources().getString(R.string.fui_required_field));
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            emailEditText.setError(getResources().getString(R.string.fui_invalid_email_address));
            emailEditText.requestFocus();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.fui_title_confirm_recover_password), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Something went wrong, please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
