package com.example.emptyactivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * =====================================================================
 * 🔍 نشاط البحث المتقدم - SearchActivity
 * =====================================================================
 * 
 * 💫 هذا النشاط مسؤول عن البحث عن المستخدمين في قاعدة البيانات
 * ✨ يتميز بالبحث الفوري مع تحسين الأداء وتجربة مستخدم سلسة
 * 
 * @version 2.0.0
 * @since 2026
 * =====================================================================
 */
public class SearchActivity extends AppCompatActivity {

    // ================================================================
    // 🔧 الثوابت - Constants
    // ================================================================
    private static final String TAG = "SearchActivity";
    private static final long SEARCH_DELAY_MS = 500; // تأخير البحث لتحسين الأداء
    private static final int MIN_SEARCH_CHARS = 1; // أقل عدد أحرف للبحث
    private static final String DATABASE_PATH_USERS = "Users";
    private static final String DATABASE_FIELD_USERNAME = "username";
    private static final String DATABASE_FIELD_DISPLAY_NAME = "displayName";

    // ================================================================
    // 📊 متغيرات الواجهة - UI Components
    // ================================================================
    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ImageView btnBack;
    private TextView tvNoResults;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingIndicator;

    // ================================================================
    // 🔄 متغيرات البيانات - Data Components
    // ================================================================
    private SearchAdapter adapter;
    private List<User> userList;
    private List<User> filteredList;
    private DatabaseReference mDb;

    // ================================================================
    // ⚡ متغيرات التحكم - Control Variables
    // ================================================================
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private AtomicBoolean isSearching = new AtomicBoolean(false);
    private ValueEventListener searchListener;

