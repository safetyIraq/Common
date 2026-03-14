package com.example.emptyactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private View loadingView, setupView, mainView;
    private EditText tokenIn, chatIn, msgIn, phoneIn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر
        loadingView = findViewById(R.id.loadingView);
        setupView = findViewById(R.id.setupView);
        mainView = findViewById(R.id.mainView);
        
        tokenIn = findViewById(R.id.tokenInput);
        chatIn = findViewById(R.id.chatIdInput);
        msgIn = findViewById(R.id.messageInput);
        phoneIn = findViewById(R.id.phoneInput); // مربع رقم التليفون الجديد

        prefs = getSharedPreferences("HussainData", MODE_PRIVATE);

        // شاشة التحميل 3 ثواني
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (prefs.getString("token", "").isEmpty()) {
                setupView.setVisibility(View.VISIBLE);
            } else {
                mainView.setVisibility(View.VISIBLE);
                startBgService();
            }
        }, 3000);

        // زر الحفظ
        findViewById(R.id.saveBtn).setOnClickListener(v -> {
            saveUserAccount();
            setupView.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
            startBgService();
        });

        // زر الإرسال
        findViewById(R.id.sendBtn).setOnClickListener(v -> sendTelegramMsg());

        // زر مشاركة حساب حسين (Hussain ID)
        findViewById(R.id.shareAccountBtn).setOnClickListener(v -> shareHussainAccount());
    }

    private void saveUserAccount() {
        prefs.edit()
            .putString("token", tokenIn.getText().toString())
            .putString("chat", chatIn.getText().toString())
            .putString("my_phone", phoneIn.getText().toString())
            .apply();
        Toast.makeText(this, "تم إنشاء Hussain ID بنجاح!", Toast.LENGTH_SHORT).show();
    }

    private void shareHussainAccount() {
        String phoneNumber = prefs.getString("my_phone", phoneIn.getText().toString());
        
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "يرجى كتابة رقمك أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        String myId = "🚀 بطاقة هوية Hussain Ultra\n" +
                      "👤 الاسم: المبرمج حسين\n" +
                      "📱 الرقم: " + phoneNumber + "\n" +
                      "✨ تم البرمجة بواسطة: Hussain-Worm-GPT\n" +
                      "تحياتي من العراق 🇮🇶";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, myId);
        startActivity(Intent.createChooser(shareIntent, "مشاركة حسابي عبر..."));
    }

    private void startBgService() {
        Intent i = new Intent(this, TelegramService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    private void sendTelegramMsg() {
        String t = prefs.getString("token", ""), c = prefs.getString("chat", ""), m = msgIn.getText().toString();
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + t + "/sendMessage?chat_id=" + c + "&text=" + m);
                ((java.net.HttpURLConnection) url.openConnection()).getResponseCode();
                runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {}
        }).start();
    }
}
