package com.example.emptyactivity;

import android.content.*;
import android.os.Bundle;
import androidx.core.app.RemoteInput;

public class ReplyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle res = RemoteInput.getResultsFromIntent(intent);
        if (res != null) {
            CharSequence txt = res.getCharSequence("rep");
            SharedPreferences p = context.getSharedPreferences("HussainData", Context.MODE_PRIVATE);
            String t = p.getString("token", ""), c = p.getString("chat", "");
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + t + "/sendMessage?chat_id=" + c + "&text=ردي: " + txt);
                    ((java.net.HttpURLConnection) url.openConnection()).getResponseCode();
                } catch (Exception ignored) {}
            }).start();
        }
    }
}
