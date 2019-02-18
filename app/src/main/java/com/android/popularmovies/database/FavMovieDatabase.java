package com.android.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {FavMovie.class}, version = 1, exportSchema = false)
public abstract class FavMovieDatabase extends RoomDatabase {

    private static final String LOG_TAG = FavMovieDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favoritesDb";
    private static FavMovieDatabase sInstance;

    public static FavMovieDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        FavMovieDatabase.class, FavMovieDatabase.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();

                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    public abstract MovieDao movieDao();
}
