package com.android.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.popularmovies.R;
import com.android.popularmovies.dto.Trailer;

import java.util.List;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private List<Trailer> mTrailers;
    private TextView tv_movie_trailers;
    private Context context;

    public TrailersAdapter(List<Trailer> trailers, Context context) {
        mTrailers = trailers;
        this.context = context;
    }

    @NonNull
    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutId = R.layout.trailer_item;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutId, viewGroup, shouldAttachToParentImmediately);
        return new TrailersAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapterViewHolder holder, int position) {
        tv_movie_trailers.setText(mTrailers.get(position).getName());

        // https://stackoverflow.com/questions/7324759/how-to-display-thumbnail-of-youtube-videos-in-android/38740186#38740186
        // found some help online on how to display thumbnail images but not sure how to make onClick of images to open youtube app.
        // any kind of guidance is appreciated, will be working on smaller project once Im done with submissions.

        final String trailerToPlayOnVideoApp = mTrailers.get(position).getKey();
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri openTrailerVideo = Uri.parse(YOUTUBE_BASE_URL + trailerToPlayOnVideoApp);

                Intent intent = new Intent(Intent.ACTION_VIEW, openTrailerVideo);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mTrailers.isEmpty()) {
            return 0;
        }
        return mTrailers.size();
    }

    class TrailersAdapterViewHolder extends RecyclerView.ViewHolder {
        TrailersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_movie_trailers = itemView.findViewById(R.id.tv_movie_trailers);
        }
    }
}
