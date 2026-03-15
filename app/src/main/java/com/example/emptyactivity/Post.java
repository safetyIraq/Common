package com.example.emptyactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    // ============================================================
    // الثوابت
    // ============================================================
    public static final String FIELD_POST_ID = "postId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_USER_NAME = "userName";
    public static final String FIELD_USER_IMAGE = "userImage";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_IMAGE_URL = "imageUrl";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_LIKES = "likes";
    public static final String FIELD_COMMENTS = "comments";

    // ============================================================
    // المتغيرات
    // ============================================================
    private String postId;
    private String userId;
    private String userName;
    private String userImage;
    private String content;
    private String imageUrl;
    private long timestamp;
    private int likes;
    private int comments;

    // ============================================================
    // المشيدات
    // ============================================================
    public Post() {
        // مطلوب لـ Firebase
    }

    public Post(String postId, String userId, String userName, String userImage, 
                String content, String imageUrl, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.likes = 0;
        this.comments = 0;
    }

    // ============================================================
    // Getters
    // ============================================================
    public String getPostId() { return postId != null ? postId : ""; }
    public String getUserId() { return userId != null ? userId : ""; }
    public String getUserName() { return userName != null ? userName : ""; }
    public String getUserImage() { return userImage != null ? userImage : ""; }
    public String getContent() { return content != null ? content : ""; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public long getTimestamp() { return timestamp; }
    public int getLikes() { return likes; }
    public int getComments() { return comments; }

    // ============================================================
    // Setters
    // ============================================================
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserImage(String userImage) { this.userImage = userImage; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setComments(int comments) { this.comments = comments; }

    // ============================================================
    // دوال مساعدة
    // ============================================================
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_POST_ID, postId);
        map.put(FIELD_USER_ID, userId);
        map.put(FIELD_USER_NAME, userName);
        map.put(FIELD_USER_IMAGE, userImage);
        map.put(FIELD_CONTENT, content);
        map.put(FIELD_IMAGE_URL, imageUrl);
        map.put(FIELD_TIMESTAMP, timestamp);
        map.put(FIELD_LIKES, likes);
        map.put(FIELD_COMMENTS, comments);
        return map;
    }

    @Exclude
    public String getFormattedTime() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // أقل من دقيقة
            return "منذ لحظات";
        } else if (diff < 3600000) { // أقل من ساعة
            long minutes = diff / 60000;
            return "منذ " + minutes + " دقيقة";
        } else if (diff < 86400000) { // أقل من يوم
            long hours = diff / 3600000;
            return "منذ " + hours + " ساعة";
        } else {
            long days = diff / 86400000;
            return "منذ " + days + " يوم";
        }
    }
}
