package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    // ============================================================
    // متغيرات الواجهة
    // ============================================================
    // حقول الإدخال
    private TextInputEditText regEmail, regPass, regUser;
    private TextInputLayout layoutUser;

    // الأزرار
    private MaterialButton mainActionBtn, btnGoogle, btnFacebook;

    // النصوص
    private TextView tvSwitchAction, tvTitle, profileName, layoutChats;

    // مؤشر التحميل
    private ProgressBar loadingView;

    // الحاويات الرئيسية
    private View authView, dashboardView, layoutSettings, layoutProfile;

    // شريط التنقل والأدوات
    private BottomNavigationView bottomNavigation;
    private Toolbar mainToolbar;

    // ============================================================
    // متغيرات Firebase
    // ============================================================
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private GoogleSignInClient mGoogleSignInClient;

    // ============================================================
    // متغيرات الحالة
    // ============================================================
    private boolean isLoginMode = true;

    // ============================================================
    // Launcher لتسجيل الدخول بجوجل
    // ============================================================
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        resetUI("فشل تسجيل الدخول بجوجل");
                    }
                } else {
                    resetUI("تم إلغاء العملية");
                }
            }
    );

    // ============================================================
    // دورة حياة النشاط
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupFirebase();
        setupGoogleSignIn();
        setupClickListeners();
        setupBottomNavigation();
        checkCurrentUser();
    }

    // ============================================================
    // تهيئة عناصر الواجهة
    // ============================================================
    private void initViews() {
        // حقول الإدخال
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        layoutUser = findViewById(R.id.layoutUser);

        // الأزرار
        mainActionBtn = findViewById(R.id.mainActionBtn);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        // النصوص
        tvSwitchAction = findViewById(R.id.tvSwitchAction);
        tvTitle = findViewById(R.id.tvTitle);
        profileName = findViewById(R.id.profileName);
        layoutChats = findViewById(R.id.layoutChats);

        // مؤشر التحميل
        loadingView = findViewById(R.id.loadingView);

        // الحاويات
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        layoutSettings = findViewById(R.id.layoutSettings);
        layoutProfile = findViewById(R.id.layoutProfile);

        // شريط التنقل والأدوات
        bottomNavigation = findViewById(R.id.bottomNavigation);
        mainToolbar = findViewById(R.id.mainToolbar);
    }

    // ============================================================
    // إعداد شريط التنقل السفلي
    // ============================================================
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // إخفاء جميع الشاشات أولاً
            layoutSettings.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.GONE);
            layoutChats.setVisibility(View.GONE);

            // إظهار الشاشة المحددة
            if (id == R.id.nav_chats) {
                mainToolbar.setTitle("المحادثات");
                layoutChats.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_contacts) {
                mainToolbar.setTitle("جهات الاتصال");
                layoutChats.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_settings) {
                mainToolbar.setTitle("الإعدادات");
                layoutSettings.setVisibility(View.VISIBLE);
            } else if (id == R.id.nav_profile) {
                mainToolbar.setTitle("");
                layoutProfile.setVisibility(View.VISIBLE);
            }
            return true;
        });
    }

    // ============================================================
    // إعداد Firebase
    // ============================================================
    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();
    }

    // ============================================================
    // إعداد تسجيل الدخول بجوجل
    // ============================================================
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ============================================================
    // إعداد مستمعي الأحداث
    // ============================================================
    private void setupClickListeners() {
        findViewById(R.id.switchModeLayout).setOnClickListener(v -> switchMode());
        mainActionBtn.setOnClickListener(v -> validateAndExecute());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "تسجيل فيسبوك سيتوفر قريباً", Toast.LENGTH_SHORT).show()
        );
    }

    // ============================================================
    // التحقق من وجود مستخدم حالي
    // ============================================================
    private void checkCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            goToDashboard();
        }
    }

    // ============================================================
    // تسجيل الدخول بجوجل
    // ============================================================
    private void signInWithGoogle() {
        setLoadingState(true);
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // ============================================================
    // مصادقة Firebase باستخدام جوجل
    // ============================================================
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "تم الدخول بنجاح", Toast.LENGTH_SHORT).show();
                goToDashboard();
            } else {
                resetUI("فشل المصادقة");
            }
        });
    }

    // ============================================================
    // التبديل بين وضعي تسجيل الدخول وإنشاء الحساب
    // ============================================================
    private void switchMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            // وضع تسجيل الدخول
            tvTitle.setText("تسجيل الدخول");
            mainActionBtn.setText("تسجيل الدخول");
            tvSwitchAction.setText("إنشاء حساب جديد");
            layoutUser.setVisibility(View.GONE);
        } else {
            // وضع إنشاء حساب
            tvTitle.setText("إنشاء حساب");
            mainActionBtn.setText("إنشاء حساب");
            tvSwitchAction.setText("تسجيل الدخول");
            layoutUser.setVisibility(View.VISIBLE);
        }
    }

    // ============================================================
    // التحقق من صحة البيانات وتنفيذ الإجراء
    // ============================================================
    private void validateAndExecute() {
        String email = regEmail.getText().toString().trim();
        String pass = regPass.getText().toString().trim();

        // التحقق من صحة البريد الإلكتروني
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("صيغة غير صحيحة");
            return;
        }

        // التحقق من صحة كلمة المرور
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            regPass.setError("كلمة المرور قصيرة");
            return;
        }

        setLoadingState(true);

        if (isLoginMode) {
            // تسجيل الدخول
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    goToDashboard();
                } else {
                    resetUI("تأكد من بياناتك");
                }
            });
        } else {
            // إنشاء حساب جديد
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    goToDashboard();
                } else {
                    resetUI("فشل إنشاء الحساب");
                }
            });
        }
    }

    // ============================================================
    // التحكم في حالة التحميل
    // ============================================================
    private void setLoadingState(boolean isLoading) {
        loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mainActionBtn.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
        btnFacebook.setEnabled(!isLoading);
    }

    // ============================================================
    // إعادة تعيين الواجهة مع عرض رسالة خطأ
    // ============================================================
    private void resetUI(String errorMsg) {
        setLoadingState(false);
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    // ============================================================
    // الانتقال إلى لوحة التحكم
    // ============================================================
    private void goToDashboard() {
        authView.setVisibility(View.GONE);
        dashboardView.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            profileName.setText(mAuth.getCurrentUser().getDisplayName().toUpperCase() + " •");
        }

        mainToolbar.setTitle("المحادثات");
        layoutSettings.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.GONE);
        layoutChats.setVisibility(View.VISIBLE);
        bottomNavigation.setSelectedItemId(R.id.nav_chats);
    }
}
