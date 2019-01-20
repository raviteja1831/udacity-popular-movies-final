package com.android.popularmovies.dto;

import lombok.Data;

@Data
public class Movie {
    private int id;
    private String title;
    private String imageUrl;
    private String rating;
    private String overview;
    private String releaseDate;
}