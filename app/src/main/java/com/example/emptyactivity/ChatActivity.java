package com.example.emptyactivity;

import android.content.Intent;
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
        
        try {
            setContentView(R.layout.activity_chat);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في تحميل واجهة المحادثة", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // استقبال البيانات مع التحقق
        Intent intent = getIntent();
        if (intent != null) {
            receiverId = intent.getStringExtra("user_id");
            receiverName = intent.getStringExtra("user_name");
            receiverImage = intent.getStringExtra("user_image");
        }

        // التحقق من البيانات
        if (receiverId == null || receiverId.isEmpty() || 
            receiverName == null || receiverName.isEmpty()) {
            Toast.makeText(this, "خطأ: بيانات المستخدم غير صحيحة", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            initViews();
            setupFirebase();
            setupChatRoom();
            loadMessages();
            setupClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في تهيئة المحادثة: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // عرض اسم المستخدم
        userName.setText(receiverName);
        
        // عرض الصورة إذا وجدت
        if (receiverImage != null && !receiverImage.isEmpty()) {
            try {
                Glide.with(this).load(receiverImage).into(userImage);
            } catch (Exception e) {
                // إذا فشل تحميل الصورة، استخدم الصورة الافتراضية
                userImage.setImageResource(R.drawable.bg_login);
            }
        }

        // إعداد RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, receiverName, receiverImage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        
        userStatus.setText("جاري الاتصال...");
    }

    private void setupFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            currentUserId = mAuth.getCurrentUser().getUid();
            mDb = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في الاتصال بـ Firebase", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupChatRoom() {
        // إنشاء معرف فريد للمحادثة
        if (currentUserId.compareTo(receiverId) < 0) {
            chatRoomId = currentUserId + "_" + receiverId;
        } else {
            chatRoomId = receiverId + "_" + currentUserId;
        }
    }

    private void loadMessages() {
        try {
            mDb.child("Chats").child(chatRoomId).child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            messageList.clear();
                            for (DataSnapshot data : snapshot.getChildren()) {
                                try {
                                    ChatMessage message = data.getValue(ChatMessage.class);
                                    if (message != null) {
                                        messageList.add(message);
                                    }
                                } catch (Exception e) {
                                    // تخطي الرسائل التالفة
                                }
                            }
                            chatAdapter.notifyDataSetChanged();
                            if (messageList.size() > 0) {
                                recyclerView.scrollToPosition(messageList.size() - 1);
                            }
                            userStatus.setText("متصل");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            userStatus.setText("غير متصل");
                        }
                    });
        } catch (Exception e) {
            userStatus.setText("غير متصل");
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
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(ChatActivity.this, "فشل في إرسال الرسالة", Toast.LENGTH_SHORT).show()
                );
    }
}
