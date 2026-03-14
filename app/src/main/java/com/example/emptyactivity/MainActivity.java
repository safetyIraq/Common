package com.example.emptyactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    
    // لإدارة الذاكرة الآمنة
    private static final String PREFS_NAME = "MyTelegramPrefs";
    private static final String KEY_TOKEN = "bot_token";
    private static final String KEY_CHAT_ID = "chat_id";

    private View redDot;
    private Handler handler = new Handler();
    private int lastUpdateId = 0; 
    private String latestMessage = "لا توجد رسائل جديدة"; 
    private TextInputEditText tokenIn, chatIdIn, msgIn;

    @Override
    protected void Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط عناصر الواجهة الجديدة
        redDot = findViewById(R.id.redDot);
        Button sendBtn = findViewById(R.id.sendBtn);
        tokenIn = findViewById(R.id.tokenInput);
        chatIdIn = findViewById(R.id.chatIdInput);
        msgIn = findViewById(R.id.messageInput);

        // تحميل البيانات المحفوظة (إن وجدت)
        loadSavedCredentials();

        // تشغيل نظام "التفقد التلقائي" بالخلفية كل 10 ثواني
        startAutoCheck();

        // حفظ البيانات تلقائياً أول ما تنكتب
        addSaveListeners();

        sendBtn.setOnClickListener(v -> {
            // نأخذ البيانات مباشرة من مربعات النص، لأنها راح تكون مكتوبة جاهزة
            sendTelegramMessage(
                tokenIn.getText().toString(),
                chatIdIn.getText().toString(),
                msgIn.getText().toString()
            );
        });

        // الجرس التفاعلي
        findViewById(R.id.bellContainer).setOnClickListener(v -> {
            redDot.setVisibility(View.GONE);
            showMessageDialog();
        });
    }

    // --- وظائف الذاكرة (SharedPreferences) ---

    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedToken = prefs.getString(KEY_TOKEN, "");
        String savedChatId = prefs.getString(KEY_CHAT_ID, "");

        tokenIn.setText(savedToken);
        chatIdIn.setText(savedChatId);
    }

    private void addSaveListeners() {
        // "مراقب" لمربع التوكن؛ يحفظ بمجرد تغيير النص
        tokenIn.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                saveCredential(KEY_TOKEN, s.toString());
            }
        });

        // مراقب لمربع Chat ID
        chatIdIn.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                saveCredential(KEY_CHAT_ID, s.toString());
            }
        });
    }

    private void saveCredential(String key, String value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    // --- وظائف التفقد والاستقبال (Polling) ---

    private void showMessageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("الرسالة الواردة")
                .setMessage(latestMessage)
                .setPositiveButton("تم", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startAutoCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // نأخذ التوكن من مربع النص المكتوب
                String token = tokenIn.getText().toString();
                if (!token.isEmpty()) {
                    fetchUpdates(token);
                }
                handler.postDelayed(this, 10000); 
            }
        }, 10000);
    }

    private void fetchUpdates(String token) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray result = jsonResponse.getJSONArray("result");

                if (result.length() > 0) {
                    JSONObject lastObj = result.getJSONObject(result.length() - 1);
                    lastUpdateId = lastObj.getInt("update_id");
                    latestMessage = lastObj.getJSONObject("message").getString("text");

                    runOnUiThread(() -> {
                        redDot.setVisibility(View.VISIBLE);
                        playNotificationSound();
                        Toast.makeText(this, "رسالة جديدة من تيليجرام!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- وظائف الإرسال ---

    private void sendTelegramMessage(String token, String chat, String msg) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chat + "&text=" + msg);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال بنجاح!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "❌ فشل الإرسال: تأكد من التوكن", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
