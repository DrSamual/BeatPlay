package com.beat.play.model;

public class Banner {
    public String id;
    public String title;
    public String image;
    public String targetTitle;
    public String targetUrl;

    public Banner() {
    }

    public Banner(String id, String title, String image, String targetTitle, String targetUrl) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.targetTitle = targetTitle;
        this.targetUrl = targetUrl;
    }
}
