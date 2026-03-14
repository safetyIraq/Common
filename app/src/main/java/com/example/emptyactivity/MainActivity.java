package com.example.emptyactivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    
    private View redDot;
    private Handler handler = new Handler();
    private int lastUpdateId = 0; // لحفظ معرف آخر رسالة

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redDot = findViewById(R.id.redDot);
        Button sendBtn = findViewById(R.id.sendBtn);
        EditText tokenIn = findViewById(R.id.tokenInput);
        EditText chatIn = findViewById(R.id.chatIdInput);
        EditText msgIn = findViewById(R.id.messageInput);

        // تشغيل نظام "التفقد التلقائي" كل 10 ثواني
        startAutoCheck(tokenIn);

        sendBtn.setOnClickListener(v -> {
            // كود الإرسال القديم مالتك يبقى نفسه هنا
            sendTelegramMessage(tokenIn.getText().toString(), chatIn.getText().toString(), msgIn.getText().toString());
        });

        // عند الضغط على الجرس، تختفي النقطة الحمراء
        findViewById(R.id.bellContainer).setOnClickListener(v -> {
            redDot.setVisibility(View.GONE);
            Toast.makeText(this, "تم قراءة التنبيهات", Toast.LENGTH_SHORT).show();
        });
    }

    private void startAutoCheck(EditText tokenIn) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String token = tokenIn.getText().toString();
                if (!token.isEmpty()) {
                    checkNewMessages(token);
                }
                handler.postDelayed(this, 10000); // كرر العملية كل 10 ثواني
            }
        }, 10000);
    }

    private void checkNewMessages(String token) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                // إذا كان الـ JSON يحتوي على كلمة "message" يعني اكو رسالة جديدة
                if (result.contains("\"message\"")) {
                    runOnUiThread(() -> redDot.setVisibility(View.VISIBLE));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
