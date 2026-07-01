package com.example.sms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sms.ui.student.SinhVienMainActivity;
import com.example.sms.api.AuthApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.AlertDialog;
import android.widget.LinearLayout;

public class Login extends AppCompatActivity {

    Button buttonLogin;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;
    android.widget.TextView tvForgotPassword;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize
        buttonLogin = findViewById(R.id.login_button);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progress_bar);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        mAuth = FirebaseAuth.getInstance();

        //login onclick
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            String email, password;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());

            //validation
            if (email.isEmpty()) {
                Toast.makeText(Login.this, "Email không được để trống", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(Login.this, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            //login
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            reload(user);
                        } else {
                            Toast.makeText(Login.this, "Đăng nhập thất bại. Kiểm tra lại tài khoản và mật khẩu.", Toast.LENGTH_SHORT).show();
                            reload(null);
                        }
                    }
                });
        });

        // Forgot password click listener
        tvForgotPassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle("Khôi phục mật khẩu");
            builder.setMessage("Vui lòng nhập địa chỉ email của bạn. Hệ thống sẽ gửi một liên kết để đặt lại mật khẩu.");

            LinearLayout layout = new LinearLayout(Login.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 10);

            final EditText etResetEmail = new EditText(Login.this);
            etResetEmail.setHint("Nhập Email của bạn");
            etResetEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            
            // Nếu ngoài màn hình đăng nhập đã gõ sẵn email thì điền luôn vào
            String currentEmail = String.valueOf(editTextEmail.getText()).trim();
            if (!currentEmail.isEmpty()) {
                etResetEmail.setText(currentEmail);
            }
            
            layout.addView(etResetEmail);
            builder.setView(layout);

            builder.setPositiveButton("Gửi", (dialog, which) -> {
                String resetEmail = etResetEmail.getText().toString().trim();
                if (resetEmail.isEmpty()) {
                    Toast.makeText(Login.this, "Email không được để trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(resetEmail)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            new AlertDialog.Builder(Login.this)
                                .setTitle("Đã gửi Email")
                                .setMessage("Nếu email này tồn tại trong hệ thống, bạn sẽ nhận được một hướng dẫn khôi phục mật khẩu. Vui lòng kiểm tra cả hộp thư Rác (Spam).")
                                .setPositiveButton("OK", null)
                                .show();
                        } else {
                            Toast.makeText(Login.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            });

            builder.setNegativeButton("Hủy", null);
            builder.show();
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload(currentUser);
        }
    }

    private void reload(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(Login.this, SinhVienMainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}