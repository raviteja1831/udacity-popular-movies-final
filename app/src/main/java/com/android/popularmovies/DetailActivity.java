package com.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.popularmovies.adapter.ReviewsAdapter;
import com.android.popularmovies.adapter.TrailersAdapter;
import com.android.popularmovies.database.FavoriteMoviesContract;
import com.android.popularmovies.database.FavoriteMoviesDBHelper;
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
    @BindView(R.id.add_to_favorites)
    Button mFavorites;
    @BindView(R.id.details_scrollView)
    ScrollView mScrollView;

    String[] mProjection =
            {
                    FavoriteMoviesContract.Favorites._ID,
                    FavoriteMoviesContract.Favorites.MOVIE_ID
            };
    Uri mNewUri;

    private String title;
    private String imageUrl;
    private String rating;
    private String overview;
    private String releaseDate;
    private int id;

    private SQLiteDatabase mDb;
    private String[] mSelectionArgs = {""};
    private String mSelectionClause;

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

        FavoriteMoviesDBHelper dbHelper = new FavoriteMoviesDBHelper(this);
        mDb = dbHelper.getWritableDatabase();

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

        id = getIntent().getIntExtra("movieId", 0);

        mFavorites.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //if (isMovieFavorited(id)) {
                if (isMovieFavorited(String.valueOf(id))) {
                    removeFavorites(String.valueOf(id));

                    Context context = getApplicationContext();
                    CharSequence removedFavorites = "This movie is removed from your favorites.";
                    Toast toast = Toast.makeText(context, removedFavorites, Toast.LENGTH_SHORT);
                    toast.show();

                    mFavorites.setText(getString(R.string.add_to_favorites));
                } else {
                    addToFavorites(title, id, imageUrl, rating, releaseDate, overview);
                    Context context = getApplicationContext();
                    CharSequence addedFavorites = "This movie is added to your favorites.";
                    Toast toast = Toast.makeText(context, addedFavorites, Toast.LENGTH_SHORT);
                    toast.show();

                    mFavorites.setText(getString(R.string.remove_from_favorites));
                }
            }
        });

        isMovieFavorited(String.valueOf(id));
    }

    private void populateUserReviews() {
        new DownloadUserReviewsTask().execute(String.valueOf(id));
    }

    private void populateTrailers() {
        new DownloadTrailersTask().execute(String.valueOf(id));
    }

    private void populateMovieDetails() {
        title = getIntent().getStringExtra("movieTitle");
        imageUrl = getIntent().getStringExtra("imageUrl");
        rating = getIntent().getStringExtra("movieRating");
        overview = getIntent().getStringExtra("movieOverview");
        releaseDate = getIntent().getStringExtra("releaseDate");
        id = getIntent().getIntExtra("movieId", 0);

        mMovieTitle.setText(title);
        mMovieOverview.setText(overview);
        mMovieReleaseDate.setText(releaseDate);
        mMovieRating.setText(rating);

        Picasso.get()
                .load(imageUrl)
                .into(mMoviePoster);
    }

    private void addToFavorites(String name, int id, String poster, String rate, String release, String overview) {
        //create a ContentValues instance to pass the values onto the insert query
        ContentValues cv = new ContentValues();
        //call put to insert the values with the keys
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_ID, id);
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_TITLE, name);
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_IMAGE_URL, poster);
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_RATING, rate);
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_RELEASE_DATE, release);
        cv.put(FavoriteMoviesContract.Favorites.MOVIE_OVERVIEW, overview);
        //run an insert query on TABLE_NAME with the ContentValues created
        //return mDb.insert(FavoritesContract.FavoritesAdd.TABLE_NAME, null, cv);
        mNewUri = getContentResolver().insert(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                cv
        );
    }

    //remove favorites
    private void removeFavorites(String id) {
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " LIKE ?";
        String[] selectionArgs = new String[]{id};
        //return mDb.delete(FavoritesContract.FavoritesAdd.TABLE_NAME,
        //      FavoritesContract.FavoritesAdd.COLUMN_MOVIE_ID + "=" + id, null) > 0;
        getContentResolver().delete(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                mSelectionClause,
                selectionArgs
        );
    }

    //https://stackoverflow.com/questions/28236390/recyclerview-store-restore-state-between-activities
    @Override
    protected void onPause() {
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = rv_reviews.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            rv_reviews.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    //check if the id exist in database
    public boolean isMovieFavorited(String id) {
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " = ?";
        mSelectionArgs[0] = id;
        Cursor mCursor = getContentResolver().query(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                null);

        if (mCursor.getCount() <= 0) {
            mCursor.close();
            mFavorites.setText(getString(R.string.add_to_favorites));
            return false;
        }
        mCursor.close();
        mFavorites.setText(getString(R.string.remove_from_favorites));
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

            // error handling to be added for else condition, going to skip for now.
        }
    }
}
