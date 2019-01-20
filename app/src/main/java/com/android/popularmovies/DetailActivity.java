package com.android.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.popularmovies.adapter.ReviewsAdapter;
import com.android.popularmovies.adapter.TrailersAdapter;
import com.android.popularmovies.dto.Review;
import com.android.popularmovies.dto.Trailer;
import com.android.popularmovies.utils.JsonUtils;
import com.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

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

    private String title;
    private String moviePoster;
    private String rating;
    private String overview;
    private String releaseDate;
    private int id;


    private RecyclerView rv_trailers;
    private TrailersAdapter trailersAdapter;
    private List<Trailer> trailers;

    private RecyclerView rv_reviews;
    private ReviewsAdapter reviewsAdapter;
    private List<Review> reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        populateMovieDetails();

        rv_trailers = findViewById(R.id.rv_trailers);
        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);

        rv_trailers.setLayoutManager(trailersLayoutManager);
        rv_trailers.setHasFixedSize(true);
        rv_trailers.setAdapter(trailersAdapter);
        populateTrailers();

        rv_reviews = findViewById(R.id.rv_movie_reviews);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);

        rv_reviews.setLayoutManager(reviewsLayoutManager);
        rv_reviews.setHasFixedSize(true);
        rv_reviews.setAdapter(reviewsAdapter);
        populateUserReviews();

    }

    private void populateUserReviews() {
        new DownloadUserReviewsTask().execute(String.valueOf(id));
    }

    private void populateTrailers() {
        new DownloadTrailersTask().execute(String.valueOf(id));
    }

    private void populateMovieDetails() {
        title = getIntent().getStringExtra("movieTitle");
        moviePoster = getIntent().getStringExtra("imageUrl");
        rating = getIntent().getStringExtra("movieRating");
        overview = getIntent().getStringExtra("movieOverview");
        releaseDate = getIntent().getStringExtra("releaseDate");
        id = getIntent().getIntExtra("movieId", 0);

        mMovieTitle.setText(title);
        mMovieOverview.setText(overview);
        mMovieReleaseDate.setText(releaseDate);
        mMovieRating.setText(rating);

        Picasso.get()
                .load(moviePoster)
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

            URL reviewsUrl = NetworkUtils.populateUrlForReviewsData(id);

            try {
                String reviewsJsonFromTMDB = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);

                reviews = JsonUtils.populateUserReviews(DetailActivity.this, reviewsJsonFromTMDB);
                return reviews;


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewsList) {
            if (!reviewsList.isEmpty()) {
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

            URL trailersUrl = NetworkUtils.populateUrlForTrailerData(id);

            try {
                String trailersJsonFromTMDB = NetworkUtils.getResponseFromHttpUrl(trailersUrl);

                trailers
                        = JsonUtils.populateTrailers(DetailActivity.this, trailersJsonFromTMDB);

                return trailers;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Trailer> trailerList) {
            if (trailerList != null) {
//                 having hard time how to not have the label when there is no reviews available, moved around this piece of code to and from multiple places, need some help on how to not create a TextView
//                 when the underlying data is not available for a particular movie.

//                TextView tv_userReviews_label = findViewById(R.id.tv_userReviews_label);
//                tv_userReviews_label.setVisibility(View.VISIBLE);

                trailersAdapter = new TrailersAdapter(trailerList, DetailActivity.this);
                rv_trailers.setAdapter(trailersAdapter);
            }

            // error handling can be added to else condition, going to skip for now.
        }
    }
}
