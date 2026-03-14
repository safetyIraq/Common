package com.example.emptyactivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText regEmail, regPass, regUser;
    private TextInputLayout layoutUser;
    private MaterialButton mainActionBtn;
    private TextView tvTitle, tvSwitchPrefix, tvSwitchAction;
    private ProgressBar loadingView;
    private View authView, dashboardView;
    private ImageButton btnGoogle, btnFacebook;

    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private boolean isLoginMode = true; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط الواجهة بالكود
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        layoutUser = findViewById(R.id.layoutUser);
        mainActionBtn = findViewById(R.id.mainActionBtn);
        tvTitle = findViewById(R.id.tvTitle);
        tvSwitchPrefix = findViewById(R.id.tvSwitchPrefix);
        tvSwitchAction = findViewById(R.id.tvSwitchAction);
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();

        // تفعيل زر تبديل الواجهة (دخول / تسجيل)
        findViewById(R.id.switchModeLayout).setOnClickListener(v -> switchMode());

        // تفعيل الزر الرئيسي مع التحقق
        mainActionBtn.setOnClickListener(v -> validateAndExecute());

        // أزرار السوشيال
        btnGoogle.setOnClickListener(v -> Toast.makeText(this, "سيتم تفعيل جوجل قريباً!", Toast.LENGTH_SHORT).show());
        btnFacebook.setOnClickListener(v -> Toast.makeText(this, "سيتم تفعيل فيسبوك قريباً!", Toast.LENGTH_SHORT).show());
    }

    private void switchMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText("تسجيل الدخول");
            mainActionBtn.setText("تسجيل الدخول");
            layoutUser.setVisibility(View.GONE);
            tvSwitchPrefix.setText("ليس لديك حساب؟ ");
            tvSwitchAction.setText("إنشاء حساب جديد");
        } else {
            tvTitle.setText("إنشاء حساب");
            mainActionBtn.setText("إنشاء حساب");
            layoutUser.setVisibility(View.VISIBLE);
            tvSwitchPrefix.setText("لديك حساب بالفعل؟ ");
            tvSwitchAction.setText("تسجيل الدخول");
        }
    }

    private void validateAndExecute() {
        String email = regEmail.getText().toString().trim();
        String pass = regPass.getText().toString().trim();
        String user = regUser.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("يرجى إدخال بريد إلكتروني صحيح");
            return;
        }

        if (pass.isEmpty() || pass.length() < 6) {
            regPass.setError("يجب أن تكون كلمة المرور 6 أحرف على الأقل");
            return;
        }

        if (!isLoginMode && user.isEmpty()) {
            regUser.setError("يرجى اختيار يوزر نيم");
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        mainActionBtn.setEnabled(false);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "مرحباً بك مجدداً!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                } else {
                    resetUI("خطأ: تأكد من بياناتك.");
                }
            });
        } else {
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getUid();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", user.toLowerCase());
                    map.put("email", email);
                    mDb.child("Users").child(uid).setValue(map);
                    mDb.child("Usernames").child(user.toLowerCase()).setValue(uid);
                    Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                } else {
                    resetUI("فشل إنشاء الحساب.");
                }
            });
        }
    }

    private void resetUI(String errorMsg) {
        loadingView.setVisibility(View.GONE);
        mainActionBtn.setEnabled(true);
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void goToDashboard() {
        loadingView.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        dashboardView.setVisibility(View.VISIBLE);
    }
}
