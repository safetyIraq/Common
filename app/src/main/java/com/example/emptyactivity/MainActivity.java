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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private View loadingView, authView, dashboardView;
    private EditText regName, regUser, regPhone, msgInput;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
        msgInput = findViewById(R.id.messageInput);

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

        // 2. زر الدخول للمنصة (التسجيل)
        findViewById(R.id.registerBtn).setOnClickListener(v -> registerUser());

        // 3. زر إرسال الرسالة (داخل المنصة)
        findViewById(R.id.sendBtn).setOnClickListener(v -> sendMessageToTelegram());
    }

    private void registerUser() {
        String name = regName.getText().toString().trim();
        String user = regUser.getText().toString().trim();
        String phone = regPhone.getText().toString().trim();

        if (name.isEmpty() || user.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "عيني حسين، املأ كل الحقول!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "جاري الاتصال بالسيرفر...", Toast.LENGTH_SHORT).show();

        // محاولة تسجيل الدخول
        mAuth.signInAnonymously()
            .addOnSuccessListener(authResult -> {
                String uid = mAuth.getUid();
                HashMap<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("username", user.toLowerCase());
                userData.put("phone", phone);

                // حفظ البيانات في السحاب
                mDatabase.child("HussainUsers").child(uid).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        authView.setVisibility(View.GONE);
                        dashboardView.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "تم التسجيل بنجاح! ✅", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                // إذا طلع هذا الخطأ، فالمشكلة بإعدادات موقع Firebase
                Toast.makeText(this, "خطأ بالسيرفر: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void sendMessageToTelegram() {
        String message = msgInput.getText().toString().trim();
        // حط التوكن والآيدي مالتك هنا ثابتين حتى يرسل فوراً
        String token = "8307560710:AAFNRpzh141cq7rKt_OmPR0A823dxEaOZVU"; 
        String chatId = "7259620384";

        if (message.isEmpty()) {
            Toast.makeText(this, "اكتب شي حتى نرسله!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + message);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> Toast.makeText(this, "✅ وصلت الرسالة لتيليجرام!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "❌ خطأ برابط البوت", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ فشل الاتصال بالإنترنت", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
