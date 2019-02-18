package com.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.popularmovies.adapter.ReviewsAdapter;
import com.android.popularmovies.adapter.TrailersAdapter;
import com.android.popularmovies.database.FavMovie;
import com.android.popularmovies.database.FavMovieDatabase;
import com.android.popularmovies.dto.Movie;
import com.android.popularmovies.dto.Review;
import com.android.popularmovies.dto.Trailer;
import com.android.popularmovies.utils.AppExecutors;
import com.android.popularmovies.utils.JsonUtils;
import com.android.popularmovies.utils.NetworkUtils;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.iv_movie_poster)
    ImageView mMoviePoster;
    @BindView(R.id.tv_movie_title)
    TextView mMovieTitle;
    @BindView(R.id.tv_movie_overview)
    TextView mMovieOverview;
    @BindView(R.id.tv_release_date)
    TextView mMovieReleaseDate;
    @BindView(R.id.tv_rating)
    TextView mMovieRating;
    @BindView(R.id.btn_favourite)
    ShineButton favoriteButton;
    @BindView(R.id.details_scrollView)
    ScrollView mScrollView;

    private Movie movie;
    private int movieId;

    private RecyclerView rv_trailers;
    private TrailersAdapter trailersAdapter;

    private RecyclerView rv_reviews;
    private ReviewsAdapter reviewsAdapter;

    private FavMovieDatabase database;
    private Boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("Movie details");

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            Toast.makeText(this, "intent is null!!", Toast.LENGTH_SHORT).show();
        }

        movie = (Movie) Objects.requireNonNull(intent).getSerializableExtra("movieItem");

        if (movie == null) {
            finish();
            Toast.makeText(this, "no movies found.", Toast.LENGTH_SHORT).show();
            return;
        }
        movieId = movie.getId();

        database = FavMovieDatabase.getInstance(getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final FavMovie favMovie = database.movieDao().loadMovieById(movie.getId());
                setFavoriteMovie(favMovie != null ? true : false);
            }
        });

        populateMovieDetails();
        trailers();
        userReviews();

        favoriteButtonOnStateChanged();
    }

    private void setFavoriteMovie(Boolean favorite) {
        if (favorite) {
            isFavorite = true;
            favoriteButton.setChecked(true);
        } else {
            isFavorite = false;
            favoriteButton.setChecked(false);
        }

    }

    private void trailers() {
        rv_trailers = findViewById(R.id.rv_trailers);
        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);

        rv_trailers.setLayoutManager(trailersLayoutManager);
        rv_trailers.setHasFixedSize(true);
        rv_trailers.setAdapter(trailersAdapter);
        populateTrailers();
    }

    private void userReviews() {
        rv_reviews = findViewById(R.id.rv_movie_reviews);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);

        rv_reviews.setLayoutManager(reviewsLayoutManager);
        rv_reviews.setHasFixedSize(true);
        rv_reviews.setAdapter(reviewsAdapter);
        rv_reviews.setVisibility(View.INVISIBLE);
        populateUserReviews();
    }

    private void favoriteButtonOnStateChanged() {
        favoriteButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, final boolean checked) {

                final FavMovie favMovie = new FavMovie(movie.getId(), movie.getTitle(), movie.getImageUrl(), movie.getRating(), movie.getOverview(), movie.getReleaseDate());

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                            if (checked) {
                                database.movieDao().insert(favMovie);
                            } else {
                                database.movieDao().delete(favMovie);
                            }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFavoriteMovie(!isFavorite);
                            }
                        });
                    }
                });
            }
        });
    }

    private void populateUserReviews() {
        new DownloadUserReviewsTask().execute(String.valueOf(movieId));
    }

    private void populateTrailers() {
        new DownloadTrailersTask().execute(String.valueOf(movieId));
    }

    private void populateMovieDetails() {
        String imageUrl = movie.getImageUrl();
        movieId = movie.getId();

        mMovieTitle.setText(movie.getTitle());
        mMovieOverview.setText(movie.getOverview());
        mMovieReleaseDate.setText(movie.getReleaseDate());
        mMovieRating.setText(movie.getRating());

        Picasso.get()
                .load(imageUrl)
                .into(mMoviePoster);
    }

    public class DownloadUserReviewsTask extends AsyncTask<String, Void, List<Review>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Review> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            URL reviewsUrl = NetworkUtils.populateUrlForReviewsData(movieId);

            try {
                String reviewsJsonFromTMDB = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);

                return JsonUtils.populateUserReviews(DetailActivity.this, reviewsJsonFromTMDB);


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewsList) {
            if (!reviewsList.isEmpty()) {
                TextView tv_userReviews_label = findViewById(R.id.tv_userReviews_label);
                tv_userReviews_label.setVisibility(View.VISIBLE);
                rv_reviews.setVisibility(View.VISIBLE);
                reviewsAdapter = new ReviewsAdapter(reviewsList);
                rv_reviews.setAdapter(reviewsAdapter);
            }
        }

    }

    public class DownloadTrailersTask extends AsyncTask<String, Void, List<Trailer>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            URL trailersUrl = NetworkUtils.populateUrlForTrailerData(movieId);

            try {
                String trailersJsonFromTMDB = NetworkUtils.getResponseFromHttpUrl(trailersUrl);

                return JsonUtils.populateTrailers(DetailActivity.this, trailersJsonFromTMDB);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Trailer> trailerList) {
            if (trailerList != null) {
                trailersAdapter = new TrailersAdapter(trailerList, DetailActivity.this);
                rv_trailers.setAdapter(trailersAdapter);
            }
        }
    }
}
