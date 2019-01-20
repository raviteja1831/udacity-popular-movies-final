package com.android.popularmovies.dto;

import lombok.Data;

@Data
public class Review {
    private int id;
    private String author;
    private String content;
}
