package com.example.emptyactivity;

import android.os.Bundle;
import android.widget.Button;
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
    private EditText msgInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendUid = getIntent().getStringExtra("friendUid");
        myUid = FirebaseAuth.getInstance().getUid();
        mDb = FirebaseDatabase.getInstance().getReference();
        chatRoom = myUid.compareTo(friendUid) < 0 ? myUid + friendUid : friendUid + myUid;

        chatLog = findViewById(R.id.chatLog);
        msgInput = findViewById(R.id.msgInput);

        mDb.child("Chats").child(chatRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot s) {
                StringBuilder b = new StringBuilder();
                for (DataSnapshot ds : s.getChildren()) {
                    String sender = ds.child("sender").getValue().toString();
                    b.append(sender.equals(myUid) ? "● أنت: " : "○ صديقك: ");
                    b.append(ds.child("text").getValue().toString()).append("\n\n");
                }
                chatLog.setText(b.toString());
            }
            @Override public void onCancelled(DatabaseError e) {}
        });

        findViewById(R.id.sendMsgBtn).setOnClickListener(v -> {
            String t = msgInput.getText().toString().trim();
            if (t.isEmpty()) return;
            HashMap<String, Object> m = new HashMap<>();
            m.put("sender", myUid); m.put("text", t);
            mDb.child("Chats").child(chatRoom).push().setValue(m);
            msgInput.setText("");
        });
    }
}
