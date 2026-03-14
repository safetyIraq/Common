package com.example.emptyactivity;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    
    private View redDot;
    private Handler handler = new Handler();
    private int lastUpdateId = 0; 
    private String latestMessage = "لا توجد رسائل جديدة"; // لحفظ نص الرسالة

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redDot = findViewById(R.id.redDot);
        Button sendBtn = findViewById(R.id.sendBtn);
        EditText tokenIn = findViewById(R.id.tokenInput);
        EditText chatIdIn = findViewById(R.id.chatIdInput);
        EditText msgIn = findViewById(R.id.messageInput);

        // تفقد الرسائل كل 10 ثواني تلقائياً
        startAutoCheck(tokenIn);

        sendBtn.setOnClickListener(v -> {
            sendTelegramMessage(tokenIn.getText().toString(), chatIdIn.getText().toString(), msgIn.getText().toString());
        });

        // عند الضغط على الجرس: يفتح نافذة تشوف بيها الرسالة وتختفي النقطة
        findViewById(R.id.bellContainer).setOnClickListener(v -> {
            redDot.setVisibility(View.GONE);
            showMessageDialog();
        });
    }

    private void showMessageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("الرسالة الواردة")
                .setMessage(latestMessage)
                .setPositiveButton("تم", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startAutoCheck(EditText tokenIn) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
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
                // نطلب التحديثات الجديدة فقط باستخدام offset
                URL url = new URL("https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray result = jsonResponse.getJSONArray("result");

                if (result.length() > 0) {
                    // ناخذ آخر رسالة وصلتنا
                    JSONObject lastObj = result.getJSONObject(result.length() - 1);
                    lastUpdateId = lastObj.getInt("update_id");
                    latestMessage = lastObj.getJSONObject("message").getString("text");

                    // تشغيل التنبيهات على الواجهة الرئيسية
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

    private void sendTelegramMessage(String token, String chat, String msg) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chat + "&text=" + msg);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
