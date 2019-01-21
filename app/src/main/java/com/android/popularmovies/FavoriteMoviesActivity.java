package com.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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

public class FavoriteMoviesActivity extends DetailActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerViewReviews;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recyclerView_state";
    private TrailersAdapter mTrailerAdapter;
    private ReviewsAdapter mReviewAdapter;
    private List<Trailer> jsonTrailerData;
    private List<Review> jsonReviewData;
    private int id = 0;
    private String title = "";
    private String poster = "";
    private String rate = "";
    private String release = "";
    private String overview = "";
    private SQLiteDatabase mDb;
    String[] mProjection =
            {
                    FavoriteMoviesContract.Favorites._ID,
                    FavoriteMoviesContract.Favorites.MOVIE_ID
            };

    private String[] mSelectionArgs = {""};
    private String mSelectionClause;
    Uri mNewUri;


    @BindView(R.id.iv_movie_poster)
    ImageView mMoviePosterDisplay;
    @BindView(R.id.tv_movie_title)
    TextView mMovieTitleDisplay;
    @BindView(R.id.tv_rating)
    TextView mMovieRateDisplay;
    @BindView(R.id.tv_release_date)
    TextView mMovieReleaseDisplay;
    @BindView(R.id.tv_movie_overview)
    TextView mMoviePlotSynopsisDisplay;
    @BindView(R.id.add_to_favorites)
    Button mFavorites;
    @BindView(R.id.details_scrollView)
    ScrollView mScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_activity_detail);

        //favorites
        FavoriteMoviesDBHelper dbHelper = new FavoriteMoviesDBHelper(this);
        mDb = dbHelper.getWritableDatabase();



        //trailers
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //set the layout manager
        mRecyclerView.setLayoutManager(layoutManager);
        //changes in content shouldn't change the layout size
        mRecyclerView.setHasFixedSize(true);

        //set trailer adapter for recycler view
        mRecyclerView.setAdapter(mTrailerAdapter);



        //reviews
        mRecyclerViewReviews = (RecyclerView) findViewById(R.id.rv_movie_reviews);

        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);
        //set the layout manager
        mRecyclerViewReviews.setLayoutManager(reviewsLayoutManager);
        //changes in content shouldn't change the layout size
        mRecyclerViewReviews.setHasFixedSize(true);

        //set review adapter for recycler view
        mRecyclerViewReviews.setAdapter(mReviewAdapter);


        ButterKnife.bind(this);


        // https://stackoverflow.com/questions/41791737/how-to-pass-json-image-from-recycler-view-to-another-activity
        poster = getIntent().getStringExtra("imageUrl");
        title = getIntent().getStringExtra("movieTitle");
        rate = getIntent().getStringExtra("movieRating");
        release = getIntent().getStringExtra("releaseDate");
        overview = getIntent().getStringExtra("movieOverview");
        id = getIntent().getIntExtra("movieId",0);


        mMovieTitleDisplay.setText(title);
        mMoviePlotSynopsisDisplay.setText(overview);
        mMovieRateDisplay.setText(rate);
        mMovieReleaseDisplay.setText(release);
        Picasso.get()
                .load(poster)
                .into(mMoviePosterDisplay);

        mFavorites.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isMovieFavorited(String.valueOf(id))) {
                    removeFavorites(String.valueOf(id));

                    Context context = getApplicationContext();
                    CharSequence removedFavorites = "This movie is removed from your favorites.";
                    Toast toast = Toast.makeText(context, removedFavorites, Toast.LENGTH_SHORT);
                    toast.show();

                    mFavorites.setText(getString(R.string.add_to_favorites));
                } else {
                    addToFavorites(title, id, poster, rate, release, overview);
                    Context context = getApplicationContext();
                    CharSequence addedFavorites = "This movie is added to your favorites.";
                    Toast toast = Toast.makeText(context, addedFavorites, Toast.LENGTH_SHORT);
                    toast.show();

                    mFavorites.setText(getString(R.string.remove_from_favorites));
                }
            }
        });


        loadTrailerData();
        loadReviewData();
        isMovieFavorited(String.valueOf(id));
    }


    private void loadTrailerData() {
        String trailerId = String.valueOf(id);
        new FetchTrailerTask().execute(trailerId);
    }

    private void loadReviewData() {
        String reviewId = String.valueOf(id);
        new FetchReviewTask().execute(reviewId);
    }

    // Async Task for trailers
    public class FetchTrailerTask extends AsyncTask<String, Void, List<Trailer>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }

            URL movieRequestUrl = NetworkUtils.populateUrlForTrailerData(id);

            try {
                String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                jsonTrailerData
                        = JsonUtils.populateTrailers(FavoriteMoviesActivity.this, jsonMovieResponse);

                return jsonTrailerData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Trailer> trailerData) {
            if (trailerData != null) {
                mTrailerAdapter = new TrailersAdapter(trailerData, FavoriteMoviesActivity.this);
                mRecyclerView.setAdapter(mTrailerAdapter);
            } else {
            }

        }

    }


    //Async task for reviews
    public class FetchReviewTask extends AsyncTask<String, Void, List<Review>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Review> doInBackground(String... params) {
            if (params.length == 0){
                return null;
            }

            URL movieRequestUrl = NetworkUtils.populateUrlForReviewsData(id);

            try {
                String jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                jsonReviewData
                        = JsonUtils.populateUserReviews(FavoriteMoviesActivity.this, jsonMovieResponse);

                return jsonReviewData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewData) {
            if (reviewData != null) {
                mReviewAdapter = new ReviewsAdapter(reviewData);
                mRecyclerViewReviews.setAdapter(mReviewAdapter);
            } else {
            }
        }

    }


    //add to favorites
    private void addToFavorites(String name, int id, String poster, String rate, String release, String overview){
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
        //return mDb.insert(FavoriteMoviesContract.Favorites.TABLE_NAME, null, cv);
        mNewUri = getContentResolver().insert(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                cv
        );
    }

    //remove favorites
    private void removeFavorites(String id){
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " LIKE ?";
        String[] selectionArgs = new String[] {id};
        //return mDb.delete(FavoritesContract.FavoritesAdd.TABLE_NAME,
        //      FavoritesContract.FavoritesAdd.COLUMN_MOVIE_ID + "=" + id, null) > 0;
        getContentResolver().delete(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                mSelectionClause,
                selectionArgs
        );
    }

    //check if the id exist in database
    public boolean isMovieFavorited(String id){
        mSelectionClause = FavoriteMoviesContract.Favorites.MOVIE_ID + " = ?";
        mSelectionArgs[0] = id;
        Cursor mCursor = getContentResolver().query(
                FavoriteMoviesContract.Favorites.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                null);

        if(mCursor.getCount() <= 0){
            mCursor.close();
            mFavorites.setText(getString(R.string.add_to_favorites));
            return false;
        }
        mCursor.close();
        mFavorites.setText(getString(R.string.remove_from_favorites));
        return true;
    }


    //https://stackoverflow.com/questions/28236390/recyclerview-store-restore-state-between-activities
    @Override
    protected void onPause() {
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerViewReviews.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerViewReviews.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

}
