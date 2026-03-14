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
    private EditText regName, regUser, searchField;
    private TextView friendNameTxt;
    private DatabaseReference mDb;
    private FirebaseAuth mAuth;
    private String foundFriendUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        friendCard = findViewById(R.id.friendCard);
        regName = findViewById(R.id.regName);
        regUser = findViewById(R.id.regUser);
        searchField = findViewById(R.id.searchField);
        friendNameTxt = findViewById(R.id.friendNameTxt);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        new Handler().postDelayed(() -> {
            loadingView.setVisibility(View.GONE);
            if (mAuth.getCurrentUser() == null) authView.setVisibility(View.VISIBLE);
            else dashboardView.setVisibility(View.VISIBLE);
        }, 3000);

        findViewById(R.id.registerBtn).setOnClickListener(v -> register());
        findViewById(R.id.searchBtn).setOnClickListener(v -> searchFriend());
        findViewById(R.id.startChatBtn).setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("friendUid", foundFriendUid);
            startActivity(i);
        });
    }

    private void register() {
        String name = regName.getText().toString().trim();
        String user = regUser.getText().toString().toLowerCase().trim();
        if (name.isEmpty() || user.isEmpty()) return;

        mAuth.signInAnonymously().addOnSuccessListener(r -> {
            String uid = mAuth.getUid();
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name); map.put("username", user);
            mDb.child("Users").child(uid).setValue(map);
            mDb.child("Usernames").child(user).setValue(uid);
            authView.setVisibility(View.GONE); dashboardView.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void searchFriend() {
        String query = searchField.getText().toString().toLowerCase().trim();
        mDb.child("Usernames").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot s) {
                if (s.exists()) {
                    foundFriendUid = s.getValue(String.class);
                    mDb.child("Users").child(foundFriendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot us) {
                            friendNameTxt.setText(us.child("name").getValue(String.class));
                            friendCard.setVisibility(View.VISIBLE);
                        }
                        @Override public void onCancelled(DatabaseError e) {}
                    });
                } else Toast.makeText(MainActivity.this, "غير موجود!", Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }
}
