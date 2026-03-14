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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

        // ربط العناصر
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        regName = findViewById(R.id.regName);
        regUser = findViewById(R.id.regUsername);
        regPhone = findViewById(R.id.regPhone);
        searchUser = findViewById(R.id.searchUsername);
        resultTxt = findViewById(R.id.searchResult);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 1. شاشة تحميل
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (mAuth.getCurrentUser() == null) {
                authView.setVisibility(View.VISIBLE);
            } else {
                dashboardView.setVisibility(View.VISIBLE);
            }
        }, 3000);

        // 2. زر التسجيل في منصة حسين
        findViewById(R.id.registerBtn).setOnClickListener(v -> registerOnPlatform());

        // 3. زر البحث عن صديق باليوزر نيم
        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriendByUsername());
    }

    private void registerOnPlatform() {
        String name = regName.getText().toString();
        String username = regUser.getText().toString();
        String phone = regPhone.getText().toString();

        if (name.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "يرجى ملء كافة البيانات", Toast.LENGTH_SHORT).show();
            return;
        }

        // تسجيل دخول وهمي (لأغراض التجربة) أو ربطه بـ Firebase Auth
        mAuth.signInAnonymously().addOnSuccessListener(authResult -> {
            String uid = mAuth.getUid();
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("username", username.toLowerCase());
            userMap.put("phone", phone);

            // حفظ بياناتك في "قائمة مستخدمي منصة حسين"
            mDatabase.child("HussainUsers").child(uid).setValue(userMap);
            // حفظ اليوزر نيم في قائمة البحث السريع
            mDatabase.child("Usernames").child(username.toLowerCase()).setValue(uid);

            authView.setVisibility(View.GONE);
            dashboardView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "أهلاً بك في Hussain Ultra!", Toast.LENGTH_SHORT).show();
        });
    }

    private void searchFriendByUsername() {
        String query = searchUser.getText().toString().toLowerCase();
        resultTxt.setText("جارِ البحث عن " + query + "...");

        mDatabase.child("Usernames").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String friendUid = snapshot.getValue(String.class);
                    // جلب معلومات الصديق
                    mDatabase.child("HussainUsers").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            String fName = userSnapshot.child("name").getValue(String.class);
                            String fPhone = userSnapshot.child("phone").getValue(String.class);
                            resultTxt.setText("✅ تم العثور على الصديق:\nالاسم: " + fName + "\nالرقم: " + fPhone + "\nيمكنك مراسلته الآن!");
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
                } else {
                    resultTxt.setText("❌ اليوزر غير موجود في منصة حسين");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
