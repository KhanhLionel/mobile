package com.example.studentmanagenment.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagenment.R;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmNewPassword;
    private Button btnChangePassword;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        if (!validateInput(oldPassword, newPassword, confirmNewPassword)) {
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Báº¡n cáº§n Ä‘Äƒng nháº­p láº¡i", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLoading(true);

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPassword))
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    setLoading(false);

                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Äá»•i máº­t kháº©u thÃ nh cÃ´ng", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        String error = updateTask.getException() != null
                                                ? updateTask.getException().getMessage()
                                                : "Äá»•i máº­t kháº©u tháº¥t báº¡i";
                                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        setLoading(false);
                        Toast.makeText(this, "Máº­t kháº©u cÅ© khÃ´ng Ä‘Ãºng", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String oldPassword, String newPassword, String confirmNewPassword) {
        if (TextUtils.isEmpty(oldPassword)) {
            edtOldPassword.setError("Vui lÃ²ng nháº­p máº­t kháº©u cÅ©");
            edtOldPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("Vui lÃ²ng nháº­p máº­t kháº©u má»›i");
            edtNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Máº­t kháº©u má»›i tá»‘i thiá»ƒu 6 kÃ½ tá»±");
            edtNewPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Máº­t kháº©u nháº­p láº¡i khÃ´ng khá»›p");
            edtConfirmNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!isLoading);
    }
}



