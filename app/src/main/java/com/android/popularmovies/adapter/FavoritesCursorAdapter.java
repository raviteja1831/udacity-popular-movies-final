package com.android.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.popularmovies.FavoriteMoviesActivity;
import com.android.popularmovies.R;
import com.android.popularmovies.database.FavoriteMoviesContract.*;
import com.squareup.picasso.Picasso;

public class FavoritesCursorAdapter extends RecyclerView.Adapter<FavoritesCursorAdapter.FavoriteViewHolder> {

    private Cursor mCursor;
    private Context mContext;


    public FavoritesCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(Favorites._ID);
        int posterIndex = mCursor.getColumnIndex(Favorites.MOVIE_IMAGE_URL);

        mCursor.moveToPosition(position);

        int id = mCursor.getInt(idIndex);
        String imageUrl = mCursor.getString(posterIndex);

        holder.itemView.setTag(id);
        Picasso.get()
                .load(imageUrl)
                .into(holder.mMovieListImageView);


    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }

        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mMovieListImageView;

        public FavoriteViewHolder(View itemView) {
            super(itemView);

            mMovieListImageView = (ImageView) itemView.findViewById(R.id.iv_movie_posters);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            Class destinationClass = FavoriteMoviesActivity.class;

            int movieId = mCursor.getInt(mCursor.getColumnIndex(Favorites.MOVIE_ID));
            String title = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_TITLE));
            String imageUrl = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_IMAGE_URL));
            String rating = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_RATING));
            String overview = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_OVERVIEW));
            String releaseDate = mCursor.getString(mCursor.getColumnIndex(Favorites.MOVIE_RELEASE_DATE));

            Intent intentToStartDetailActivity = new Intent(mContext, destinationClass);
            intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, adapterPosition);
            intentToStartDetailActivity.putExtra("movieId", movieId);
            intentToStartDetailActivity.putExtra("movieTitle", title);
            intentToStartDetailActivity.putExtra("imageUrl", imageUrl);
            intentToStartDetailActivity.putExtra("movieRating", rating);
            intentToStartDetailActivity.putExtra("movieOverview", overview);
            intentToStartDetailActivity.putExtra("releaseDate", releaseDate);

            mContext.startActivity(intentToStartDetailActivity);

        }
    }
}
