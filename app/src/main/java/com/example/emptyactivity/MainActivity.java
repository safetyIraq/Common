package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
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

    private TextInputEditText regEmail, regPass, regUser;
    private TextInputLayout layoutUser, layoutEmail, layoutPass;
    private MaterialButton mainActionBtn, btnGoogle, btnFacebook;
    private TextView tvTitle, tvSwitchPrefix, tvSwitchAction, tvForgotPassword;
    private ProgressBar loadingView;
    private View authView, dashboardView;
    private MaterialCheckBox showPasswordCheckBox;

    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private boolean isLoginMode = true;
    private GoogleSignInClient mGoogleSignInClient;

    // قواعد التحقق من كلمة المرور القوية
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
    
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            resetUI("فشل الحصول على بيانات حساب جوجل");
                        }
                    } catch (ApiException e) {
                        handleAuthError("فشل تسجيل الدخول بجوجل", e);
                    }
                } else {
                    resetUI("تم إلغاء تسجيل الدخول بجوجل");
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
        checkCurrentUser();
    }

    private void initViews() {
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        layoutUser = findViewById(R.id.layoutUser);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPass = findViewById(R.id.layoutPass);
        mainActionBtn = findViewById(R.id.mainActionBtn);
        tvTitle = findViewById(R.id.tvTitle);
        tvSwitchPrefix = findViewById(R.id.tvSwitchPrefix);
        tvSwitchAction = findViewById(R.id.tvSwitchAction);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        dashboardView = findViewById(R.id.dashboardView);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);

        setupPasswordVisibility();
    }

    private void setupPasswordVisibility() {
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                regPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                regPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            regPass.setSelection(regPass.getText().length());
        });
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
        btnFacebook.setOnClickListener(v -> showFeatureInDevelopment());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
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
        if (idToken == null) {
            resetUI("رمز المصادقة غير صالح");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
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
        String photoUrl = mAuth.getCurrentUser().getPhotoUrl() != null ? 
                          mAuth.getCurrentUser().getPhotoUrl().toString() : null;

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("lastLogin", ServerValue.TIMESTAMP);
        
        if (name != null) {
            String username = generateUsernameFromEmail(email);
            userMap.put("displayName", name);
            userMap.put("username", username);
        }
        
        if (photoUrl != null) {
            userMap.put("photoUrl", photoUrl);
        }
        
        userMap.put("provider", "google");
        userMap.put("emailVerified", true);

        mDb.child("Users").child(uid).updateChildren(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, 
                        "مرحباً بك " + (name != null ? name : "!") , Toast.LENGTH_SHORT).show();
                    goToDashboard();
                })
                .addOnFailureListener(e -> {
                    resetUI("تم تسجيل الدخول ولكن فشل حفظ البيانات");
                });
    }

    private String generateUsernameFromEmail(String email) {
        String username = email.split("@")[0];
        return username.toLowerCase().replaceAll("[^a-z0-9._-]", "");
    }

    private void switchMode() {
        isLoginMode = !isLoginMode;
        
        // إضافة تأثير حركي بسيط
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        
        if (isLoginMode) {
            tvTitle.setText("تسجيل الدخول");
            mainActionBtn.setText("تسجيل الدخول");
            layoutUser.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
            tvSwitchPrefix.setText("ليس لديك حساب؟ ");
            tvSwitchAction.setText("إنشاء حساب جديد");
        } else {
            tvTitle.setText("إنشاء حساب");
            mainActionBtn.setText("إنشاء حساب");
            layoutUser.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            tvSwitchPrefix.setText("لديك حساب بالفعل؟ ");
            tvSwitchAction.setText("تسجيل الدخول");
        }
        
        // مسح الأخطاء السابقة
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

        if (!validateEmail(email)) return;
        if (!validatePassword(pass)) return;
        if (!isLoginMode && !validateUsername(user)) return;

        setLoadingState(true);

        if (isLoginMode) {
            loginUser(email, pass);
        } else {
            registerUser(email, pass, user);
        }
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            regEmail.setError("البريد الإلكتروني مطلوب");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("صيغة البريد الإلكتروني غير صحيحة");
            return false;
        }
        if (email.length() > 100) {
            regEmail.setError("البريد الإلكتروني طويل جداً");
            return false;
        }
        regEmail.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            regPass.setError("كلمة المرور مطلوبة");
            return false;
        }
        
        if (isLoginMode) {
            if (password.length() < 6) {
                regPass.setError("كلمة المرور يجب أن تكون 6 أحرف على الأقل");
                return false;
            }
        } else {
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                regPass.setError("كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل، حرف كبير، حرف صغير، رقم، ورمز خاص");
                return false;
            }
        }
        
        regPass.setError(null);
        return true;
    }

    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            regUser.setError("اسم المستخدم مطلوب");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            regUser.setError("اسم المستخدم يجب أن يكون 3-20 حرفاً، ويمكن أن يحتوي على أحرف وأرقام و._- فقط");
            return false;
        }
        regUser.setError(null);
        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handleSuccessfulLogin();
                    } else {
                        handleAuthError("فشل تسجيل الدخول", task.getException());
                    }
                });
    }

    private void handleSuccessfulLogin() {
        updateLastLogin();
        Toast.makeText(this, "مرحباً بعودتك!", Toast.LENGTH_SHORT).show();
        goToDashboard();
    }

    private void registerUser(String email, String password, String username) {
        // التحقق من توفر اسم المستخدم أولاً
        mDb.child("Usernames").child(username.toLowerCase()).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        resetUI("اسم المستخدم غير متاح");
                    } else {
                        createFirebaseUser(email, password, username);
                    }
                })
                .addOnFailureListener(e -> {
                    resetUI("فشل التحقق من اسم المستخدم");
                });
    }

    private void createFirebaseUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserData(username, email);
                    } else {
                        handleAuthError("فشل إنشاء الحساب", task.getException());
                    }
                });
    }

    private void saveUserData(String username, String email) {
        String uid = mAuth.getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username.toLowerCase());
        userMap.put("email", email);
        userMap.put("createdAt", ServerValue.TIMESTAMP);
        userMap.put("lastLogin", ServerValue.TIMESTAMP);
        userMap.put("accountType", "email");
        userMap.put("emailVerified", false);

        mDb.child("Users").child(uid).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    mDb.child("Usernames").child(username.toLowerCase()).setValue(uid)
                            .addOnSuccessListener(aVoid2 -> {
                                sendEmailVerification();
                                Toast.makeText(this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_SHORT).show();
                                goToDashboard();
                            })
                            .addOnFailureListener(e -> {
                                // Rollback user creation if username reservation fails
                                mAuth.getCurrentUser().delete();
                                resetUI("فشل في حفظ اسم المستخدم");
                            });
                })
                .addOnFailureListener(e -> {
                    mAuth.getCurrentUser().delete();
                    resetUI("فشل في حفظ بيانات المستخدم");
                });
    }

    private void sendEmailVerification() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "تم إرسال رابط التفعيل إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "فشل إرسال رابط التفعيل", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateLastLogin() {
        if (mAuth.getCurrentUser() != null) {
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("lastLogin", ServerValue.TIMESTAMP);
            mDb.child("Users").child(mAuth.getCurrentUser().getUid())
                    .updateChildren(updates);
        }
    }

    private void handleForgotPassword() {
        String email = regEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "الرجاء إدخال بريدك الإلكتروني", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "الرجاء إدخال بريد إلكتروني صحيح", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, 
                            "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني", 
                            Toast.LENGTH_LONG).show();
                    } else {
                        handleAuthError("فشل إرسال رابط إعادة التعيين", task.getException());
                    }
                });
    }

    private void handleAuthError(String defaultMessage, Exception exception) {
        String errorMessage = defaultMessage;
        
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "البريد الإلكتروني أو كلمة المرور غير صحيحة";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "البريد الإلكتروني مستخدم بالفعل";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "كلمة المرور ضعيفة جداً";
        } else if (exception instanceof ApiException) {
            int statusCode = ((ApiException) exception).getStatusCode();
            if (statusCode == 12500 || statusCode == 12501) {
                errorMessage = "فشل الاتصال بخدمة جوجل";
            }
        } else if (exception != null) {
            errorMessage = exception.getMessage();
        }
        
        resetUI(errorMessage);
    }

    private void showFeatureInDevelopment() {
        Toast.makeText(this, 
            "هذه الميزة قيد التطوير وستتوفر قريباً!", 
            Toast.LENGTH_LONG).show();
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            loadingView.setVisibility(View.VISIBLE);
            mainActionBtn.setEnabled(false);
            btnGoogle.setEnabled(false);
            btnFacebook.setEnabled(false);
        } else {
            loadingView.setVisibility(View.GONE);
            mainActionBtn.setEnabled(true);
            btnGoogle.setEnabled(true);
            btnFacebook.setEnabled(true);
        }
    }

    private void resetUI(String errorMsg) {
        setLoadingState(false);
        if (errorMsg != null && !errorMsg.isEmpty()) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void goToDashboard() {
        setLoadingState(false);
        
        // تأثير انتقالي
        Animation slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        
        authView.startAnimation(slideOut);
        authView.setVisibility(View.GONE);
        
        dashboardView.startAnimation(slideIn);
        dashboardView.setVisibility(View.VISIBLE);
        
        // تحديث واجهة المستخدم بعد الدخول
        updateDashboardUI();
    }

    private void updateDashboardUI() {
        TextView welcomeText = findViewById(R.id.welcomeText);
        TextView userEmailText = findViewById(R.id.userEmailText);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        
        if (mAuth.getCurrentUser() != null) {
            String displayName = mAuth.getCurrentUser().getDisplayName();
            String email = mAuth.getCurrentUser().getEmail();
            
            if (displayName != null && !displayName.isEmpty()) {
                welcomeText.setText("مرحباً " + displayName);
            } else {
                welcomeText.setText("مرحباً بك في التطبيق");
            }
            
            userEmailText.setText(email);
            
            logoutButton.setOnClickListener(v -> logout());
        }
    }

    private void logout() {
        setLoadingState(true);
        
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    mAuth.signOut();
                    setLoadingState(false);
                    
                    Animation slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
                    Animation slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
                    
                    dashboardView.startAnimation(slideOut);
                    dashboardView.setVisibility(View.GONE);
                    
                    authView.startAnimation(slideIn);
                    authView.setVisibility(View.VISIBLE);
                    
                    clearFields();
                    
                    Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        regEmail.setText("");
        regPass.setText("");
        regUser.setText("");
        clearErrors();
        showPasswordCheckBox.setChecked(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // تحديث حالة المستخدم عند العودة للتطبيق
        if (mAuth.getCurrentUser() != null && dashboardView.getVisibility() != View.VISIBLE) {
            goToDashboard();
        }
    }

    @Override
    public void onBackPressed() {
        if (dashboardView.getVisibility() == View.VISIBLE) {
            // تأكيد الخروج من التطبيق
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }
            }
