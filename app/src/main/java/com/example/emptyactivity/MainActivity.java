package com.example.emptyactivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    
    private View loadingView, setupView, mainView, redDot;
    private EditText tokenIn, chatIn, msgIn;
    private SharedPreferences prefs;
    private int lastUpdateId = 0;
    private String latestMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // الربط
        loadingView = findViewById(R.id.loadingView);
        setupView = findViewById(R.id.setupView);
        mainView = findViewById(R.id.mainView);
        redDot = findViewById(R.id.redDot);
        tokenIn = findViewById(R.id.tokenInput);
        chatIn = findViewById(R.id.chatIdInput);
        msgIn = findViewById(R.id.messageInput);
        prefs = getSharedPreferences("HussainData", MODE_PRIVATE);

        // شاشة التحميل (تطلع 3 ثواني)
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            checkInitialData();
        }, 3000);

        findViewById(R.id.saveBtn).setOnClickListener(v -> saveAndStart());
        findViewById(R.id.sendBtn).setOnClickListener(v -> sendMsg());
        findViewById(R.id.bellContainer).setOnClickListener(v -> {
            redDot.setVisibility(View.GONE);
            new AlertDialog.Builder(this).setTitle("الرسالة الواردة").setMessage(latestMsg).show();
        });
    }

    private void checkInitialData() {
        String t = prefs.getString("token", "");
        if (t.isEmpty()) {
            setupView.setVisibility(View.VISIBLE);
        } else {
            mainView.setVisibility(View.VISIBLE);
            startPolling();
        }
    }

    private void saveAndStart() {
        prefs.edit().putString("token", tokenIn.getText().toString()).putString("chat", chatIn.getText().toString()).apply();
        setupView.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
        startPolling();
    }

    private void startPolling() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchUpdates();
                new Handler().postDelayed(this, 10000);
            }
        }, 10000);
    }

    private void fetchUpdates() {
        String t = prefs.getString("token", "");
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + t + "/getUpdates?offset=" + (lastUpdateId + 1));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String res = s.hasNext() ? s.next() : "";
                JSONArray results = new JSONObject(res).getJSONArray("result");
                if (results.length() > 0) {
                    JSONObject last = results.getJSONObject(results.length() - 1);
                    lastUpdateId = last.getInt("update_id");
                    latestMsg = last.getJSONObject("message").getString("text");
                    runOnUiThread(() -> {
                        redDot.setVisibility(View.VISIBLE);
                        showNotification(latestMsg);
                    });
                }
            } catch (Exception e) {}
        }).start();
    }

    private void showNotification(String m) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel("h", "H", NotificationManager.IMPORTANCE_HIGH));
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, "h")
            .setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle("رسالة جديدة").setContentText(m)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setAutoCancel(true);
        nm.notify(1, b.build());
    }

    private void sendMsg() {
        String t = prefs.getString("token", "");
        String c = prefs.getString("chat", "");
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + t + "/sendMessage?chat_id=" + c + "&text=" + msgIn.getText().toString());
                ((HttpURLConnection) url.openConnection()).getResponseCode();
                runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {}
        }).start();
    }
}
