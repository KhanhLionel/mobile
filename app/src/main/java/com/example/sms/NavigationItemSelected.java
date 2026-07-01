package com.example.sms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.sms.ui.student.SinhVienMainActivity;
import com.example.sms.ui.subject.MonHocMainActivity;
import com.example.sms.ui.schedule.ThoiKhoaBieuActivity;
import com.example.sms.api.AuthApi;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;

public class NavigationItemSelected {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    AlertDialog logoutDialog;
    Context context;

    String field;

    public NavigationItemSelected(NavigationView navigationView, DrawerLayout drawerLayout, AlertDialog logoutDialog, Context context) {
        this.navigationView = navigationView;
        this.drawerLayout = drawerLayout;
        this.logoutDialog = logoutDialog;
        this.context = context;
    }

    public void itemSelected(){
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_student){
                Intent intent = new Intent(context, SinhVienMainActivity.class);
                context.startActivity(intent);
            } else if (id == R.id.nav_subject){
                Intent intent = new Intent(context, MonHocMainActivity.class);
                context.startActivity(intent);
            } else if (id == R.id.nav_schedule){
                Intent intent = new Intent(context, ThoiKhoaBieuActivity.class);
                context.startActivity(intent);
            } else if (id == R.id.nav_change_password) {
                showChangePasswordDialog();
            } else if (id == R.id.nav_logout){
                logoutDialog.show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Đổi mật khẩu");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNewPassword = new EditText(context);
        etNewPassword.setHint("Mật khẩu mới (Tối thiểu 6 ký tự)");
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newPassword = etNewPassword.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(context, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
