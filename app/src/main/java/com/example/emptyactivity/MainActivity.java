package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
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

    // متغيرات جوجل
    private GoogleSignInClient mGoogleSignInClient;

    // نظام الأندرويد الحديث لاستقبال نتيجة تسجيل الدخول من جوجل
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        resetUI("فشل تسجيل الدخول بجوجل: " + e.getMessage());
                    }
                } else {
                    resetUI("تم إلغاء تسجيل الدخول.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ربط الواجهة
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

        // إعداد خيارات جوجل
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // هذا الكود يجيبه الفايربيس تلقائياً
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // تفعيل الأزرار
        findViewById(R.id.switchModeLayout).setOnClickListener(v -> switchMode());
        mainActionBtn.setOnClickListener(v -> validateAndExecute());
        
        // تفعيل زر جوجل
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        
        btnFacebook.setOnClickListener(v -> Toast.makeText(this, "سيتم تفعيل فيسبوك قريباً!", Toast.LENGTH_SHORT).show());
    }

    // --- دوال جوجل ---
    private void signInWithGoogle() {
        loadingView.setVisibility(View.VISIBLE);
        mainActionBtn.setEnabled(false);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // حفظ بيانات المستخدم في قاعدة البيانات
                String uid = mAuth.getUid();
                String email = mAuth.getCurrentUser().getEmail();
                String name = mAuth.getCurrentUser().getDisplayName(); // يجيب اسمه من جوجل

                HashMap<String, Object> map = new HashMap<>();
                map.put("email", email);
                if(name != null) map.put("username", name.toLowerCase().replace(" ", "")); // نسويله يوزر من اسمه

                mDb.child("Users").child(uid).updateChildren(map);

                Toast.makeText(MainActivity.this, "تم الدخول بحساب جوجل بنجاح!", Toast.LENGTH_SHORT).show();
                goToDashboard();
            } else {
                resetUI("فشل المصادقة مع السيرفر.");
            }
        });
    }

    // --- دوال الايميل والباسورد (نفسها بدون تغيير) ---
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
            regEmail.setError("يرجى إدخال بريد إلكتروني صحيح"); return;
        }
        if (pass.isEmpty() || pass.length() < 6) {
            regPass.setError("يجب أن تكون كلمة المرور 6 أحرف على الأقل"); return;
        }
        if (!isLoginMode && user.isEmpty()) {
            regUser.setError("يرجى اختيار يوزر نيم"); return;
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
        if(!errorMsg.isEmpty()) Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void goToDashboard() {
        loadingView.setVisibility(View.GONE);
        authView.setVisibility(View.GONE);
        dashboardView.setVisibility(View.VISIBLE);
    }
}
