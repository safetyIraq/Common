package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // Views
    private TextInputEditText regEmail, regPass, regUser;
    private TextInputLayout layoutUser;
    private MaterialButton mainActionBtn, btnGoogle, btnFacebook;
    private TextView tvTitle, tvSwitchPrefix, tvSwitchAction, tabContentText, profileName;
    private ProgressBar loadingView;
    private View authView, dashboardView, layoutSettings, layoutProfile, layoutChats;
    private BottomNavigationView bottomNavigation;
    private Toolbar mainToolbar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;

    // State
    private boolean isLoginMode = true;
    private GoogleSignInClient mGoogleSignInClient;

    // Constants
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");

    // Activity Result Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        handleAuthError("فشل تسجيل الدخول بجوجل", e);
                    }
                } else {
                    resetUI("تم إلغاء العملية");
                }
            }
    );

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

    private void initViews() {
        // Auth Views
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
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        // Dashboard Views
        dashboardView = findViewById(R.id.dashboardView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        mainToolbar = findViewById(R.id.mainToolbar);
        tabContentText = findViewById(R.id.tabContentText);
        layoutSettings = findViewById(R.id.layoutSettings);
        layoutProfile = findViewById(R.id.layoutProfile);
        layoutChats = findViewById(R.id.layoutChats);
        profileName = findViewById(R.id.profileName);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        findViewById(R.id.switchModeLayout).setOnClickListener(v -> switchMode());
        mainActionBtn.setOnClickListener(v -> validateAndExecute());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebook.setOnClickListener(v -> Toast.makeText(this, "قريباً!", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Hide all layouts
            layoutSettings.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.GONE);
            layoutChats.setVisibility(View.GONE);

            if (id == R.id.nav_chats) {
                mainToolbar.setTitle("المحادثات");
                layoutChats.setVisibility(View.VISIBLE);
                tabContentText.setText("قائمة المحادثات ستظهر هنا");
            } else if (id == R.id.nav_contacts) {
                mainToolbar.setTitle("جهات الاتصال");
                layoutChats.setVisibility(View.VISIBLE);
                tabContentText.setText("قائمة جهات الاتصال ستظهر هنا");
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

    private void checkCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            goToDashboard();
        }
    }

    private void signInWithGoogle() {
        setLoadingState(true);
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                handleSuccessfulGoogleSignIn();
            } else {
                handleAuthError("فشل المصادقة مع السيرفر", task.getException());
            }
        });
    }

    private void handleSuccessfulGoogleSignIn() {
        String uid = mAuth.getUid();
        String email = mAuth.getCurrentUser().getEmail();
        String name = mAuth.getCurrentUser().getDisplayName();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("lastLogin", ServerValue.TIMESTAMP);
        
        if (name != null) {
            userMap.put("displayName", name);
            userMap.put("username", name.toLowerCase().replaceAll("[^a-z0-9._-]", ""));
        }

        mDb.child("Users").child(uid).updateChildren(userMap)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(MainActivity.this, "مرحباً بك!", Toast.LENGTH_SHORT).show();
                goToDashboard();
            })
            .addOnFailureListener(e -> resetUI("فشل حفظ البيانات"));
    }

    private void switchMode() {
        isLoginMode = !isLoginMode;
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        
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
        
        clearErrors();
        layoutUser.startAnimation(fadeIn);
    }

    private void clearErrors() {
        regEmail.setError(null);
        regPass.setError(null);
        regUser.setError(null);
    }

    private void validateAndExecute() {
        String email = regEmail.getText().toString().trim();
        String pass = regPass.getText().toString().trim();
        String user = regUser.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("صيغة البريد غير صحيحة"); 
            return;
        }

        if (TextUtils.isEmpty(pass) || (isLoginMode && pass.length() < 6)) {
            regPass.setError("كلمة المرور قصيرة جداً"); 
            return;
        } else if (!isLoginMode && !PASSWORD_PATTERN.matcher(pass).matches()) {
            regPass.setError("يجب أن تحتوي على حروف كبيرة، صغيرة، أرقام، ورموز"); 
            return;
        }

        if (!isLoginMode && (TextUtils.isEmpty(user) || !USERNAME_PATTERN.matcher(user).matches())) {
            regUser.setError("اسم المستخدم يجب أن يكون 3-20 حرفاً (إنجليزي وأرقام فقط)"); 
            return;
        }

        setLoadingState(true);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "مرحباً بعودتك!", Toast.LENGTH_SHORT).show();
                        goToDashboard();
                    } else {
                        handleAuthError("فشل تسجيل الدخول", task.getException());
                    }
                });
        } else {
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserData(user, email);
                    } else {
                        handleAuthError("فشل إنشاء الحساب", task.getException());
                    }
                });
        }
    }

    private void saveUserData(String username, String email) {
        String uid = mAuth.getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username.toLowerCase());
        userMap.put("email", email);
        userMap.put("createdAt", ServerValue.TIMESTAMP);

        mDb.child("Users").child(uid).setValue(userMap)
            .addOnSuccessListener(aVoid -> {
                mDb.child("Usernames").child(username.toLowerCase()).setValue(uid);
                Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_SHORT).show();
                goToDashboard();
            })
            .addOnFailureListener(e -> resetUI("فشل حفظ بيانات المستخدم"));
    }

    private void handleAuthError(String defaultMessage, Exception exception) {
        String errorMessage = defaultMessage;
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "البريد الإلكتروني أو كلمة المرور غير صحيحة";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "البريد الإلكتروني مستخدم بالفعل";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "كلمة المرور ضعيفة جداً";
        }
        resetUI(errorMessage);
    }

    private void setLoadingState(boolean isLoading) {
        loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mainActionBtn.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
        btnFacebook.setEnabled(!isLoading);
    }

    private void resetUI(String errorMsg) {
        setLoadingState(false);
        if (errorMsg != null && !errorMsg.isEmpty()) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void goToDashboard() {
        setLoadingState(false);
        
        Animation slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        
        authView.startAnimation(slideOut);
        authView.setVisibility(View.GONE);
        
        // Set profile name
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            profileName.setText(mAuth.getCurrentUser().getDisplayName().toUpperCase() + " •");
        }
        
        dashboardView.startAnimation(slideIn);
        dashboardView.setVisibility(View.VISIBLE);
        mainToolbar.setTitle("المحادثات");
    }
}
