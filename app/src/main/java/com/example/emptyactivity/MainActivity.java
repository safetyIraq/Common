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
    private EditText regName, regUser, searchField;
    private TextView friendNameTxt;
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        regName = findViewById(R.id.regName);
        regUser = findViewById(R.id.regUser);
        searchField = findViewById(R.id.searchField);
        friendNameTxt = findViewById(R.id.friendNameTxt);

        // تشغيل الخدمات مع فحص الأخطاء
        try {
            mAuth = FirebaseAuth.getInstance();
            mDb = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Toast.makeText(this, "عطل في الفايربيس! تأكد من ملف google-services.json", Toast.LENGTH_LONG).show();
        }

        // المؤقت اللي ينهي الشاشة السوداء
        new Handler().postDelayed(() -> {
            if (loadingView != null) loadingView.setVisibility(View.GONE); // إخفاء اللودينج إجبارياً
            
            if (mAuth != null && mAuth.getCurrentUser() != null) {
                dashboardView.setVisibility(View.VISIBLE);
            } else {
                authView.setVisibility(View.VISIBLE);
            }
        }, 3000);

        findViewById(R.id.registerBtn).setOnClickListener(v -> registerUser());
        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriend());
    }

    private void registerUser() {
        String name = regName.getText().toString().trim();
        String user = regUser.getText().toString().toLowerCase().trim();

        if (name.isEmpty() || user.isEmpty()) {
            Toast.makeText(this, "الحقول فارغة!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInAnonymously().addOnSuccessListener(authResult -> {
            String uid = mAuth.getUid();
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("username", user);
            
            mDb.child("Users").child(uid).setValue(map);
            mDb.child("Usernames").child(user).setValue(uid);
            
            authView.setVisibility(View.GONE);
            dashboardView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "أهلاً بك في منصة حسين!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "فشل السيرفر: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void searchFriend() {
        String query = searchField.getText().toString().toLowerCase().trim();
        mDb.child("Usernames").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fUid = snapshot.getValue(String.class);
                    mDb.child("Users").child(fUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot s) {
                            friendNameTxt.setText("تم العثور على: " + s.getValue().toString());
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
                } else Toast.makeText(MainActivity.this, "اليوزر غير موجود!", Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
