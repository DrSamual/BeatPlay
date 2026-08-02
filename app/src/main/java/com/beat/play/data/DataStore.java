package com.beat.play.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataStore {

    private static final DatabaseReference ROOT = FirebaseDatabase.getInstance().getReference();

    private DataStore() {
    }

    public static DatabaseReference channels() {
        return ROOT.child("channels");
    }

    public static DatabaseReference movies() {
        return ROOT.child("movies");
    }

    public static DatabaseReference settings() {
        return ROOT.child("settings");
    }

    public static DatabaseReference banners() {
        return ROOT.child("banners");
    }

    public static DatabaseReference notifications() {
        return ROOT.child("notifications");
    }
}
