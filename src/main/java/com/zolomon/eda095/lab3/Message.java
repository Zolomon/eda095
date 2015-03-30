package com.zolomon.eda095.lab3;

public class Message {
    private long timestamp;
    private String author;
    private String message;

    public Message(String author, String message) {
        this.timestamp = System.currentTimeMillis();
        this.author = author;
        this.message = message;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getMessage() {
        return this.message;
    }
}
