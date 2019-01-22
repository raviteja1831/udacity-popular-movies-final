package com.android.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.popularmovies.adapter.ReviewsAdapter;
import com.android.popularmovies.adapter.TrailersAdapter;
import com.android.popularmovies.database.FavoriteMoviesContract;
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

import static com.android.popularmovies.utils.Constants.IMAGE_URL;
import static com.android.popularmovies.utils.Constants.MOVIE_ID;
import static com.android.popularmovies.utils.Constants.MOVIE_OVERVIEW;
import static com.android.popularmovies.utils.Constants.MOVIE_RATING;
import static com.android.popularmovies.utils.Constants.MOVIE_TITLE;
import static com.android.popularmovies.utils.Constants.RELEASE_DATE;

public class DetailActivity extends AppCompatActivity {
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recyclerView_state";

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

    String[] columnsToReturnProjection =
            {
                    FavoriteMoviesContract.Favorites._ID,
                    FavoriteMoviesContract.Favorites.MOVIE_ID
            };
    Uri newContentUri;

    private String movieTitle;
    private String imageUrl;
    private String movieRating;
    private String movieOverview;
    private String releaseDate;
    private int movieId;

    private String[] selectionArgs = {""};
    private String selectionClause;

    private RecyclerView rv_trailers;
    private TrailersAdapter trailersAdapter;

    private RecyclerView rv_reviews;
    private ReviewsAdapter reviewsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("Movie details");

        ButterKnife.bind(this);

        populateMovieDetails();
        trailers();
        userReviews();

        if (isFavoriteMovie(String.valueOf(movieId))) {
            favoriteButton.setChecked(true);
        }
        favoriteButtonOnStateChanged();
        isFavoriteMovie(String.valueOf(movieId));
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
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (checked) {
                                addToFavorites(movieTitle, movieId, imageUrl, movieRating, releaseDate, movieOverview);
                            } else {
                                removeFromFavorites(String.valueOf(movieId));
                            }

                        } catch (Exception e) {
                            Log.e(DetailActivity.class.getSimpleName(), e.getMessage());
                        }
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
        movieTitle = getIntent().getStringExtra(MOVIE_TITLE);
        imageUrl = getIntent().getStringExtra(IMAGE_URL);
        movieRating = getIntent().getStringExtra(MOVIE_RATING);
        movieOverview = getIntent().getStringExtra(MOVIE_OVERVIEW);
        releaseDate = getIntent().getStringExtra(RELEASE_DATE);
        movieId = getIntent().getIntExtra(MOVIE_ID, 0);

        mMovieTitle.setText(movieTitle);
        mMovieOverview.setText(movieOverview);
        mMovieReleaseDate.setText(releaseDate);
        mMovieRating.setText(movieRating);

        Picasso.get()
                .load(imageUrl)
                .into(mMoviePoster);
    }

    private void addToFavorites(String name, int id, String poster, String rate, String release, String overview) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_ID, id);
        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_TITLE, name);
        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_IMAGE_URL, poster);
        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_RATING, rate);
        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_RELEASE_DATE, release);
        contentValues.put(FavoriteMoviesContract.Favorites.MOVIE_OVERVIEW, overview);


        newContentUri = getContentResolver().insert(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                contentValues
        );
    }

    private void removeFromFavorites(String id) {
        selectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " LIKE ?";
        String[] selectionArgs = new String[]{id};

        getContentResolver().delete(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                selectionClause,
                selectionArgs
        );
    }

    @Override
    protected void onPause() {
        mBundleRecyclerViewState = new Bundle();

        Parcelable listState = Objects.requireNonNull(rv_reviews.getLayoutManager()).onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            Objects.requireNonNull(rv_reviews.getLayoutManager()).onRestoreInstanceState(listState);
        }
    }

    public boolean isFavoriteMovie(String id) {
        selectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " = ?";
        selectionArgs[0] = id;
        Cursor mCursor = getContentResolver().query(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                columnsToReturnProjection,
                selectionClause,
                selectionArgs,
                null);

        if (Objects.requireNonNull(mCursor).getCount() <= 0) {
            mCursor.close();
            return false;
        }
        mCursor.close();
        return true;
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
            // having hard time how to not have the label when there is no reviews available, moved around this piece of code to multiple places, need some help on how to not create a TextView
            // when the underlying data is not available for a particular movie.

            // TextView tv_userReviews_label = findViewById(R.id.tv_userReviews_label);
            // tv_userReviews_label.setVisibility(View.VISIBLE);

            // leaving comment as I struggled a bit here and not sure if using Retrofit or Volley would have made this easier, but going to explore for sure.

            if (!reviewsList.isEmpty()) {
                TextView tv_userReviews_label = findViewById(R.id.tv_userReviews_label);
                tv_userReviews_label.setVisibility(View.VISIBLE);
                rv_reviews.setVisibility(View.VISIBLE);
                reviewsAdapter = new ReviewsAdapter(reviewsList);
                rv_reviews.setAdapter(reviewsAdapter);
            }

            // error handling and moving to use Retrofit for populating all this data to be added later , going to skip for now.
            // going hardcode, starting with duplicating DetailActivity, going to find efficient way to load FavoriteMoviesActivity without breaking DRY principle
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
