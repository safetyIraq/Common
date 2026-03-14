package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private View loadingView, authView, dashboardView, friendCard;
    private EditText regEmail, regPass, regUser, searchField;
    private TextView friendNameTxt;
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private String foundFriendUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط العناصر
        initViews();

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        // فحص حالة الدخول
        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (mAuth.getCurrentUser() != null) {
                dashboardView.setVisibility(View.VISIBLE);
            } else {
                authView.setVisibility(View.VISIBLE);
            }
        }, 2500);

        findViewById(R.id.registerBtn).setOnClickListener(v -> handleAuth());
        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriend());
        findViewById(R.id.startChatBtn).setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("friendUid", foundFriendUid);
            startActivity(i);
        });
    }

    private void initViews() {
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        friendCard = findViewById(R.id.friendCard);
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        searchField = findViewById(R.id.searchField);
        friendNameTxt = findViewById(R.id.friendNameTxt);
    }

    private void handleAuth() {
        String email = regEmail.getText().toString().trim();
        String pass = regPass.getText().toString().trim();
        String user = regUser.getText().toString().toLowerCase().trim();

        if (email.isEmpty() || pass.length() < 6 || user.isEmpty()) {
            Toast.makeText(this, "املأ كل البيانات يا بطل!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getUid();
                HashMap<String, Object> map = new HashMap<>();
                map.put("username", user);
                mDb.child("Users").child(uid).setValue(map);
                mDb.child("Usernames").child(user).setValue(uid);
                authView.setVisibility(View.GONE);
                dashboardView.setVisibility(View.VISIBLE);
            } else {
                mAuth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(r -> {
                    authView.setVisibility(View.GONE);
                    dashboardView.setVisibility(View.VISIBLE);
                }).addOnFailureListener(e -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void searchFriend() {
        String query = searchField.getText().toString().toLowerCase().trim();
        if (query.isEmpty()) return;

        mDb.child("Usernames").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    foundFriendUid = snapshot.getValue(String.class);
                    
                    // التعديل المطلوب: إذا بحثت عن نفسك ما تطلع
                    if (foundFriendUid.equals(mAuth.getUid())) {
                        Toast.makeText(MainActivity.this, "هذا يوزرك يا حسين! ابحث عن أصدقائك.", Toast.LENGTH_SHORT).show();
                        friendCard.setVisibility(View.GONE);
                        return;
                    }

                    mDb.child("Users").child(foundFriendUid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot s) {
                            friendNameTxt.setText("تم العثور على: @" + s.getValue().toString());
                            friendCard.setVisibility(View.VISIBLE);
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
                } else {
                    Toast.makeText(MainActivity.this, "اليوزر غير موجود!", Toast.LENGTH_SHORT).show();
                    friendCard.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
