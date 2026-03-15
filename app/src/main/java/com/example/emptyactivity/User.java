package com.example.emptyactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 💫 كلاس المستخدم - نموذج بيانات متكامل للمستخدمين
 * 
 * هذا الكلاس يمثل هيكل بيانات المستخدم في تطبيقنا
 * متوافق تماماً مع Firebase Realtime Database
 * 
 * @version 2.0.0
 * @since 2026
 */
@IgnoreExtraProperties
public class User {

    // ============================================================
    // الثوابت - حقول قاعدة البيانات
    // ============================================================
    public static final String FIELD_UID = "uid";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PROFILE_IMAGE = "profileImage";
    public static final String FIELD_BIO = "bio";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_LAST_SEEN = "lastSeen";
    public static final String FIELD_IS_ONLINE = "isOnline";

    // ============================================================
    // متغيرات الكلاس - خاصة (Private)
    // ============================================================
    private String uid;
    private String displayName;
    private String username;
    private String profileImage;
    private String bio;
    private String email;
    private Long createdAt;
    private Long lastSeen;
    private Boolean isOnline;

    // ============================================================
    // المُشيدات (Constructors)
    // ============================================================

    /**
     * المُشيد الافتراضي - مطلوب لـ Firebase
     */
    public User() {
        // ضروري لـ Firebase
    }

    /**
     * مُشيد كامل بجميع الحقول
     */
    public User(String uid, String displayName, String username, 
                String profileImage, String bio, String email,
                Long createdAt, Long lastSeen, Boolean isOnline) {
        this.uid = uid;
        this.displayName = displayName;
        this.username = username;
        this.profileImage = profileImage;
        this.bio = bio;
        this.email = email;
        this.createdAt = createdAt;
        this.lastSeen = lastSeen;
        this.isOnline = isOnline;
    }

    /**
     * مُشيد مبسط - للحالات الأساسية
     */
    public User(String uid, String displayName, String username, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.username = username;
        this.email = email;
        this.bio = "#لست صداعاً انا فكرة اكبر من رأسك."; // قيمة افتراضية
        this.profileImage = ""; // قيمة افتراضية
        this.createdAt = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.isOnline = true;
    }

    // ============================================================
    // Getters - الحصول على القيم
    // ============================================================

    @NonNull
    public String getUid() { 
        return uid != null ? uid : ""; 
    }

    @NonNull
    public String getDisplayName() { 
        return displayName != null ? displayName : ""; 
    }

    @NonNull
    public String getUsername() { 
        return username != null ? username : ""; 
    }

    @NonNull
    public String getProfileImage() { 
        return profileImage != null ? profileImage : ""; 
    }

    @NonNull
    public String getBio() { 
        return bio != null ? bio : ""; 
    }

    @NonNull
    public String getEmail() { 
        return email != null ? email : ""; 
    }

    @Nullable
    public Long getCreatedAt() { 
        return createdAt; 
    }

    @Nullable
    public Long getLastSeen() { 
        return lastSeen; 
    }

    @Nullable
    public Boolean getIsOnline() { 
        return isOnline; 
    }

    // ============================================================
    // Setters - تعيين القيم
    // ============================================================

    public void setUid(@NonNull String uid) { 
        this.uid = uid; 
    }

    public void setDisplayName(@NonNull String displayName) { 
        this.displayName = displayName; 
    }

    public void setUsername(@NonNull String username) { 
        this.username = username; 
    }

    public void setProfileImage(@NonNull String profileImage) { 
        this.profileImage = profileImage; 
    }

    public void setBio(@NonNull String bio) { 
        this.bio = bio; 
    }

    public void setEmail(@NonNull String email) { 
        this.email = email; 
    }

    public void setCreatedAt(@Nullable Long createdAt) { 
        this.createdAt = createdAt; 
    }

    public void setLastSeen(@Nullable Long lastSeen) { 
        this.lastSeen = lastSeen; 
    }

    public void setIsOnline(@Nullable Boolean isOnline) { 
        this.isOnline = isOnline; 
    }

    // ============================================================
    // دوال مساعدة (Utility Methods)
    // ============================================================

    /**
     * تحويل كائن المستخدم إلى Map للتخزين في Firebase
     * @return Map جاهز للتخزين
     */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        map.put(FIELD_UID, uid);
        map.put(FIELD_DISPLAY_NAME, displayName);
        map.put(FIELD_USERNAME, username);
        map.put(FIELD_PROFILE_IMAGE, profileImage);
        map.put(FIELD_BIO, bio);
        map.put(FIELD_EMAIL, email);
        map.put(FIELD_CREATED_AT, createdAt);
        map.put(FIELD_LAST_SEEN, lastSeen);
        map.put(FIELD_IS_ONLINE, isOnline);
        
