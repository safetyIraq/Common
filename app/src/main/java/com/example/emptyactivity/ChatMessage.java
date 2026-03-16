package com.example.emptyactivity;

import java.util.HashMap;
import java.util.Map;

public class ChatMessage {
    
    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String type;

    public ChatMessage() {}

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

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public String getType() { return type; }

    public void setRead(boolean read) { isRead = read; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("senderId", senderId);
        map.put("receiverId", receiverId);
        map.put("message", message);
        map.put("timestamp", timestamp);
        map.put("isRead", isRead);
        map.put("type", type);
        return map;
    }
}
