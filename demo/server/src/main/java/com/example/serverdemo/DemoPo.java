package com.example.serverdemo;

public class DemoPo {
    private String text;

    public String getText() {
        return text;
    }

    public DemoPo(String text, int count, long time, String token) {
        this.text = text;
        this.count = count;
        this.time = time;
        this.token = token;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private int count;
    private long time;
    private String token;
}
