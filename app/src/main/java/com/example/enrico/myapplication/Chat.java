package com.example.enrico.myapplication;

/**
 * Created by enrico on 1/7/2018.
 */

public class Chat {

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean seen;
    public long timestamp;

    public Chat(){}

    public Chat(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
