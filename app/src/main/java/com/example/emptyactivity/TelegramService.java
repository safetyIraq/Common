package com.example.emptyactivity;

import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.os.*;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import org.json.JSONArray;
import org.json.JSONObject;

public class TelegramService extends Service {
    private int lastId = 0;
    private static final String CH_ID = "h_ch";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannels();
        Notification n = new NotificationCompat.Builder(this, CH_ID).setContentTitle("Hussain Ultra").setContentText("مراقب الإشعارات شغال...").setSmallIcon(android.R.drawable.stat_notify_sync).build();
        startForeground(1, n);
        new Thread(this::loop).start();
        return START_STICKY;
    }

    private void loop() {
        SharedPreferences p = getSharedPreferences("HussainData", MODE_PRIVATE);
        while (true) {
            String t = p.getString("token", "");
            if (!t.isEmpty()) {
                try {
                    java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + t + "/getUpdates?offset=" + (lastId + 1));
                    java.util.Scanner s = new java.util.Scanner(url.openStream()).useDelimiter("\\A");
                    JSONArray res = new JSONObject(s.next()).getJSONArray("result");
                    if (res.length() > 0) {
                        JSONObject last = res.getJSONObject(res.length() - 1);
                        lastId = last.getInt("update_id");
                        showNote(last.getJSONObject("message").getString("text"));
                    }
                    Thread.sleep(10000);
                } catch (Exception e) { try { Thread.sleep(5000); } catch (Exception ignored) {} }
            }
        }
    }

    private void showNote(String m) {
        RemoteInput ri = new RemoteInput.Builder("rep").setLabel("رد سريع...").build();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, ReplyReceiver.class), PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action act = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send, "رد", pi).addRemoteInput(ri).build();
        Notification n = new NotificationCompat.Builder(this, CH_ID).setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle("رسالة جديدة").setContentText(m)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).addAction(act).setAutoCancel(true).build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(2, n);
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CH_ID, "Hussain", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    @Override public IBinder onBind(Intent i) { return null; }
}

