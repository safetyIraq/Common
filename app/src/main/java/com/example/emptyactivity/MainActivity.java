package com.example.emptyactivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendBtn = findViewById(R.id.sendBtn);
        EditText tokenIn = findViewById(R.id.tokenInput);
        EditText chatIn = findViewById(R.id.chatIdInput);
        EditText msgIn = findViewById(R.id.messageInput);

        sendBtn.setOnClickListener(v -> {
            String urlString = "https://api.telegram.org/bot" + tokenIn.getText().toString() 
                             + "/sendMessage?chat_id=" + chatIn.getText().toString() 
                             + "&text=" + msgIn.getText().toString();

            new Thread(() -> {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال!", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "❌ خطأ في الإرسال", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }
}
