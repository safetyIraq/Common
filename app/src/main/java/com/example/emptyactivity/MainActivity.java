package com.example.emptyactivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "MyTelegramPrefs";
    private View redDot;
    private Handler handler = new Handler();
    private int lastUpdateId = 0; 
    private String latestMessage = "لا توجد رسائل";
    private TextInputEditText tokenIn, chatIdIn, msgIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // تم تصحيح الخطأ هنا بإضافة onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redDot = findViewById(R.id.redDot);
        tokenIn = findViewById(R.id.tokenInput);
        chatIdIn = findViewById(R.id.chatIdInput);
        msgIn = findViewById(R.id.messageInput);
        Button sendBtn = findViewById(R.id.sendBtn);

        loadSavedData();
        startAutoCheck();

        sendBtn.setOnClickListener(v -> {
            saveData();
            sendMsg(tokenIn.getText().toString(), chatIdIn.getText().toString(), msgIn.getText().toString());
        });

        findViewById(R.id.bellContainer).setOnClickListener(v -> {
            redDot.setVisibility(View.GONE);
            new AlertDialog.Builder(this).setTitle("آخر رسالة").setMessage(latestMessage).show();
        });
    }

    private void startAutoCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String token = tokenIn.getText().toString();
                if (!token.isEmpty()) fetchUpdates(token);
                handler.postDelayed(this, 15000); // يفحص كل 15 ثانية
            }
        }, 15000);
    }

    private void fetchUpdates(String token) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String res = s.hasNext() ? s.next() : "";
                JSONObject json = new JSONObject(res);
                JSONArray results = json.getJSONArray("result");

                if (results.length() > 0) {
                    JSONObject last = results.getJSONObject(results.length() - 1);
                    lastUpdateId = last.getInt("update_id");
                    latestMessage = last.getJSONObject("message").getString("text");

                    runOnUiThread(() -> {
                        redDot.setVisibility(View.VISIBLE);
                        showSystemNotification(latestMessage);
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showSystemNotification(String message) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "telegram_channel";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(channelId, "Bot Messages", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("رسالة جديدة من البوت")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        nm.notify(1, builder.build());
    }

    private void saveData() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putString("token", tokenIn.getText().toString())
            .putString("chat", chatIdIn.getText().toString()).apply();
    }

    private void loadSavedData() {
        SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        tokenIn.setText(p.getString("token", ""));
        chatIdIn.setText(p.getString("chat", ""));
    }

    private void sendMsg(String t, String c, String m) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + t + "/sendMessage?chat_id=" + c + "&text=" + m);
                ((HttpURLConnection) url.openConnection()).getResponseCode();
                runOnUiThread(() -> Toast.makeText(this, "✅ تم!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
