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
    // تم تصحيح السطر التالي بإضافة كلمة onCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendBtn = findViewById(R.id.sendBtn);
        EditText tokenIn = findViewById(R.id.tokenInput);
        EditText chatIn = findViewById(R.id.chatIdInput);
        EditText msgIn = findViewById(R.id.messageInput);

        sendBtn.setOnClickListener(v -> {
            String token = tokenIn.getText().toString();
            String chat = chatIn.getText().toString();
            String msg = msgIn.getText().toString();

            if (token.isEmpty() || chat.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                return;
            }

            String urlString = "https://api.telegram.org/bot" + token 
                             + "/sendMessage?chat_id=" + chat 
                             + "&text=" + msg;

            new Thread(() -> {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        runOnUiThread(() -> Toast.makeText(this, "✅ تم الإرسال بنجاح!", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "❌ فشل الإرسال: تأكد من التوكن", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "❌ خطأ في الشبكة", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }
}
