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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.home.mymessenger.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout textInputLayout;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_activity);

        textInputLayout = findViewById(R.id.text_input_email);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void onResetPasswordClick(View view) {
        resetPassword();
    }

    private void resetPassword() {
        String emailAddress = textInputLayout.getEditText().getText().toString().trim();
        if (emailAddress.isEmpty()) {
            textInputLayout.setError(getResources().getString(R.string.fui_required_field));
            textInputLayout.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            textInputLayout.setError(getResources().getString(R.string.fui_error_email_does_not_exist));
            textInputLayout.requestFocus();
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
