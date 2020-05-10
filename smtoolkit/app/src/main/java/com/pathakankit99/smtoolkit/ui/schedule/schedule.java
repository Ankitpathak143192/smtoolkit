package com.pathakankit99.smtoolkit.ui.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pathakankit99.smtoolkit.MainActivity;
import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.adapter.SwipeToDeleteCallback;
import com.pathakankit99.smtoolkit.adapter.SwipeToUpdateCallback;
import com.pathakankit99.smtoolkit.adapter.YTScheduleRecyclerViewAdapter;
import com.pathakankit99.smtoolkit.add_video;
import com.pathakankit99.smtoolkit.app_nav;
import com.pathakankit99.smtoolkit.database.SqliteDBHelper;
import com.pathakankit99.smtoolkit.database.videoList;
import com.pathakankit99.smtoolkit.update_video;

import java.util.ArrayList;

import static com.pathakankit99.smtoolkit.MainActivity.LOG_TAG;

public class schedule extends Fragment implements YTScheduleRecyclerViewAdapter.ItemClickListener {
    private static final String TAG = "debugCheck,YTSchedule";
    private View view2;
    private YTScheduleRecyclerViewAdapter adapter, adapter2;
    private ArrayList<videoList> videos;
    private SqliteDBHelper sqliteDBHelper;
    private LottieAnimationView animationView2;
    private int totalVideosScheduled = 0;
    private int totalVideosScheduled2 = 0;
    private RecyclerView recyclerView;
    private Switch switch1;
    boolean state;
    String state2;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);

        sqliteDBHelper = new SqliteDBHelper(getActivity());
        FloatingActionButton fab = root.findViewById(R.id.fab);
        switch1=root.findViewById(R.id.filter);


        switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state=switch1.isChecked();
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("FilterState", String.valueOf(state));
                editor.apply();

                Log.d(LOG_TAG,"After Click State is "+state);

                if(state==false)
                {
                    loadvideos();
                }
                if(state==true)
                {
                    loadvideos2();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), add_video.class);
                startActivity(intent);

            }
        });
        view2 = root.findViewById(R.id.linearlayoutRoot);
        recyclerView = root.findViewById(R.id.videoListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        checkState();

        enableSwipeToDeleteAndUndo();
        enableSwipeToUpdate();
        return root;
    }
    @Override
    public void onResume() {
        Log.d(TAG,"Inside onResume function");

        // animationView2.setVisibility(View.INVISIBLE);
        //update events home
        videos.clear();
        checkState();

        super.onResume();
    }
    private void loadvideos() {
        Log.d(LOG_TAG,"Inside loadvideos function");

        // Fetch all events and save in videos ArrayList
        videos = sqliteDBHelper.getAllvideos();
        totalVideosScheduled = videos.size();

        Log.d(LOG_TAG, "Total videos: " + totalVideosScheduled);
        if (totalVideosScheduled == 0) {
            // animationView2.setVisibility(View.VISIBLE);
            //animationView2.playAnimation();
            Snackbar snackbar = Snackbar
                    .make(view2, "No Video Schedule found.", Snackbar.LENGTH_LONG);
            snackbar.show();

        }
        adapter = new YTScheduleRecyclerViewAdapter(getActivity(), videos);
        recyclerView.setAdapter(adapter);
    }
    private void loadvideos2() {

        Log.d(LOG_TAG,"Inside loadvideos2 function");

        // Fetch all events and save in videos ArrayList
        videos = sqliteDBHelper.getAllNotPublishedVideos();
        totalVideosScheduled2 = videos.size();

        Log.d(LOG_TAG, "Total videos2: " + totalVideosScheduled2);
        if (totalVideosScheduled2 == 0) {
            // animationView2.setVisibility(View.VISIBLE);
            //animationView2.playAnimation();
            Snackbar snackbar = Snackbar
                    .make(view2, "No Video Schedule found.", Snackbar.LENGTH_LONG);
            snackbar.show();

        }
        adapter = new YTScheduleRecyclerViewAdapter(getActivity(), videos);
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter();
    }
    private  void checkState()
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        state2 = sharedPreferences.getString("FilterState", null);


        state=Boolean.parseBoolean(state2);

        Log.d(LOG_TAG,"Before Click State2 is "+state2);
        Log.d(LOG_TAG,"Before Click State is "+state);
        if (state==true)
        {

            loadvideos2();
            switch1.setChecked(true);

        }

        else
        {
            loadvideos();
            switch1.setChecked(false);
        }


    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getActivity()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final videoList item = adapter.getData().get(position);
                Snackbar snackbar = Snackbar
                        .make(view2, "Video Schedule Deleted Successfully", Snackbar.LENGTH_SHORT);
                snackbar.show();
                sqliteDBHelper.deleteYTSchedule(item);
                videos.remove(position);
                adapter.notifyDataSetChanged();
                onResume();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    private void enableSwipeToUpdate() {
        SwipeToUpdateCallback swipeToUpdateCallback = new SwipeToUpdateCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final videoList item = adapter.getData().get(position);
                Intent intent = new Intent(getActivity(), update_video.class);
                intent.putExtra("id", item.getId());
                startActivity(intent);


            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToUpdateCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
