package com.example.emptyactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ImageView btnBack;
    private TextView tvNoResults;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;

    private SearchAdapter adapter;
    private List<User> userList;
    private List<User> filteredList;
    private DatabaseReference mDb;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private AtomicBoolean isSearching = new AtomicBoolean(false);
    private ValueEventListener searchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupFirebase();
        setupRecyclerView();
        setupSearchListener();
        setupClickListeners();
        loadAllUsers();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        btnBack = findViewById(R.id.btnBack);
        tvNoResults = findViewById(R.id.tvNoResults);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        setupKeyboardAction();
    }

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

    private void setupFirebase() {
        mDb = FirebaseDatabase.getInstance().getReference("Users");
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setHasFixedSize(true);
        rvSearchResults.setItemViewCacheSize(20);
        adapter = new SearchAdapter(filteredList);
        rvSearchResults.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            finish();
        });

        swipeRefreshLayout.setOnRefreshListener(this::refreshUserList);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark
        );
    }

    private void refreshUserList() {
        loadAllUsers();
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                String searchText = s.toString().trim();
                if (searchText.length() >= 1) {
                    showLoading(true);
                    searchRunnable = () -> performSearch(searchText.toLowerCase(Locale.getDefault()));
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    filteredList.clear();
                    filteredList.addAll(userList);
                    adapter.notifyDataSetChanged();
                    updateNoResultsVisibility();
                    showLoading(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

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
                        // ✅ التأكد من تعيين UID من مفتاح Firebase
                        if (user.getUid() == null || user.getUid().isEmpty()) {
                            user.setUid(data.getKey());
                        }
                        userList.add(user);
                    }
                }
                sortUsersByName();
                filteredList.clear();
                filteredList.addAll(userList);
                adapter.notifyDataSetChanged();
                updateNoResultsVisibility();
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, "خطأ: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
            }
        };
        mDb.addValueEventListener(searchListener);
    }

    private void sortUsersByName() {
        Collections.sort(userList, (u1, u2) -> {
            String name1 = u1.getDisplayName() != null ? u1.getDisplayName() : "";
            String name2 = u2.getDisplayName() != null ? u2.getDisplayName() : "";
            return name1.compareToIgnoreCase(name2);
        });
    }

    private void performSearch(String searchText) {
        if (isSearching.get()) return;
        isSearching.set(true);

        new Thread(() -> {
            List<User> results = new ArrayList<>();
            for (User user : userList) {
                if (isMatch(user, searchText)) {
                    results.add(user);
                }
            }
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

    private boolean isMatch(User user, String searchText) {
        if (user == null || searchText == null || searchText.isEmpty()) return false;
        String username = user.getUsername();
        if (username != null && username.toLowerCase(Locale.getDefault()).contains(searchText)) return true;
        String displayName = user.getDisplayName();
        if (displayName != null && displayName.toLowerCase(Locale.getDefault()).contains(searchText)) return true;
        String email = user.getEmail();
        if (email != null && email.toLowerCase(Locale.getDefault()).contains(searchText)) return true;
        return false;
    }

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

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.UserViewHolder> {
        private final List<User> users;

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
            bindUserData(holder, user);
            setupItemClickListeners(holder, user);
        }

        private void bindUserData(@NonNull UserViewHolder holder, User user) {
            String displayName = user.getDisplayName() != null ? user.getDisplayName() + " •" : "مستخدم •";
            holder.itemName.setText(displayName);
            
            String bio = user.getBio();
            holder.itemBio.setText(bio != null && !bio.isEmpty() ? bio : "لا توجد نبذة");
            
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

        // ✅ زر المحادثة - المعدل النهائي
        private void setupItemClickListeners(@NonNull UserViewHolder holder, User user) {
            holder.btnChatWithUser.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(SearchActivity.this, android.R.anim.fade_in));
                
                // التحقق من البيانات
                if (user == null) {
                    Toast.makeText(SearchActivity.this, "خطأ: المستخدم غير موجود", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // الحصول على UID
                String userId = user.getUid();
                String userName = user.getDisplayName();
                String userImage = user.getProfileImage();
                
                // طباعة للتحقق
                Log.d("CHAT_DEBUG", "محاولة فتح محادثة مع: " + userName);
                Log.d("CHAT_DEBUG", "المعرف: " + userId);
                
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(SearchActivity.this, "خطأ: معرف المستخدم غير صحيح", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (userName == null || userName.isEmpty()) {
                    userName = "مستخدم";
                }
                
                if (userImage == null) {
                    userImage = "";
                }
                
                // فتح المحادثة
                Intent chatIntent = new Intent(SearchActivity.this, ChatActivity.class);
                chatIntent.putExtra("user_id", userId);
                chatIntent.putExtra("user_name", userName);
                chatIntent.putExtra("user_image", userImage);
                startActivity(chatIntent);
            });

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(SearchActivity.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView itemName, itemBio;
            CircleImageView itemProfileImage;
            View btnChatWithUser;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.itemName);
                itemBio = itemView.findViewById(R.id.itemBio);
                itemProfileImage = itemView.findViewById(R.id.itemProfileImage);
                btnChatWithUser = itemView.findViewById(R.id.btnChatWithUser);
            }
        }
    }
}
