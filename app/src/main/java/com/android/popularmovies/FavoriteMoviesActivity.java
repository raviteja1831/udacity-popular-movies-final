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
import com.android.popularmovies.databaseold.FavoriteMoviesContract;
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

public class FavoriteMoviesActivity extends AppCompatActivity {

    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recyclerView_state";
    String[] columnsToReturnProjection =
            {
                    FavoriteMoviesContract.Favorites._ID,
                    FavoriteMoviesContract.Favorites.MOVIE_ID
            };
    Uri newContentUri;

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

    private RecyclerView rv_reviews;
    private RecyclerView rv_trailers;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;

    private String title;
    private String imageUrl;
    private String rating;
    private String overview;
    private String releaseDate;
    private int id;

    private String[] mSelectionArgs = {""};
    private String mSelectionClause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_activity_detail);
        setTitle("Favorite Movie Details");

        ButterKnife.bind(this);

        populateMovieDetails();

        trailers();

        userReviews();

        if (isFavoriteMovie(String.valueOf(id))) {
            favoriteButton.setChecked(true);
        }
        favoriteButtonOnStateChanged();

        isFavoriteMovie(String.valueOf(id));
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
                                addToFavorites(title, id, imageUrl, rating, releaseDate, overview);
                            } else {
                                removeFromFavorites(String.valueOf(id));
                            }

                        } catch (Exception e) {
                            Log.e(FavoriteMoviesActivity.class.getSimpleName(), e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void userReviews() {
        rv_reviews = findViewById(R.id.rv_movie_reviews);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);

        rv_reviews.setLayoutManager(reviewsLayoutManager);
        rv_reviews.setHasFixedSize(true);
        rv_reviews.setAdapter(reviewsAdapter);
        populateUserReviews();
    }

    private void trailers() {
        rv_trailers = findViewById(R.id.rv_trailers);
        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);

        rv_trailers.setLayoutManager(trailersLayoutManager);
        rv_trailers.setHasFixedSize(true);
        rv_trailers.setAdapter(trailersAdapter);
        populateTrailers();
    }


    private void populateMovieDetails() {
        title = getIntent().getStringExtra(MOVIE_TITLE);
        imageUrl = getIntent().getStringExtra(IMAGE_URL);
        rating = getIntent().getStringExtra(MOVIE_RATING);
        overview = getIntent().getStringExtra(MOVIE_OVERVIEW);
        releaseDate = getIntent().getStringExtra(RELEASE_DATE);
        id = getIntent().getIntExtra(MOVIE_ID, 0);

        mMovieTitle.setText(title);
        mMovieOverview.setText(overview);
        mMovieReleaseDate.setText(releaseDate);
        mMovieRating.setText(rating);

        Picasso.get()
                .load(imageUrl)
                .into(mMoviePoster);
    }

    private void populateUserReviews() {
        new DownloadUserReviewsTask().execute(String.valueOf(id));
    }

    private void populateTrailers() {
        new DownloadTrailersTask().execute(String.valueOf(id));
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
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " LIKE ?";
        String[] selectionArgs = new String[]{id};
        getContentResolver().delete(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                mSelectionClause,
                selectionArgs
        );
    }

    public boolean isFavoriteMovie(String id) {
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " = ?";
        mSelectionArgs[0] = id;
        Cursor mCursor = getContentResolver().query(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                columnsToReturnProjection,
                mSelectionClause,
                mSelectionArgs,
                null);

        if (Objects.requireNonNull(mCursor).getCount() <= 0) {
            mCursor.close();
            return false;
        }
        mCursor.close();
        return true;
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
            Objects.requireNonNull(rv_trailers.getLayoutManager()).onRestoreInstanceState(listState);
        }
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

                return JsonUtils.populateUserReviews(FavoriteMoviesActivity.this, reviewsJsonFromTMDB);

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

            URL trailersUrl = NetworkUtils.populateUrlForTrailerData(id);

            try {
                String trailersJsonFromTMDB = NetworkUtils.getResponseFromHttpUrl(trailersUrl);

                return JsonUtils.populateTrailers(FavoriteMoviesActivity.this, trailersJsonFromTMDB);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Trailer> trailerList) {
            if (trailerList != null) {
                trailersAdapter = new TrailersAdapter(trailerList, FavoriteMoviesActivity.this);
                rv_trailers.setAdapter(trailersAdapter);
            }
        }
    }

}