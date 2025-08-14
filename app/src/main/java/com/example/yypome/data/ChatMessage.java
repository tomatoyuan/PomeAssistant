package com.example.yypome.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String text;
    private String imageUrl;
    private boolean isSent;
    private long timestamp;

    public ChatMessage(String text, boolean isSent, long timestamp) {
        this.text = text;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public ChatMessage(String text, String imageUrl, boolean isSent, long timestamp) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String message) {
        this.text = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isSent() {
        return isSent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        // 格式化时间戳为可读时间
//        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

}
