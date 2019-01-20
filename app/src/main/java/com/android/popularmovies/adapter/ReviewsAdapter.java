package com.android.popularmovies.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.popularmovies.R;
import com.android.popularmovies.dto.Review;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private List<Review> mUserReviews;
    private TextView tv_review_author;
    private TextView tv_review_content;

    public ReviewsAdapter(List<Review> reviews) {
        mUserReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutId = R.layout.review_item;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutId, viewGroup, shouldAttachToParentImmediately);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder reviewsAdapterViewHolder, int position) {
        tv_review_author.setText("Written by " + mUserReviews.get(position).getAuthor());
        tv_review_content.setText(mUserReviews.get(position).getContent());
        tv_review_content.setTypeface(tv_review_content.getTypeface(), Typeface.ITALIC);

    }

    @Override
    public int getItemCount() {
        if (mUserReviews.isEmpty()) {
            return 0;
        }
        return mUserReviews.size();
    }

    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {

        ReviewsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_review_author = itemView.findViewById(R.id.tv_review_author);
            tv_review_content = itemView.findViewById(R.id.tv_review_content);
        }
    }
}
