package com.example.studentmanagenment.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagenment.R;
import com.example.studentmanagenment.ui.auth.ChangePasswordActivity;
import com.example.studentmanagenment.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnStudentList, btnChangePassword, btnLogout;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        btnStudentList = findViewById(R.id.btnStudentList);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        showCurrentUser();

        btnStudentList.setOnClickListener(v -> {
            // Sau nÃ y thÃ nh viÃªn 2 thay Ä‘oáº¡n nÃ y báº±ng mÃ n hÃ¬nh danh sÃ¡ch sinh viÃªn.
            // VÃ­ dá»¥:
            // startActivity(new Intent(this, StudentListActivity.class));
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void showCurrentUser() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            goToLogin();
            return;
        }

        String email = user.getEmail() != null ? user.getEmail() : "NgÆ°á»i dÃ¹ng";
        tvWelcome.setText("Xin chÃ o, " + email);
    }

    private void logout() {
        auth.signOut();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}



