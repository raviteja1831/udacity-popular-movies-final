package com.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {
    private final MoviesAdapterOnClickHandler onClickHandler;
    private List<Movie> mMoviesList;

    MoviesAdapter(List<Movie> movie, MoviesAdapterOnClickHandler clickHandler) {
        mMoviesList = movie;
        onClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutId = R.layout.movie_item;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutId, viewGroup, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder holder, int position) {
        String movie = mMoviesList.get(position).getImageUrl();
        Picasso.get()
                .load(movie)
                .into(holder.mMoviesImageView);
    }

    @Override
    public int getItemCount() {
        if (mMoviesList.isEmpty()) {
            return 0;
        }
        return mMoviesList.size();
    }

    public interface MoviesAdapterOnClickHandler {
        void onClick(int position);
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mMoviesImageView;

        MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mMoviesImageView = itemView.findViewById(R.id.iv_movie_posters);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            onClickHandler.onClick(adapterPos);
        }
    }
}
