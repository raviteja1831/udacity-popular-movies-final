package com.android.popularmovies;

import lombok.Data;

@Data
public class Movie {
    private String title;
    private String imageUrl;
    private String rating;
    private String overview;
    private String releaseDate;
}