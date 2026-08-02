package com.beat.play.model;

public class Channel {
    public String id;
    public String name;
    public String url;
    public String logo;
    public String category;

    public Channel() {
    }

    public Channel(String id, String name, String url, String logo, String category) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.logo = logo;
        this.category = category;
    }
}