    // ================================================================
    // 🏗️ دورة حياة النشاط - Activity Lifecycle
    // ================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupFirebase();
        setupRecyclerView();
        setupSearchListener();
        setupClickListeners();
        loadAllUsers(); // تحميل جميع المستخدمين عند البداية
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // تنظيف الموارد لمنع تسرب الذاكرة
        searchHandler.removeCallbacksAndMessages(null);
        if (searchListener != null && mDb != null) {
            mDb.removeEventListener(searchListener);
        }
    }

    // ================================================================
    // 🎨 تهيئة الواجهات - View Initialization
    // ================================================================

    /**
     * تهيئة جميع عناصر الواجهة
     */
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        btnBack = findViewById(R.id.btnBack);
        tvNoResults = findViewById(R.id.tvNoResults);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // إعدادات إضافية للواجهة
        setupKeyboardAction();
    }

    /**
     * إعداد زر الإدخال في لوحة المفاتيح
     */
    private void setupKeyboardAction() {
        etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    // ================================================================
    // 🔥 إعداد Firebase - Firebase Setup
    // ================================================================

    /**
     * تهيئة اتصال Firebase
     */
    private void setupFirebase() {
        mDb = FirebaseDatabase.getInstance().getReference(DATABASE_PATH_USERS);
    }

    // ================================================================
    // 📋 إعداد RecyclerView - RecyclerView Setup
    // ================================================================

    /**
     * تهيئة RecyclerView والمحول
     */
    private void setupRecyclerView() {
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setHasFixedSize(true);
        rvSearchResults.setItemViewCacheSize(20);
        
        adapter = new SearchAdapter(filteredList);
        rvSearchResults.setAdapter(adapter);
    }

    // ================================================================
    // 👆 إعداد مستمعي الأحداث - Click Listeners Setup
    // ================================================================

    /**
     * إعداد جميع مستمعي الأحداث
     */
    private void setupClickListeners() {
        // زر الرجوع
        btnBack.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            finish();
        });

        // تحديث القائمة بالسحب للأسفل
        swipeRefreshLayout.setOnRefreshListener(this::refreshUserList);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_dark,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark
        );
    }

    /**
     * تحديث قائمة المستخدمين
     */
    private void refreshUserList() {
        loadAllUsers();
    }

    // ================================================================
    // 🔍 إعداد البحث - Search Setup
    // ================================================================

    /**
     * إعداد مستمع البحث الفوري مع تأخير لتحسين الأداء
     */
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // لا حاجة لتنفيذ شيء
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // إلغاء الطلب السابق وتأخير البحث الجديد
                searchHandler.removeCallbacks(searchRunnable);
                
                String searchText = s.toString().trim();
                
                if (searchText.length() >= MIN_SEARCH_CHARS) {
                    showLoading(true);
                    searchRunnable = () -> performSearch(searchText.toLowerCase(Locale.getDefault()));
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    // إظهار جميع المستخدمين إذا كان النص فارغاً
                    filteredList.clear();
                    filteredList.addAll(userList);
                    adapter.notifyDataSetChanged();
                    updateNoResultsVisibility();
                    showLoading(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // لا حاجة لتنفيذ شيء
            }
        });
    }

    // ================================================================
    // 📥 تحميل البيانات - Data Loading
    // ================================================================

    /**
     * تحميل جميع المستخدمين من قاعدة البيانات
     */
    private void loadAllUsers() {
        showLoading(true);
        
        if (searchListener != null) {
            mDb.removeEventListener(searchListener);
        }

        searchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // ترتيب المستخدمين أبجدياً
                sortUsersByName();

                // تحديث القائمة المعروضة
                filteredList.clear();
                filteredList.addAll(userList);
                adapter.notifyDataSetChanged();

                updateNoResultsVisibility();
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
            }
        };

        mDb.addValueEventListener(searchListener);
    }

    /**
     * ترتيب المستخدمين حسب الاسم
     */
    private void sortUsersByName() {
        Collections.sort(userList, (u1, u2) -> {
            String name1 = u1.getDisplayName() != null ? u1.getDisplayName() : "";
            String name2 = u2.getDisplayName() != null ? u2.getDisplayName() : "";
            return name1.compareToIgnoreCase(name2);
        });
    }

    // ================================================================
    // 🔎 تنفيذ البحث - Perform Search
    // ================================================================

    /**
     * تنفيذ البحث في قائمة المستخدمين
     * @param searchText نص البحث
     */
    private void performSearch(String searchText) {
        if (isSearching.get()) return;
        
        isSearching.set(true);
        
        // البحث في الذاكرة المحلية (أسرع من البحث في Firebase)
        new Thread(() -> {
            List<User> results = new ArrayList<>();
            
            for (User user : userList) {
                if (isMatch(user, searchText)) {
                    results.add(user);
                }
            }

            // تحديث الواجهة في الـ UI Thread
            runOnUiThread(() -> {
                filteredList.clear();
                filteredList.addAll(results);
                adapter.notifyDataSetChanged();
                updateNoResultsVisibility();
                showLoading(false);
                isSearching.set(false);
            });
        }).start();
    }

    /**
     * التحقق مما إذا كان المستخدم يطابق نص البحث
     * @param user المستخدم
     * @param searchText نص البحث
     * @return true إذا كان مطابقاً
     */
    private boolean isMatch(User user, String searchText) {
        if (user == null || searchText == null || searchText.isEmpty()) return false;

        // البحث في اسم المستخدم
        String username = user.getUsername();
        if (username != null && username.toLowerCase(Locale.getDefault()).contains(searchText)) {
            return true;
        }

        // البحث في الاسم المعروض
        String displayName = user.getDisplayName();
        if (displayName != null && displayName.toLowerCase(Locale.getDefault()).contains(searchText)) {
            return true;
        }

        // البحث في البريد الإلكتروني
        String email = user.getEmail();
        if (email != null && email.toLowerCase(Locale.getDefault()).contains(searchText)) {
            return true;
        }

        return false;
    }

    // ================================================================
    // 🎯 تحديث الواجهة - UI Updates
    // ================================================================

    /**
     * تحديث ظهور رسالة "لا توجد نتائج"
     */
    private void updateNoResultsVisibility() {
        if (tvNoResults != null) {
            if (filteredList.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
                rvSearchResults.setVisibility(View.GONE);
            } else {
                tvNoResults.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * إظهار/إخفاء مؤشر التحميل
     * @param show true للإظهار، false للإخفاء
     */
    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    // ================================================================
    // ⚠️ معالجة الأخطاء - Error Handling
    // ================================================================

    /**
     * معالجة أخطاء قاعدة البيانات
     * @param error كائن الخطأ
     */
    private void handleDatabaseError(@NonNull DatabaseError error) {
        String errorMessage;
        switch (error.getCode()) {
            case DatabaseError.PERMISSION_DENIED:
                errorMessage = "ليس لديك صلاحية للوصول إلى هذه البيانات";
                break;
            case DatabaseError.NETWORK_ERROR:
                errorMessage = "خطأ في الشبكة، تحقق من اتصالك بالإنترنت";
                break;
            default:
                errorMessage = "حدث خطأ: " + error.getMessage();
                break;
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // ================================================================
    // 📦 محول RecyclerView - RecyclerView Adapter
    // ================================================================

    /**
     * محول عرض المستخدمين في القائمة
     */
    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.UserViewHolder> {

        private final List<User> users;
        private int lastAnimatedPosition = -1;

        public SearchAdapter(List<User> users) {
            this.users = users;
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            
            // تطبيق تأثير ظهور تدريجي
            animateItemView(holder.itemView, position);
            
            // تعيين البيانات
            bindUserData(holder, user);
            
            // إعداد مستمعي الأحداث
            setupItemClickListeners(holder, user, position);
        }

        /**
         * تطبيق تأثير ظهور تدريجي للعناصر
         */
        private void animateItemView(View view, int position) {
            if (position > lastAnimatedPosition) {
                view.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.slide_in_left));
                lastAnimatedPosition = position;
            }
        }

        /**
         * تعيين بيانات المستخدم في واجهة العنصر
         */
        private void bindUserData(@NonNull UserViewHolder holder, User user) {
            // تعيين الاسم
            String displayName = user.getDisplayNameWithDot();
            holder.itemName.setText(displayName.isEmpty() ? "مستخدم" : displayName);

            // تعيين النبذة
            String bio = user.getBio();
            holder.itemBio.setText(bio != null && !bio.isEmpty() ? bio : "لا توجد نبذة");

            // تحميل الصورة
            String imageUrl = user.getProfileImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(SearchActivity.this)
                        .load(imageUrl)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .placeholder(R.drawable.bg_login)
                        .error(R.drawable.bg_login)
                        .into(holder.itemProfileImage);
            } else {
                holder.itemProfileImage.setImageResource(R.drawable.bg_login);
            }
        }

        /**
         * إعداد مستمعي الأحداث للعنصر
         */
        private void setupItemClickListeners(@NonNull UserViewHolder holder, User user, int position) {
            // فتح المحادثة
            holder.btnChatWithUser.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in));
                openChatWithUser(user);
            });

            // النقر على العنصر ككل
            holder.itemView.setOnClickListener(v -> {
                showUserProfile(user);
            });
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        @Override
        public long getItemId(int position) {
            // استخدام hashcode لضمان استقرار المعرفات
            return users.get(position).hashCode();
        }

        /**
         * ViewHolder لعناصر القائمة
         */
        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView itemName, itemBio;
            CircleImageView itemProfileImage;
            View btnChatWithUser;
            View onlineIndicator;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.itemName);
                itemBio = itemView.findViewById(R.id.itemBio);
                itemProfileImage = itemView.findViewById(R.id.itemProfileImage);
                btnChatWithUser = itemView.findViewById(R.id.btnChatWithUser);
                onlineIndicator = itemView.findViewById(R.id.itemOnlineStatus);
            }
        }
    }

    // ================================================================
    // 🚀 وظائف إضافية - Additional Functions
    // ================================================================

    /**
     * فتح محادثة مع المستخدم
     */
    private void openChatWithUser(User user) {
        String message = String.format("سيتم فتح محادثة مع %s في الإصدار القادم!", 
                user.getDisplayName() != null ? user.getDisplayName() : "المستخدم");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // TODO: إضافة Intent لفتح ChatActivity
        // Intent intent = new Intent(this, ChatActivity.class);
        // intent.putExtra("user_id", user.getUid());
        // startActivity(intent);
    }

    /**
     * عرض الملف الشخصي للمستخدم
     */
    private void showUserProfile(User user) {
        Toast.makeText(this, "عرض ملف " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        
        // TODO: إضافة Intent لفتح ProfileActivity
        // Intent intent = new Intent(this, ProfileActivity.class);
        // intent.putExtra("user_id", user.getUid());
        // startActivity(intent);
    }

    // ================================================================
    // 📝 مثال للاستخدام المتقدم
    // ================================================================
    
    /**
     * بحث متقدم مع فلترة حسب عدة معايير
     */
    private void advancedSearch(String text) {
        // يمكن إضافة خيارات بحث متقدمة هنا
        // مثل: البحث حسب الاسم، المدينة، الاهتمامات، إلخ
    }
                }
