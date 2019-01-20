package com.android.popularmovies.utils;

import android.content.Context;

import com.android.popularmovies.dto.Movie;
import com.android.popularmovies.dto.Review;
import com.android.popularmovies.dto.Trailer;

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

        List<Movie> movieResults = new ArrayList<>();

        for (int i = 0; i < jsonMoviesArray.length(); i++) {
            Movie movie = new Movie();

            String title = jsonMoviesArray.getJSONObject(i).optString(TMDB_TITLE);
            String releaseDate = jsonMoviesArray.getJSONObject(i).optString(TMDB_RELEASE_DATE);
            String rating = jsonMoviesArray.getJSONObject(i).optString(TMDB_VOTE_AVG);
            String overview = jsonMoviesArray.getJSONObject(i).optString(TMDB_OVERVIEW);
            int id = jsonMoviesArray.getJSONObject(i).optInt(TMDB_ID);

            String poster_path = jsonMoviesArray.getJSONObject(i).optString(TMDB_POSTER_PATH);

            movie.setImageUrl(IMAGE_BASE_URL + TMDB_IMAGE_W500 + poster_path);
            movie.setTitle(title);
            movie.setId(id);
            movie.setReleaseDate(releaseDate);
            movie.setRating(rating);
            movie.setOverview(overview);

            movieResults.add(movie);
        }

        return movieResults;
    }

    public static List<Trailer> populateTrailers(Context context, String json) throws Exception {
        JSONObject jsonTrailer = new JSONObject(json);

        JSONArray jsonTrailersArray = jsonTrailer.getJSONArray(TMDB_RESULTS);

        List<Trailer> trailerResults = new ArrayList<>();

        for (int i = 0; i < jsonTrailersArray.length(); i++) {
            Trailer trailer = new Trailer();

            String key = jsonTrailersArray.getJSONObject(i).optString(TMDB_TRAILER_KEY);
            String name = jsonTrailersArray.getJSONObject(i).optString(TMDB_TRAILER_NAME);

            trailer.setKey(key);
            trailer.setName(name);

            trailerResults.add(trailer);
        }

        return trailerResults;

    }

    public static List<Review> populateUserReviews(Context context, String json) throws JSONException {
        JSONObject reviewJson = new JSONObject(json);

        JSONArray jsonReviewsArray = reviewJson.getJSONArray(TMDB_RESULTS);

        List<Review> reviewResults = new ArrayList<>();

        for (int i = 0; i < jsonReviewsArray.length(); i++) {
            Review review = new Review();

            String author = jsonReviewsArray.getJSONObject(i).optString(TMDB_AUTHOR_KEY);
            String content = jsonReviewsArray.getJSONObject(i).optString(TMDB_CONTENT_KEY);

            review.setAuthor(author);
            review.setContent(content);

            reviewResults.add(review);
        }

        return reviewResults;
    }


}

