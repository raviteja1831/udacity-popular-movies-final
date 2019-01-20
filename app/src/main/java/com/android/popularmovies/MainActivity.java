package com.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.popularmovies.utils.JsonUtils;
import com.android.popularmovies.utils.NetworkUtils;

import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler {

    String sortByQuery = "popular";
    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> jsonResponseFromTMDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        detailActivityIntent.putExtra("movieTitle", jsonResponseFromTMDB.get(adapterPosition).getTitle());
        detailActivityIntent.putExtra("imageUrl", jsonResponseFromTMDB.get(adapterPosition).getImageUrl());
        detailActivityIntent.putExtra("movieRating", jsonResponseFromTMDB.get(adapterPosition).getRating());
        detailActivityIntent.putExtra("movieOverview", jsonResponseFromTMDB.get(adapterPosition).getOverview());
        detailActivityIntent.putExtra("releaseDate", jsonResponseFromTMDB.get(adapterPosition).getReleaseDate());

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


        return super.onOptionsItemSelected(item);
    }

    //    This AsyncTask class should be static or leaks might occur (com.android.popmovies.MainActivity.DownloadMoviesTask) less... (⌘F1)
//    A static field will leak contexts.
    // Not sure how to address this but hoping it won't break the app as taking the editor suggestion and changing to static then gets fired back while changing the RecyclerView to static and faced similar memory leak error.
    // Guess I need to revisit some classes to refactor all of this, thought of moving out altogether to different object but wasn't sure of how to get access to recyclerView and adapter

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

                jsonResponseFromTMDB = JsonUtils.populateMovies(MainActivity.this, jsonResultsFromTMDB);

                return jsonResponseFromTMDB;

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