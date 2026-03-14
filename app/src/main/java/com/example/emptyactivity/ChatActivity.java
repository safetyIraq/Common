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
    private TextView chatLog; // يمكنك لاحقاً استخدام RecyclerView للأفضل
    private EditText chatInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendUid = getIntent().getStringExtra("friendUid");
        myUid = FirebaseAuth.getInstance().getUid();
        mDb = FirebaseDatabase.getInstance().getReference();

        // إنشاء معرف غرفة فريد
        chatRoom = myUid.compareTo(friendUid) < 0 ? myUid + friendUid : friendUid + myUid;

        chatInput = findViewById(R.id.chatInput);
        // للتسهيل الآن سنستخدم TextView لعرض كل الرسائل
        chatLog = new TextView(this); // هذا مثال بسيط، الكود الحقيقي يستخدم RecyclerView

        mDb.child("Chats").child(chatRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // هنا يتم تحديث المحادثة فوراً عند وصول رسالة
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        findViewById(R.id.sendBtn).setOnClickListener(v -> {
            String msg = chatInput.getText().toString().trim();
            if (msg.isEmpty()) return;

            HashMap<String, Object> map = new HashMap<>();
            map.put("sender", myUid);
            map.put("text", msg);
            map.put("time", System.currentTimeMillis());

            mDb.child("Chats").child(chatRoom).push().setValue(map);
            chatInput.setText("");
        });
    }
}
