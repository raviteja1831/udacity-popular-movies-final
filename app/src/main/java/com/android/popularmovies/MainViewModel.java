package com.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.android.popularmovies.database.FavMovie;
import com.android.popularmovies.database.FavMovieDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<FavMovie>> movies;

    public MainViewModel(Application application) {
        super(application);

        FavMovieDatabase database = FavMovieDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving favorite movies from the DataBase");

        movies = database.movieDao().loadAllMovies();
    }

    public LiveData<List<FavMovie>> getMovies() {
        return movies;
    }
}
