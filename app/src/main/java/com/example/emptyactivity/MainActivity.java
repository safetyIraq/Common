package com.example.emptyactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private View loadingView, setupView, mainView, redDot;
    private EditText tokenIn, chatIn, msgIn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingView = findViewById(R.id.loadingView);
        setupView = findViewById(R.id.setupView);
        mainView = findViewById(R.id.mainView);
        redDot = findViewById(R.id.redDot);
        tokenIn = findViewById(R.id.tokenInput);
        chatIn = findViewById(R.id.chatIdInput);
        msgIn = findViewById(R.id.messageInput);
        prefs = getSharedPreferences("HussainData", MODE_PRIVATE);

        // إذن الإشعارات لأندرويد 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (prefs.getString("token", "").isEmpty()) setupView.setVisibility(View.VISIBLE);
            else { mainView.setVisibility(View.VISIBLE); startBgService(); }
        }, 3000);

        findViewById(R.id.saveBtn).setOnClickListener(v -> {
            prefs.edit().putString("token", tokenIn.getText().toString()).putString("chat", chatIn.getText().toString()).apply();
            setupView.setVisibility(View.GONE); mainView.setVisibility(View.VISIBLE); startBgService();
        });

        findViewById(R.id.sendBtn).setOnClickListener(v -> sendMsg());
        findViewById(R.id.bellContainer).setOnClickListener(v -> redDot.setVisibility(View.GONE));
    }

    private void startBgService() {
        Intent i = new Intent(this, TelegramService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
        else startService(i);
    }

    private void sendMsg() {
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
