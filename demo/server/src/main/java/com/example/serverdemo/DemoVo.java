package com.example.serverdemo;

public class DemoVo {
    private String text;
    private long time;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DemoVo(String text, long time) {
        this.text = text;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
