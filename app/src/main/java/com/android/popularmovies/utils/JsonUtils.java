package com.android.popularmovies.utils;

import android.content.Context;

import com.android.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.popularmovies.utils.Constants.*;

public class JsonUtils {

    public static List<Movie> populateMovies(Context context, String json) throws JSONException {

        JSONObject jsonMovie = new JSONObject(json);

        JSONArray jsonMoviesArray = jsonMovie.getJSONArray(TMDB_RESULTS);

        List<Movie> results = new ArrayList<>();

        for (int i = 0; i < jsonMoviesArray.length(); i++) {
            Movie movie = new Movie();

            String title = jsonMoviesArray.getJSONObject(i).optString(TMDB_TITLE);
            String releaseDate = jsonMoviesArray.getJSONObject(i).optString(TMDB_RELEASE_DATE);
            String rating = jsonMoviesArray.getJSONObject(i).optString(TMDB_VOTE_AVG);
            String overview = jsonMoviesArray.getJSONObject(i).optString(TMDB_OVERVIEW);

            String poster_path = jsonMoviesArray.getJSONObject(i).optString(TMDB_POSTER_PATH);

            movie.setImageUrl(IMAGE_BASE_URL + TMDB_IMAGE_W500 + poster_path);
            movie.setTitle(title);
            movie.setReleaseDate(releaseDate);
            movie.setRating(rating);
            movie.setOverview(overview);

            results.add(movie);
        }

        return results;
    }
}

