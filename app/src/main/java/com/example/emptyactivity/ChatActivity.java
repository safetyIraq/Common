package com.example.emptyactivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private String friendUid, myUid, chatRoom;
    private DatabaseReference mDb;
    private TextView chatLog;
    private EditText chatInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendUid = getIntent().getStringExtra("friendUid");
        myUid = FirebaseAuth.getInstance().getUid();
        mDb = FirebaseDatabase.getInstance().getReference();
        
        // غرفة دردشة فريدة
        chatRoom = myUid.compareTo(friendUid) < 0 ? myUid + friendUid : friendUid + myUid;

        chatLog = findViewById(R.id.chatLog);
        chatInput = findViewById(R.id.msgInput);

        mDb.child("Chats").child(chatRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder b = new StringBuilder();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String sender = ds.child("sender").getValue().toString();
                    String text = ds.child("text").getValue().toString();
                    b.append(sender.equals(myUid) ? "● أنت: " : "○ صديقك: ");
                    b.append(text).append("\n\n");
                }
                chatLog.setText(b.toString());
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        findViewById(R.id.sendMsgBtn).setOnClickListener(v -> {
            String t = chatInput.getText().toString().trim();
            if (t.isEmpty()) return;
            HashMap<String, Object> m = new HashMap<>();
            m.put("sender", myUid);
            m.put("text", t);
            mDb.child("Chats").child(chatRoom).push().setValue(m);
            chatInput.setText("");
        });
    }
}
