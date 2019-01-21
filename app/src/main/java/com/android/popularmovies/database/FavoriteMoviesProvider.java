package com.android.popularmovies.database;

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

import static com.android.popularmovies.utils.Constants.AUTHORITY;

import static com.android.popularmovies.database.FavoriteMoviesContract.Favorites.*;

public class FavoriteMoviesProvider extends ContentProvider {
    private static final int FAVORITE_MOVIES = 500;
    private static final int FAVORITE_MOVIES_ID = 501;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private FavoriteMoviesDBHelper favoriteMoviesDBHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, FavoriteMoviesContract.FAV_MOVIES_PATH, FAVORITE_MOVIES);
        uriMatcher.addURI("com.android.popularmovies", FavoriteMoviesContract.FAV_MOVIES_PATH + "/#", FAVORITE_MOVIES_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        favoriteMoviesDBHelper = new FavoriteMoviesDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = favoriteMoviesDBHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);
        Cursor returnCursor;

        switch (match) {
            case FAVORITE_MOVIES:
                returnCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITE_MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                returnCursor = db.query(TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);

        switch (match) {
            case FAVORITE_MOVIES:
                return "vnd.android.cursor.dir" + "/" + AUTHORITY + "/" + FavoriteMoviesContract.FAV_MOVIES_PATH;
            case FAVORITE_MOVIES_ID:
                return "vnd.android.cursor.item" + "/" + AUTHORITY + "/" + FavoriteMoviesContract.FAV_MOVIES_PATH;
            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = favoriteMoviesDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri returnUri; //Uri to be returned

        switch (match) {
            case FAVORITE_MOVIES:
                long id = db.insert(TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(FavoriteMoviesContract.Favorites.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = favoriteMoviesDBHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int favoritesDeleted;

        switch (match) {
            case FAVORITE_MOVIES:
                favoritesDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }

        if (favoritesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return favoritesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int favoriteUpdated;
        int match = uriMatcher.match(uri);

        switch (match) {
            case FAVORITE_MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                favoriteUpdated = favoriteMoviesDBHelper.getWritableDatabase()
                        .update(TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Cannot find location - " + uri);
        }

        if (favoriteUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return favoriteUpdated;
    }
}
