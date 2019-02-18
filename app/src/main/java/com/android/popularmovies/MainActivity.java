package com.android.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.popularmovies.adapter.MoviesAdapter;
import com.android.popularmovies.database.FavMovie;
import com.android.popularmovies.dto.Movie;
import com.android.popularmovies.utils.JsonUtils;
import com.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements MoviesAdapter.ListItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String POPULAR_MOVIES_TITLE = "Popular Movies";
    private static final String POPULAR_SORT_QUERY = "popular";

    private static final String TOP_RATED_MOVIES_TITLE = "Top Rated Movies";
    private static final String TOP_RATED_SORT_QUERY = "top_rated";

    private static final String FAVORITE_SORT_QUERY = "favorite";

    private static String sortByQuery = "popular";
    private static final String LIFECYCLE_CALLBACKS_KEY = "callbacks";
    private static Bundle mRecyclerViewStateFromBundle;
    private final String RECYCLER_VIEW_STATE = "recyclerView_state";
    private RecyclerView recyclerView;
    private MoviesAdapter moviesAdapter;

    private List<Movie> movies;
    private List<FavMovie> favMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sortByQuery = savedInstanceState.getString(LIFECYCLE_CALLBACKS_KEY);
        }

        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.rv_movies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        moviesAdapter = new MoviesAdapter(movies, this, this);
        recyclerView.setAdapter(moviesAdapter);

        setTitle(POPULAR_MOVIES_TITLE);
        favMovies = new ArrayList<>();
        setupViewModel();

    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getMovies().observe(this, new Observer<List<FavMovie>>() {

            @Override
            public void onChanged(@Nullable List<FavMovie> favMovieList) {
                if (Objects.requireNonNull(favMovieList).size() > 0) {
                    favMovies.clear();
                    favMovies = favMovieList;
                }
                for (int i = 0; i < favMovies.size(); i++) {
                    Log.d(TAG, favMovies.get(i).getTitle());
                }
                loadMovies();
            }
        });
    }

    private void loadMovies() {
        if (sortByQuery.equals(FAVORITE_SORT_QUERY)) {
            clearList();
            for (int i = 0; i < favMovies.size(); i++) {
                Movie movie = new Movie();
                movie.setId(favMovies.get(i).getId());
                movie.setTitle(favMovies.get(i).getTitle());
                movie.setRating(favMovies.get(i).getRating());
                movie.setOverview(favMovies.get(i).getOverview());
                movie.setReleaseDate(favMovies.get(i).getReleaseDate());
                movie.setImageUrl(favMovies.get(i).getImageUrl());
                movies.add(movie);
            }
            moviesAdapter.populateMovieInformation(movies);
        } else {
            String sortQuery = sortByQuery;
            new DownloadMoviesTask().execute(sortQuery);
        }
    }

    private void clearList() {
        if (movies != null) {
            movies.clear();
        } else {
            movies = new ArrayList<Movie>();
        }
    }


    @Override
    public void OnListItemClick(Movie movieItem) {
        Intent myIntent = new Intent(this, DetailActivity.class);
        myIntent.putExtra("movieItem", movieItem);
        startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItem = item.getItemId();

        if (menuItem == R.id.action_popular) {
            clearList();
            setTitle(POPULAR_MOVIES_TITLE);
            sortByQuery = POPULAR_SORT_QUERY;
            loadMovies();
            return true;
        }

        if (menuItem == R.id.action_topRated) {
            clearList();
            setTitle(TOP_RATED_MOVIES_TITLE);
            sortByQuery = TOP_RATED_SORT_QUERY;
            loadMovies();
            return true;
        }

        if (menuItem == R.id.action_FavoriteMovie) {
            clearList();
            setTitle("Favorite Movies");
            sortByQuery = FAVORITE_SORT_QUERY;
            loadMovies();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        sortByQuery = savedInstanceState.getString(LIFECYCLE_CALLBACKS_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String lifecycleSortBy = sortByQuery;
        outState.putString(LIFECYCLE_CALLBACKS_KEY, lifecycleSortBy);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        mRecyclerViewStateFromBundle = new Bundle();
        Parcelable listState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        mRecyclerViewStateFromBundle.putParcelable(RECYCLER_VIEW_STATE, listState);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerViewStateFromBundle != null) {
            Parcelable listState = mRecyclerViewStateFromBundle.getParcelable(RECYCLER_VIEW_STATE);
            Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(listState);
        }
    }

    public class DownloadMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            String sortByParam = params[0];
            URL url = NetworkUtils.populateURL(sortByParam);

            try {
                String jsonResultsFromTMDB = NetworkUtils.getResponseFromHttpUrl(url);

                movies = JsonUtils.populateMovies(MainActivity.this, jsonResultsFromTMDB);

                return movies;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            if (!movieList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                moviesAdapter.populateMovieInformation(movies);
            }
        }
    }
}