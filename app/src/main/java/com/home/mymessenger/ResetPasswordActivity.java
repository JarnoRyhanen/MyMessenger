package com.home.mymessenger;

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
            emailEditText.setError("This field can't be empty");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            emailEditText.setError("Please provide a valid address");
            emailEditText.requestFocus();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Check your email to reset the password!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Something went wrong, please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
