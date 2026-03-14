package com.example.emptyactivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private View loadingView, authView, dashboardView;
    private EditText regEmail, regPass, regUser, searchField;
    private TextView friendNameTxt;
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        searchField = findViewById(R.id.searchField);
        friendNameTxt = findViewById(R.id.friendNameTxt);
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        // فحص حالة الدخول
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (mAuth.getCurrentUser() != null) {
                dashboardView.setVisibility(View.VISIBLE);
            } else {
                authView.setVisibility(View.VISIBLE);
            }
        }, 3000);

        findViewById(R.id.registerBtn).setOnClickListener(v -> handleAuth());
        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriend());
    }

    private void handleAuth() {
        String email = regEmail.getText().toString().trim();
        String pass = regPass.getText().toString().trim();
        String user = regUser.getText().toString().toLowerCase().trim();

        if (email.isEmpty() || pass.length() < 6) {
            Toast.makeText(this, "الايميل مطلوب والرمز لازم 6 أحرف!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "جاري الدخول للمنصة...", Toast.LENGTH_SHORT).show();

        // محاولة تسجيل الدخول أولاً
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // دخل بنجاح
                authView.setVisibility(View.GONE);
                dashboardView.setVisibility(View.VISIBLE);
            } else {
                // إذا فشل الدخول، نجرب نسوي حساب جديد
                mAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
                    String uid = mAuth.getUid();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", user);
                    map.put("email", email);
                    
                    mDb.child("Users").child(uid).setValue(map);
                    mDb.child("Usernames").child(user).setValue(uid);
                    
                    authView.setVisibility(View.GONE);
                    dashboardView.setVisibility(View.VISIBLE);
                }).addOnFailureListener(e -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void searchFriend() {
        String query = searchField.getText().toString().toLowerCase().trim();
        if (query.isEmpty()) return;

        mDb.child("Usernames").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fUid = snapshot.getValue(String.class);
                    mDb.child("Users").child(fUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot s) {
                            friendNameTxt.setText("✅ تم العثور على: @" + s.getValue().toString());
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
                } else {
                    Toast.makeText(MainActivity.this, "هذا اليوزر غير مسجل في حسين ألترا!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
