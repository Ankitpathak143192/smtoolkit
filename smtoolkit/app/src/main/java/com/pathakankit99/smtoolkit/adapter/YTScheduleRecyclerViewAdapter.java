package com.pathakankit99.smtoolkit.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.database.videoList;

import java.util.ArrayList;

/**
 * The type video recycler view adapter.
 */

public class YTScheduleRecyclerViewAdapter extends RecyclerView.Adapter<YTScheduleRecyclerViewAdapter.ViewHolder> {

    private ArrayList<videoList> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    /**
     * Instantiates a new video recycler view adapter.
     *
     * @param context the context
     * @param data    the data
     */

    // data is passed into the constructor
    public YTScheduleRecyclerViewAdapter(Context context, ArrayList<videoList> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_video_schedule_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.videoNumberTextView.setText("Video Number: "+mData.get(position).getVideoNumber());
        holder.videoNameTextView.setText(mData.get(position).getTitle());
        holder.videoShootDateTextView.setText("Shoot Date: "+mData.get(position).getVideoShootDate());
        holder.videoPublishDateTextView.setText("Publish Date: " +mData.get(position).getVideoPublishDate());
        holder.videoStatus.setText("Video Current Status: "+mData.get(position).getVideoStatus());
        String test=mData.get(position).getVideoStatus();
        if (test.contains("Published")) {
            holder.childView.setBackgroundColor(Color.parseColor("#084533"));
        }


    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData != null)
            return mData.size();
        else return 0;
    }

    /**
     * Gets item.
     * @param id the id
     * @return the item
     */

    // convenience method for getting data at click position
    public videoList getItem(int id) {
        return mData.get(id);
    }


    public ArrayList<videoList> getData() {
        return mData;
    }

    /**
     * Sets click listener.
     * @param itemClickListener the item click listener
     */

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * The interface Item click listener.
     */
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        /**
         * On item click.
         * @param view     the view
         * @param position the position
         */
        void onItemClick(View view, int position);
    }

    /**
     * The type View holder.
     */

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView videoNameTextView,videoNumberTextView,videoPublishDateTextView, videoShootDateTextView, videoStatus;
        LinearLayout childView;

        /**
         * Instantiates a new View holder.
         * @param itemView the item view
         */
        ViewHolder(View itemView) {
            super(itemView);
            videoNameTextView = itemView.findViewById(R.id.videoNameTV);
            videoNumberTextView = itemView.findViewById(R.id.videoNumberTV);
            videoPublishDateTextView = itemView.findViewById(R.id.videoPublishDateTV);
            videoShootDateTextView=itemView.findViewById(R.id.videoShootDateTV);
            videoStatus=itemView.findViewById(R.id.videoStatusTV);
            childView=itemView.findViewById(R.id.childView);

        }



        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
