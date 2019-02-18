package com.android.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.popularmovies.R;
import com.android.popularmovies.dto.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {
    private static final String TAG = MoviesAdapter.class.getSimpleName();
    private final Context mContext;
    final private ListItemClickListener mOnClickListener;
    private List<Movie> mMovieList;

    public MoviesAdapter(List<Movie> movieItemList, ListItemClickListener listener, Context context) {

        mMovieList = movieItemList;

        mOnClickListener = listener;
        mContext = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        int layoutIdForListItem = R.layout.movie_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForListItem, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList == null ? 0 : mMovieList.size();
    }

    public void populateMovieInformation(List<Movie> movieList) {
        Log.d(TAG, "total no:of movies - " + movieList.size());

        mMovieList = movieList;
        notifyDataSetChanged();
    }

    public interface ListItemClickListener {
        void OnListItemClick(Movie movieItem);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivMovieImage;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ivMovieImage = itemView.findViewById(R.id.iv_movie_posters);
            itemView.setOnClickListener(this);
        }

        void bind(int index) {
            Movie movie = mMovieList.get(index);

            ivMovieImage = itemView.findViewById(R.id.iv_movie_posters);

            String posterPathURL = movie.getImageUrl();
            try {
                Picasso.get()
                        .load(posterPathURL)
                        .into(ivMovieImage);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mOnClickListener.OnListItemClick(mMovieList.get(adapterPosition));

            Log.d(TAG, "onClick inside MoviesAdapter");
        }
    }

}