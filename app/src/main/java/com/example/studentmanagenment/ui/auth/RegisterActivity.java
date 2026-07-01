package com.example.studentmanagenment.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagenment.R;
import com.example.studentmanagenment.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> register());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validateInput(fullName, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser != null) {
                            saveUserProfile(firebaseUser.getUid(), fullName, email);
                        } else {
                            setLoading(false);
                            Toast.makeText(this, "KhÃ´ng láº¥y Ä‘Æ°á»£c thÃ´ng tin tÃ i khoáº£n", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoading(false);
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "ÄÄƒng kÃ½ tháº¥t báº¡i";
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile(String uid, String fullName, String email) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("uid", uid);
        profile.put("fullName", fullName);
        profile.put("email", email);
        profile.put("role", "USER");
        profile.put("createdAt", System.currentTimeMillis());

        database.getReference("users")
                .child(uid)
                .setValue(profile)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Táº¡o tÃ i khoáº£n thÃ nh cÃ´ng", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        Toast.makeText(this, "TÃ i khoáº£n Ä‘Ã£ táº¡o nhÆ°ng lÆ°u há»“ sÆ¡ tháº¥t báº¡i", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Vui lÃ²ng nháº­p há» tÃªn");
            edtFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lÃ²ng nháº­p email");
            edtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng");
            edtEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lÃ²ng nháº­p máº­t kháº©u");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Máº­t kháº©u tá»‘i thiá»ƒu 6 kÃ½ tá»±");
            edtPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Máº­t kháº©u nháº­p láº¡i khÃ´ng khá»›p");
            edtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        tvGoLogin.setEnabled(!isLoading);
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}



