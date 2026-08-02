package com.beat.play.model;

public class Movie {
    public String id;
    public String title;
    public String url;
    public String thumbnail;
    public String description;
    public String year;
    public String category;

    public Movie() {
    }

    public Movie(String id, String title, String url, String thumbnail,
                 String description, String year, String category) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
        this.description = description;
        this.year = year;
        this.category = category;
    }
}
