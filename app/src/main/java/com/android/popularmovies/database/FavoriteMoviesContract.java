package com.android.popularmovies.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteMoviesContract {

    static final Uri BASE_URI = Uri.parse("content://" + "com.android.popularmovies");
    static final String FAV_MOVIES_PATH = "favoriteMovies";

    public static final class Favorites implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_URI.buildUpon().appendPath(FAV_MOVIES_PATH).build();

        public static final String TABLE_NAME = "favoriteMovies";

        public static final String MOVIE_ID = "movieId";
        public static final String MOVIE_TITLE = "movieTitle";
        public static final String MOVIE_IMAGE_URL = "imageUrl";
        public static final String MOVIE_RATING = "movieRating";
        public static final String MOVIE_OVERVIEW = "movieOverview";
        public static final String MOVIE_RELEASE_DATE = "releaseDate";
    }
}
