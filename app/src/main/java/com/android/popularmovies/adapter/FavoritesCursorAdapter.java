package com.android.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.popularmovies.FavoriteMoviesActivity;
import com.android.popularmovies.R;
import com.android.popularmovies.database.FavoriteMoviesContract.*;
import com.squareup.picasso.Picasso;

import static com.android.popularmovies.utils.Constants.IMAGE_URL;
import static com.android.popularmovies.utils.Constants.MOVIE_ID;
import static com.android.popularmovies.utils.Constants.MOVIE_OVERVIEW;
import static com.android.popularmovies.utils.Constants.MOVIE_RATING;
import static com.android.popularmovies.utils.Constants.MOVIE_TITLE;
import static com.android.popularmovies.utils.Constants.RELEASE_DATE;

public class FavoritesCursorAdapter extends RecyclerView.Adapter<FavoritesCursorAdapter.FavoriteMoviesViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    public FavoritesCursorAdapter(Context context) {
        this.mContext = context;
    }


    @NonNull
    @Override
    public FavoriteMoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new FavoriteMoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteMoviesViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(Favorites._ID);
        int posterIndex = mCursor.getColumnIndex(Favorites.MOVIE_IMAGE_URL);

        mCursor.moveToPosition(position);

        int id = mCursor.getInt(idIndex);
        String imageUrl = mCursor.getString(posterIndex);

        holder.itemView.setTag(id);
        Picasso.get()
                .load(imageUrl)
                .into(holder.mMovieImageView);
    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor tempCursor = mCursor;
        this.mCursor = cursor;
        this.notifyDataSetChanged();

        return tempCursor;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    class FavoriteMoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mMovieImageView;

        FavoriteMoviesViewHolder(View itemView) {
            super(itemView);

            mMovieImageView = itemView.findViewById(R.id.iv_movie_posters);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            Class favoriteMoviesActivityClass = FavoriteMoviesActivity.class;

            int movieId = mCursor.getInt(mCursor.getColumnIndex(Favorites.MOVIE_ID));
            String movieTitle = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_TITLE));
            String imageUrl = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_IMAGE_URL));
            String movieRating = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_RATING));
            String overview = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_OVERVIEW));
            String releaseDate = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_RELEASE_DATE));

            Intent intentToStartFavoriteMoviesActivity = new Intent(mContext, favoriteMoviesActivityClass);

            intentToStartFavoriteMoviesActivity.putExtra(Intent.EXTRA_TEXT, adapterPosition);
            intentToStartFavoriteMoviesActivity.putExtra(MOVIE_ID, movieId);
            intentToStartFavoriteMoviesActivity.putExtra(MOVIE_TITLE, movieTitle);
            intentToStartFavoriteMoviesActivity.putExtra(IMAGE_URL, imageUrl);
            intentToStartFavoriteMoviesActivity.putExtra(MOVIE_RATING, movieRating);
            intentToStartFavoriteMoviesActivity.putExtra(MOVIE_OVERVIEW, overview);
            intentToStartFavoriteMoviesActivity.putExtra(RELEASE_DATE, releaseDate);

            mContext.startActivity(intentToStartFavoriteMoviesActivity);

        }
    }
}