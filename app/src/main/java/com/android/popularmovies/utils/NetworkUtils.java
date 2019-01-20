package com.android.popularmovies.utils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static com.android.popularmovies.utils.Constants.API_KEY;
import static com.android.popularmovies.utils.Constants.API_KEY_PARAM;
import static com.android.popularmovies.utils.Constants.MOVIES_BASE_URL;

public class NetworkUtils {
    public static URL populateURL(String sortBy) {
        Uri movieDataUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(sortBy)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        URL movieDataUrl = null;
        try {
            movieDataUrl = new URL(movieDataUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return movieDataUrl;
    }

    public static URL populateUrlForTrailerData(int movieId) {
        Uri trailerDataUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(movieId))
                .appendEncodedPath("videos")
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        URL trailerDataUrl = null;
        try {
            trailerDataUrl = new URL(trailerDataUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return trailerDataUrl;
    }

    public static URL populateUrlForReviewsData(int movieId) {
        Uri reviewDataUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(movieId))
                .appendEncodedPath("reviews")
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        URL reviewDataUrl = null;
        try {
            reviewDataUrl = new URL(reviewDataUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return reviewDataUrl;
    }


    // took this bit from the below file
    // https://github.com/udacity/ud851-Sunshine/blob/S04.01-Exercise-LaunchNewActivity/app/src/main/java/com/example/android/sunshine/utilities/NetworkUtils.java

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
