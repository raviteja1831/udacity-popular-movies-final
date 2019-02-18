package com.android.popularmovies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM FavMovies ORDER BY id")
    LiveData<List<FavMovie>> loadAllMovies();

    @Query("SELECT * FROM FavMovies WHERE id = :id")
    FavMovie loadMovieById(int id);

    @Insert
    void insert(FavMovie favMovie);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FavMovie favMovie);

    @Delete
    void delete(FavMovie favMovie);
}
