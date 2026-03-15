package com.example.emptyactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 💬 كلاس رسالة المحادثة
 */
public class ChatMessage {
    
    public static final String FIELD_MESSAGE_ID = "messageId";
    public static final String FIELD_SENDER_ID = "senderId";
    public static final String FIELD_RECEIVER_ID = "receiverId";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_IS_READ = "isRead";
    public static final String FIELD_TYPE = "type"; // text, image, voice

    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String type;

    // مُشيد فارغ (مطلوب لـ Firebase)
    public ChatMessage() {}

    // مُشيد كامل
    public ChatMessage(String messageId, String senderId, String receiverId, 
                       String message, long timestamp, boolean isRead, String type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.type = type;
    }

    // Getters
    public String getMessageId() { return messageId != null ? messageId : ""; }
    public String getSenderId() { return senderId != null ? senderId : ""; }
    public String getReceiverId() { return receiverId != null ? receiverId : ""; }
    public String getMessage() { return message != null ? message : ""; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public String getType() { return type != null ? type : "text"; }

    // Setters
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { isRead = read; }
    public void setType(String type) { this.type = type; }

    // تحويل إلى Map للتخزين في Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_MESSAGE_ID, messageId);
        map.put(FIELD_SENDER_ID, senderId);
        map.put(FIELD_RECEIVER_ID, receiverId);
        map.put(FIELD_MESSAGE, message);
        map.put(FIELD_TIMESTAMP, timestamp);
        map.put(FIELD_IS_READ, isRead);
        map.put(FIELD_TYPE, type);
        return map;
    }
}