        return map;
    }

    /**
     * إنشاء كائن User من Map قادم من Firebase
     * @param map الخريطة القادمة من Firebase
     * @return كائن User
     */
    @NonNull
    @Exclude
    public static User fromMap(@NonNull Map<String, Object> map) {
        User user = new User();
        
        if (map.containsKey(FIELD_UID)) 
            user.setUid((String) map.get(FIELD_UID));
        
        if (map.containsKey(FIELD_DISPLAY_NAME)) 
            user.setDisplayName((String) map.get(FIELD_DISPLAY_NAME));
        
        if (map.containsKey(FIELD_USERNAME)) 
            user.setUsername((String) map.get(FIELD_USERNAME));
        
        if (map.containsKey(FIELD_PROFILE_IMAGE)) 
            user.setProfileImage((String) map.get(FIELD_PROFILE_IMAGE));
        
        if (map.containsKey(FIELD_BIO)) 
            user.setBio((String) map.get(FIELD_BIO));
        
        if (map.containsKey(FIELD_EMAIL)) 
            user.setEmail((String) map.get(FIELD_EMAIL));
        
        if (map.containsKey(FIELD_CREATED_AT) && map.get(FIELD_CREATED_AT) != null) 
            user.setCreatedAt(Long.parseLong(map.get(FIELD_CREATED_AT).toString()));
        
        if (map.containsKey(FIELD_LAST_SEEN) && map.get(FIELD_LAST_SEEN) != null) 
            user.setLastSeen(Long.parseLong(map.get(FIELD_LAST_SEEN).toString()));
        
        if (map.containsKey(FIELD_IS_ONLINE)) 
            user.setIsOnline((Boolean) map.get(FIELD_IS_ONLINE));
        
        return user;
    }

    /**
     * الحصول على اسم المستخدم مع @
     * @return @username
     */
    @Exclude
    @NonNull
    public String getUsernameWithAt() {
        return username != null && !username.isEmpty() ? "@" + username : "";
    }

    /**
     * الحصول على الاسم المعروض مع نقطة
     * @return displayName •
     */
    @Exclude
    @NonNull
    public String getDisplayNameWithDot() {
        return displayName != null && !displayName.isEmpty() ? displayName + " •" : "";
    }

    /**
     * التحقق مما إذا كان المستخدم متصل الآن
     * @return true إذا كان متصلاً
     */
    @Exclude
    public boolean isOnlineNow() {
        return isOnline != null && isOnline;
    }

    /**
     * تحديث وقت آخر ظهور
     */
    @Exclude
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    /**
     * تحديث حالة الاتصال
     */
    @Exclude
    public void setOnline(boolean online) {
        this.isOnline = online;
        if (online) {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    // ============================================================
    // Override Methods - دوال تجاوز
    // ============================================================

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return Objects.equals(uid, user.uid);
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", displayName='" + displayName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    // ============================================================
    // Builder Pattern - نمط البناء
    // ============================================================

    /**
     * كلاس Builder لإنشاء كائنات User بطريقة مرنة
     */
    public static class Builder {
        private String uid;
        private String displayName;
        private String username;
        private String profileImage;
        private String bio;
        private String email;
        private Long createdAt;
        private Long lastSeen;
        private Boolean isOnline;

        public Builder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setProfileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder setBio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setLastSeen(Long lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }

        public Builder setIsOnline(Boolean isOnline) {
            this.isOnline = isOnline;
            return this;
        }

        public User build() {
            return new User(uid, displayName, username, profileImage, 
                           bio, email, createdAt, lastSeen, isOnline);
        }
    }

    // ============================================================
    // مثال للاستخدام في MainActivity
    // ============================================================
    /*
    // حفظ مستخدم جديد
    User newUser = new User(uid, "HUSSEIN", "iomk0", email);
    mDb.child("Users").child(uid).setValue(newUser.toMap());

    // قراءة مستخدم
    mDb.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                profileName.setText(user.getDisplayNameWithDot());
                profileUsernameText.setText(user.getUsernameWithAt());
            }
        }
    });

    // استخدام Builder
    User user = new User.Builder()
            .setUid(uid)
            .setDisplayName("HUSSEIN")
            .setUsername("iomk0")
            .setEmail("user@example.com")
            .setBio("لست صداعاً انا فكرة اكبر من رأسك.")
            .setIsOnline(true)
            .build();
    */
  }
