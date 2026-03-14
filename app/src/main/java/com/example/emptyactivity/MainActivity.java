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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private View loadingView, authView, dashboardView;
    private EditText regName, regUser, regPhone, searchUser;
    private TextView resultTxt;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // الربط البرمي
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        regName = findViewById(R.id.regName);
        regUser = findViewById(R.id.regUsername);
        regPhone = findViewById(R.id.regPhone);
        searchUser = findViewById(R.id.searchUsername);
        resultTxt = findViewById(R.id.searchResult);

        // تنبيه: إذا تطبيقك كراش هنا، معناها ملف google-services.json ناقص أو غلط
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Toast.makeText(this, "فشل في تشغيل خدمات جوجل!", Toast.LENGTH_LONG).show();
        }

        // شاشة التحميل
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (mAuth.getCurrentUser() == null) {
                authView.setVisibility(View.VISIBLE);
            } else {
                dashboardView.setVisibility(View.VISIBLE);
            }
        }, 3000);

        // الزر اللي جان مديتفاعل - هسه ضفتله رسائل كشف
        findViewById(R.id.registerBtn).setOnClickListener(v -> {
            Toast.makeText(this, "تم الضغط على الزر، جاري الفحص...", Toast.LENGTH_SHORT).show();
            registerOnPlatform();
        });

        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriend());
    }

    private void registerOnPlatform() {
        String name = regName.getText().toString().trim();
        String user = regUser.getText().toString().trim();
        String phone = regPhone.getText().toString().trim();

        if (name.isEmpty() || user.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "أكو حقول فارغة يا حسين!", Toast.LENGTH_SHORT).show();
            return;
        }

        // محاولة الدخول للسيرفر
        Toast.makeText(this, "جاري الاتصال بسيرفر Hussain Ultra...", Toast.LENGTH_SHORT).show();
        
        mAuth.signInAnonymously()
            .addOnSuccessListener(authResult -> {
                String uid = mAuth.getUid();
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("username", user.toLowerCase());
                userMap.put("phone", phone);

                mDatabase.child("HussainUsers").child(uid).setValue(userMap)
                    .addOnSuccessListener(aVoid -> {
                        mDatabase.child("Usernames").child(user.toLowerCase()).setValue(uid);
                        authView.setVisibility(View.GONE);
                        dashboardView.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "تم الدخول بنجاح! ✅", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "فشل حفظ البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show());
            })
            .addOnFailureListener(e -> {
                // هذي الرسالة راح تگلك ليش الفايربيس مديقبل يدخلك
                Toast.makeText(this, "خطأ في السيرفر: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void searchFriend() {
        // كود البحث يبقى نفسه
    }
}
