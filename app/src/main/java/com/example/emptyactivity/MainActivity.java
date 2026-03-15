package com.example.emptyactivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // ============================================================
    // متغيرات الواجهة - شاشة المصادقة
    // ============================================================
    private TextInputEditText regEmail, regPass, regUser;
    private TextInputLayout layoutUser;
    private MaterialButton mainActionBtn, btnGoogle, btnFacebook;
    private TextView tvSwitchAction, tvTitle;
    private ProgressBar loadingView;
    private View authView;

    // ============================================================
    // متغيرات الواجهة - لوحة التحكم
    // ============================================================
    private View dashboardView, layoutSettings, layoutProfile;
    private TextView layoutChats;
    private BottomNavigationView bottomNavigation;
    private Toolbar mainToolbar;

    // ============================================================
    // متغيرات الملف الشخصي
    // ============================================================
    private ImageView profileImage;
    private TextView profileName, profileBio, profileUsernameText;
    private MaterialButton btnSetImage, btnEditProfile, btnAddPost;
    private FloatingActionButton btnChangeImage;
    private LinearLayout layoutEditName, layoutEditBio, layoutEditUsername;

    // ============================================================
    // متغيرات Firebase
    // ============================================================
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private StorageReference mStorage;
    private GoogleSignInClient mGoogleSignInClient;

    // ============================================================
    // متغيرات الحالة
    // ============================================================
    private boolean isLoginMode = true;

    // ============================================================
    // Launcher لاختيار الصور
    // ============================================================
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImage.setImageURI(uri);
                    uploadProfileImage(uri);
                }
            }
    );

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
                        if (account != null) firebaseAuthWithGoogle(account.getIdToken());
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
        // عناصر شاشة المصادقة
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regUser = findViewById(R.id.regUser);
        layoutUser = findViewById(R.id.layoutUser);
        mainActionBtn = findViewById(R.id.mainActionBtn);
        tvSwitchAction = findViewById(R.id.tvSwitchAction);
        tvTitle = findViewById(R.id.tvTitle);
        loadingView = findViewById(R.id.loadingView);
        authView = findViewById(R.id.authView);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        // عناصر لوحة التحكم
        dashboardView = findViewById(R.id.dashboardView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        mainToolbar = findViewById(R.id.mainToolbar);
        layoutSettings = findViewById(R.id.layoutSettings);
        layoutProfile = findViewById(R.id.layoutProfile);
        layoutChats = findViewById(R.id.layoutChats);

        // عناصر الملف الشخصي
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileBio = findViewById(R.id.profileBio);
        profileUsernameText = findViewById(R.id.profileUsernameText);
        btnSetImage = findViewById(R.id.btnSetImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAddPost = findViewById(R.id.btnAddPost);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        layoutEditName = findViewById(R.id.layoutEditName);
        layoutEditBio = findViewById(R.id.layoutEditBio);
        layoutEditUsername = findViewById(R.id.layoutEditUsername);
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
        mStorage = FirebaseStorage.getInstance().getReference();
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
        // أزرار شاشة المصادقة
        findViewById(R.id.switchModeLayout).setOnClickListener(v -> switchMode());
        mainActionBtn.setOnClickListener(v -> validateAndExecute());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebook.setOnClickListener(v -> 
            Toast.makeText(this, "تسجيل فيسبوك سيتوفر قريباً", Toast.LENGTH_SHORT).show()
        );

        // أزرار الملف الشخصي
        if (btnSetImage != null) {
            btnSetImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }
        
        if (btnChangeImage != null) {
            btnChangeImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }
        
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }
        
        if (btnAddPost != null) {
            btnAddPost.setOnClickListener(v -> 
                Toast.makeText(this, "سيتم إضافة منشور جديد", Toast.LENGTH_SHORT).show()
            );
        }

        // النقر على الاسم للتعديل
        if (layoutEditName != null) {
            layoutEditName.setOnClickListener(v -> showEditNameDialog());
        }

        // النقر على النبذة للتعديل
        if (layoutEditBio != null) {
            layoutEditBio.setOnClickListener(v -> showEditBioDialog());
        }

        // النقر على اسم المستخدم للتعديل
        if (layoutEditUsername != null) {
            layoutEditUsername.setOnClickListener(v -> showEditUsernameDialog());
        }
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
                saveUserData(
                    mAuth.getCurrentUser().getDisplayName(),
                    mAuth.getCurrentUser().getEmail()
                );
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
            tvTitle.setText("تسجيل الدخول");
            mainActionBtn.setText("تسجيل الدخول");
            tvSwitchAction.setText("إنشاء حساب جديد");
            layoutUser.setVisibility(View.GONE);
        } else {
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
        String user = regUser.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("صيغة غير صحيحة");
            return;
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            regPass.setError("كلمة المرور قصيرة");
            return;
        }

        if (!isLoginMode && TextUtils.isEmpty(user)) {
            regUser.setError("اسم المستخدم مطلوب");
            return;
        }

        setLoadingState(true);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    goToDashboard();
                } else {
                    resetUI("تأكد من بياناتك");
                }
            });
        } else {
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    saveUserData(user, email);
                } else {
                    resetUI("فشل إنشاء الحساب");
                }
            });
        }
    }

    // ============================================================
    // حفظ بيانات المستخدم في قاعدة البيانات
    // ============================================================
    private void saveUserData(String username, String email) {
        String uid = mAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("displayName", username.toUpperCase());
        userMap.put("bio", "#لست صداعاً انا فكرة اكبر من رأسك.");
        userMap.put("profileImage", "");

        mDb.child("Users").child(uid).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                goToDashboard();
            } else {
                resetUI("خطأ في حفظ البيانات");
            }
        });
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

        mainToolbar.setTitle("المحادثات");
        layoutSettings.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.GONE);
        layoutChats.setVisibility(View.VISIBLE);
        bottomNavigation.setSelectedItemId(R.id.nav_chats);

        loadUserProfile();
    }

    // ============================================================
    // جلب بيانات المستخدم من Firebase
    // ============================================================
    private void loadUserProfile() {
        String uid = mAuth.getCurrentUser().getUid();
        mDb.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("displayName").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String image = snapshot.child("profileImage").getValue(String.class);

                    if (name != null) profileName.setText(name + " •");
                    if (bio != null) profileBio.setText(bio);
                    if (username != null) profileUsernameText.setText("@" + username);

                    if (image != null && !image.isEmpty() && !isDestroyed()) {
                        Glide.with(MainActivity.this)
                                .load(image)
                                .placeholder(R.drawable.bg_login)
                                .error(R.drawable.bg_login)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============================================================
    // رفع الصورة إلى Firebase Storage
    // ============================================================
    private void uploadProfileImage(Uri imageUri) {
        Toast.makeText(this, "جاري رفع الصورة...", Toast.LENGTH_SHORT).show();
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference fileRef = mStorage.child("Profile_Images").child(uid + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                mDb.child("Users").child(uid).child("profileImage").setValue(downloadUrl);
                Toast.makeText(this, "تم تغيير الصورة بنجاح!", Toast.LENGTH_SHORT).show();
            })
        ).addOnFailureListener(e -> 
            Toast.makeText(this, "فشل رفع الصورة", Toast.LENGTH_SHORT).show()
        );
    }

    // ============================================================
    // نافذة تعديل الاسم
    // ============================================================
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تعديل الاسم");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText input = new EditText(this);
        input.setHint("الاسم الجديد");
        input.setText(profileName.getText().toString().replace(" •", ""));
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("حفظ", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                String uid = mAuth.getCurrentUser().getUid();
                mDb.child("Users").child(uid).child("displayName").setValue(newName);
                Toast.makeText(this, "تم تحديث الاسم", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ============================================================
    // نافذة تعديل النبذة
    // ============================================================
    private void showEditBioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تعديل النبذة");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText input = new EditText(this);
        input.setHint("النبذة الجديدة");
        input.setText(profileBio.getText().toString());
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("حفظ", (dialog, which) -> {
            String newBio = input.getText().toString().trim();
            if (!newBio.isEmpty()) {
                String uid = mAuth.getCurrentUser().getUid();
                mDb.child("Users").child(uid).child("bio").setValue(newBio);
                Toast.makeText(this, "تم تحديث النبذة", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ============================================================
    // نافذة تعديل اسم المستخدم (@username)
    // ============================================================
    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تعديل اسم المستخدم");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText input = new EditText(this);
        input.setHint("اسم المستخدم الجديد");
        String current = profileUsernameText.getText().toString();
        if (current.startsWith("@")) {
            input.setText(current.substring(1));
        }
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("حفظ", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                String uid = mAuth.getCurrentUser().getUid();
                mDb.child("Users").child(uid).child("username").setValue(newUsername);
                Toast.makeText(this, "تم تحديث اسم المستخدم", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ============================================================
    // نافذة تعديل البيانات الكاملة
    // ============================================================
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تعديل البيانات");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("الاسم الجديد");
        nameInput.setText(profileName.getText().toString().replace(" •", ""));
        layout.addView(nameInput);

        final EditText bioInput = new EditText(this);
        bioInput.setHint("النبذة الجديدة");
        bioInput.setText(profileBio.getText().toString());
        layout.addView(bioInput);

        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("اسم المستخدم الجديد");
        String current = profileUsernameText.getText().toString();
        if (current.startsWith("@")) {
            usernameInput.setText(current.substring(1));
        }
        layout.addView(usernameInput);

        builder.setView(layout);

        builder.setPositiveButton("حفظ", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newBio = bioInput.getText().toString().trim();
            String newUsername = usernameInput.getText().toString().trim();
            String uid = mAuth.getCurrentUser().getUid();

            if (!newName.isEmpty()) {
                mDb.child("Users").child(uid).child("displayName").setValue(newName);
            }
            if (!newBio.isEmpty()) {
                mDb.child("Users").child(uid).child("bio").setValue(newBio);
            }
            if (!newUsername.isEmpty()) {
                mDb.child("Users").child(uid).child("username").setValue(newUsername);
            }

            Toast.makeText(this, "تم تحديث البيانات", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("إلغاء", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
