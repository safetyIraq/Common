package com.example.emptyactivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView userImage;
    private TextView userName, userStatus;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btnSend;

    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private String currentUserId;
    private String receiverId;
    private String receiverName;
    private String receiverImage;

    private List<ChatMessage> messageList;
    private ChatAdapter chatAdapter;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // استقبال البيانات من SearchActivity
        receiverId = getIntent().getStringExtra("user_id");
        receiverName = getIntent().getStringExtra("user_name");
        receiverImage = getIntent().getStringExtra("user_image");

        // التحقق من استقبال البيانات
        if (receiverId == null || receiverName == null) {
            Toast.makeText(this, "خطأ في فتح المحادثة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupFirebase();
        checkFirebaseConnection();
        setupChatRoom();
        loadMessages();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.chatToolbar);
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        userStatus = findViewById(R.id.userStatus);
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> finish());

        // عرض اسم المستخدم المستقبل
        userName.setText(receiverName != null ? receiverName : "مستخدم");
        
        // عرض صورة المستخدم إذا كانت موجودة
        if (receiverImage != null && !receiverImage.isEmpty()) {
            Glide.with(this).load(receiverImage).into(userImage);
        }

        // إعداد RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, receiverName, receiverImage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                if (connected) {
                    userStatus.setText("متصل");
                    userStatus.setTextColor(0xFF4CAF50);
                } else {
                    userStatus.setText("غير متصل");
                    userStatus.setTextColor(0xFF999999);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userStatus.setText("غير متصل");
            }
        });
    }

    private void setupChatRoom() {
        // إنشاء معرف فريد للمحادثة (أصغر ID أولاً)
        if (currentUserId.compareTo(receiverId) < 0) {
            chatRoomId = currentUserId + "_" + receiverId;
        } else {
            chatRoomId = receiverId + "_" + currentUserId;
        }
    }

    private void loadMessages() {
        mDb.child("Chats").child(chatRoomId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ChatMessage message = data.getValue(ChatMessage.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);

                        // تحديث حالة القراءة
                        markMessagesAsRead();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "خطأ في تحميل الرسائل", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markMessagesAsRead() {
        for (ChatMessage message : messageList) {
            if (message.getReceiverId().equals(currentUserId) && !message.isRead()) {
                mDb.child("Chats").child(chatRoomId).child("messages")
                        .child(message.getMessageId()).child("isRead").setValue(true);
            }
        }
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ChatMessage message = new ChatMessage(
                messageId,
                currentUserId,
                receiverId,
                messageText,
                timestamp,
                false,
                "text"
        );

        mDb.child("Chats").child(chatRoomId).child("messages").child(messageId)
                .setValue(message.toMap())
                .addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                    // تحديث آخر رسالة للمعاينة
                    updateLastMessage(messageText, timestamp);
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "فشل في إرسال الرسالة", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateLastMessage(String message, long timestamp) {
        mDb.child("Chats").child(chatRoomId).child("lastMessage").setValue(message);
        mDb.child("Chats").child(chatRoomId).child("lastTimestamp").setValue(timestamp);
    }
}
