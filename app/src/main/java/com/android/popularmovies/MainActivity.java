package com.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.popularmovies.adapter.MoviesAdapter;
import com.android.popularmovies.dto.Movie;
import com.android.popularmovies.utils.JsonUtils;
import com.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String LIFECYCLE_CALLBACKS_KEY = "callbacks";
    private static Bundle mRecyclerViewStateFromBundle;
    private final String RECYCLER_VIEW_STATE = "recyclerView_state";
    private RecyclerView recyclerView;

    String sortByQuery = "popular";
    private MoviesAdapter adapter;
    private List<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            sortByQuery = savedInstanceState.getString(LIFECYCLE_CALLBACKS_KEY);
        }

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_movies);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        populateMovies();
    }

    private void populateMovies() {
        String initialSort = sortByQuery;
        recyclerView.setVisibility(View.VISIBLE);
        new DownloadMoviesTask().execute(initialSort);
    }

    @Override
    public void onClick(int adapterPosition) {
        Context context = this;
        Class detailActivityClass = DetailActivity.class;

        Intent detailActivityIntent = new Intent(context, detailActivityClass);
        detailActivityIntent.putExtra(Intent.EXTRA_TEXT, adapterPosition);
        detailActivityIntent.putExtra("movieId", movies.get(adapterPosition).getId());
        detailActivityIntent.putExtra("movieTitle", movies.get(adapterPosition).getTitle());
        detailActivityIntent.putExtra("imageUrl", movies.get(adapterPosition).getImageUrl());
        detailActivityIntent.putExtra("movieRating", movies.get(adapterPosition).getRating());
        detailActivityIntent.putExtra("movieOverview", movies.get(adapterPosition).getOverview());
        detailActivityIntent.putExtra("releaseDate", movies.get(adapterPosition).getReleaseDate());

        startActivity(detailActivityIntent);

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
            sortByQuery = "popular";
            populateMovies();
            return true;
        }

        if (menuItem == R.id.action_topRated) {
            sortByQuery = "top_rated";
            populateMovies();
            return true;
        }

        if (menuItem == R.id.action_FavoriteMovie) {
            Context context = this;
            Class destinationClass = FavoritesActivity.class;
            Intent intent = new Intent(context, destinationClass);
            startActivity(intent);
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
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        mRecyclerViewStateFromBundle.putParcelable(RECYCLER_VIEW_STATE, listState);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerViewStateFromBundle != null) {
            Parcelable listState = mRecyclerViewStateFromBundle.getParcelable(RECYCLER_VIEW_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
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
                adapter = new MoviesAdapter(movieList, MainActivity.this);
                recyclerView.setAdapter(adapter);
            }
        }
    }
}