package com.example.emptyactivity;

public class User {
    private String uid;
    private String displayName;
    private String username;
    private String profileImage;
    private String bio;
    private String email;

    public User() {}

    public User(String uid, String displayName, String username, String profileImage, String bio, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.username = username;
        this.profileImage = profileImage;
        this.bio = bio;
        this.email = email;
    }

    public String getUid() { return uid != null ? uid : ""; }
    public String getDisplayName() { return displayName != null ? displayName : "مستخدم"; }
    public String getUsername() { return username != null ? username : ""; }
    public String getProfileImage() { return profileImage != null ? profileImage : ""; }
    public String getBio() { return bio != null ? bio : ""; }
    public String getEmail() { return email != null ? email : ""; }
}
