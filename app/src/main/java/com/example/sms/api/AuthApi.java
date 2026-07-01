package com.example.sms.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthApi {
    private FirebaseAuth mAuth;
    private DatabaseReference adminsRef;

    public static boolean isAdmin = false; // Global state for simplicity in this project

    public AuthApi() {
        mAuth = FirebaseAuth.getInstance();
        adminsRef = FirebaseDatabase.getInstance().getReference("admins");
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void checkAdminStatus(ApiCallback<Boolean> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            isAdmin = false;
            callback.onSuccess(false);
            return;
        }

        adminsRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                    isAdmin = true;
                    callback.onSuccess(true);
                } else {
                    isAdmin = false;
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                isAdmin = false;
                callback.onFailure(error.toException());
            }
        });
    }

    public void logout() {
        mAuth.signOut();
        isAdmin = false;
    }
}
