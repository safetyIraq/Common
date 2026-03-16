package com.example.emptyactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class User {
    
    private String uid;
    private String displayName;
    private String username;
    private String profileImage;
    private String bio;
    private String email;

    public User() {}

    public User(String uid, String displayName, String username, 
                String profileImage, String bio, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.username = username;
        this.profileImage = profileImage;
        this.bio = bio;
        this.email = email;
    }

    @NonNull
    public String getUid() { 
        return uid != null ? uid : ""; 
    }

    @NonNull
    public String getDisplayName() { 
        return displayName != null ? displayName : "مستخدم"; 
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

    public void setUid(String uid) { this.uid = uid; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setUsername(String username) { this.username = username; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setBio(String bio) { this.bio = bio; }
    public void setEmail(String email) { this.email = email; }
}
