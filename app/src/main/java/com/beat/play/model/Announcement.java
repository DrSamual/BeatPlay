package com.beat.play.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Announcement {
    public String id;
    public String title;
    public String message;
    public long timestamp;

    public Announcement() {
    }

    public Announcement(String id, String title, String message, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static String formatTime(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
