package com.android.popularmovies.databaseold;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

import static com.android.popularmovies.utils.Constants.AUTHORITY;

import static com.android.popularmovies.databaseold.FavoriteMoviesContract.Favorites.*;

public class FavoriteMoviesProvider extends ContentProvider {
    private static final String WHERE_CLAUSE = "_id=?";
    private static final int FAVORITE_MOVIES = 500;
    private static final int FAVORITE_MOVIES_ID = 501;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private FavoriteMoviesDBHelper favoriteMoviesDBHelper;
    private SQLiteDatabase favoriteMoviesDB;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, FavoriteMoviesContract.FAV_MOVIES_PATH, FAVORITE_MOVIES);
        uriMatcher.addURI(AUTHORITY, FavoriteMoviesContract.FAV_MOVIES_PATH + "/#", FAVORITE_MOVIES_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();

        favoriteMoviesDBHelper = new FavoriteMoviesDBHelper(context);
        favoriteMoviesDB = favoriteMoviesDBHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int matchedCode = uriMatcher.match(uri);
        Cursor favMoviesCursor;

        switch (matchedCode) {
            case FAVORITE_MOVIES:
                favMoviesCursor = favoriteMoviesDB.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITE_MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                String[] mSelectionArgs = new String[]{id};

                favMoviesCursor = favoriteMoviesDB.query(TABLE_NAME,
                        projection,
                        WHERE_CLAUSE,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }

        favMoviesCursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);

        return favMoviesCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int matchedCode = uriMatcher.match(uri);

        if (matchedCode == FAVORITE_MOVIES) {
            return "vnd.android.cursor.dir" + "/" + AUTHORITY + "/" + FavoriteMoviesContract.FAV_MOVIES_PATH;
        } else if (matchedCode == FAVORITE_MOVIES_ID) {
            return "vnd.android.cursor.item" + "/" + AUTHORITY + "/" + FavoriteMoviesContract.FAV_MOVIES_PATH;
        }
        return "Error finding type";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int matchedCode = uriMatcher.match(uri);
        Uri contentUriAfterInsert = null;

        if (matchedCode == FAVORITE_MOVIES) {
            long id = favoriteMoviesDB.insert(TABLE_NAME, null, values);
            if (id > 0) {
                contentUriAfterInsert = ContentUris.withAppendedId(FavoriteMoviesContract.Favorites.CONTENT_URI, id);
            } else {
                throw new android.database.SQLException("Insertion failed at : " + uri);
            }
        }
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        return contentUriAfterInsert;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int matchedCode = uriMatcher.match(uri);
        int deleteFavoriteMovie = 0;

        if (matchedCode == FAVORITE_MOVIES) {
            deleteFavoriteMovie = favoriteMoviesDB.delete(TABLE_NAME, selection, selectionArgs);
        }

        if (deleteFavoriteMovie != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return deleteFavoriteMovie;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int updatedFavoriteMovie = 0;
        int matchedCode = uriMatcher.match(uri);

        if (matchedCode == FAVORITE_MOVIES_ID) {
            String id = uri.getPathSegments().get(1);
            updatedFavoriteMovie = favoriteMoviesDBHelper.getWritableDatabase()
                    .update(TABLE_NAME, values, WHERE_CLAUSE, new String[]{id});
        }

        if (updatedFavoriteMovie != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return updatedFavoriteMovie;
    }
}