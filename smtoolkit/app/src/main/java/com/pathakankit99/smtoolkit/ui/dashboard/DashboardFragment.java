package com.pathakankit99.smtoolkit.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.app_nav;
import com.pathakankit99.smtoolkit.database.SqliteDBHelper;
import com.pathakankit99.smtoolkit.database.videoList;
import com.pathakankit99.smtoolkit.ui.DataProviderFromActivity;
import com.pathakankit99.smtoolkit.ui.MakeApiCallListener;
import com.pathakankit99.smtoolkit.ui.MakeApiCallListener2;
import com.pathakankit99.smtoolkit.ui.MakeApiCallListener3;
import com.pathakankit99.smtoolkit.ui.apicall;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class DashboardFragment extends Fragment {
    static String LOG_TAG = "SMToolkit";
    public static final String SHARED_PREFERENCES_NAME = "AuthStateSaved";
    private static final String AUTH_STATE = "AUTH_STATE";
    AuthState mAuthState;
    View root;
    String LastUpdate;
    TextView viewsTV,subsTV,lastUpdate,unPublishdVideosTV,TotalVideosTV,monthlyEngagementTV, monthlyMinutesWatchedTV;
    TextView subGoalTV,viewGoalTV,videoGoalTV,engagementGoalTV,monthlyMinutesWatchedGoalTV;
    String TotalSub, TotalViews, TotalVideos,monthlyMinutesWatched, monthlyEngagement;
    int intTotalSub, intTotalViews, intTotalVideos,intmonthlyMinutesWatched, intmonthlyEngagement;
    private ArrayList<videoList> videos;
    private SqliteDBHelper sqliteDBHelper;
    public int totalVideosScheduled=0;
    ProgressBar progressBar;


        public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

            root = inflater.inflate(R.layout.fragment_dashboard, container, false);
            sqliteDBHelper = new SqliteDBHelper(getActivity());


            viewsTV = root.findViewById(R.id.viewsDashboardTV);
            subsTV = root.findViewById(R.id.subsDashboardTV);
            lastUpdate=root.findViewById(R.id.lastUpdated);
            unPublishdVideosTV=root.findViewById(R.id.unPublishedVideos);
            TotalVideosTV=root.findViewById(R.id.TotalVideosTV);
            progressBar=root.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
            monthlyMinutesWatchedTV=root.findViewById(R.id.monthlyMinutesWatchedTV);
            monthlyEngagementTV=root.findViewById(R.id.monthlyEngagementTV);


            engagementGoalTV=root.findViewById(R.id.engagementGoal);
            monthlyMinutesWatchedGoalTV=root.findViewById(R.id.monthlyMinutesWatchedGoal);
            subGoalTV=root.findViewById(R.id.subGoal);
            viewGoalTV=root.findViewById(R.id.viewGoal);
            videoGoalTV=root.findViewById(R.id.videoGoals);
            progressBar.setVisibility(View.VISIBLE);
            mAuthState = restoreAuthState();

             new MakeApiCallListener(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));
             new MakeApiCallListener2(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));
              new MakeApiCallListener3(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));



            Thread background = new Thread() {
                public void run() {
                    try {

                        // Thread will sleep for 5 seconds
                        sleep(5 * 1000);
                        // After 5 seconds redirect to another intent
                        data();
                        Output();
                        goals();
                        progressBar.setVisibility(View.INVISIBLE);

                        //Remove activity

                    } catch (Exception ignored) {
                    }
                }
            };
            // start thread
            background.start();
            data();
            Output();
            loadvideos();
            goals();

            Log.d(LOG_TAG,"Calling First API: Value of Authstate  "+mAuthState);
            FloatingActionButton fab = root.findViewById(R.id.fabDashboard);
            fab.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    new MakeApiCallListener(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));
                    new MakeApiCallListener2(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));
                    new MakeApiCallListener3(requireActivity(), mAuthState, new AuthorizationService(requireActivity()));

                    Thread background = new Thread() {
                        public void run() {
                            try {

                                // Thread will sleep for 5 seconds
                                sleep(5 * 1000);
                                // After 5 seconds redirect to another intent
                                data();
                                Output();
                                goals();


                                progressBar.setVisibility(View.INVISIBLE);

                                //Remove activity

                            } catch (Exception ignored) {
                            }
                        }
                    };
                    // start thread
                    background.start();

                }
            });
            return root;
    }
    public  void onResume() {
        super.onResume();
        data();
        Output();


    }

    public void notification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),"SMToolkit")
                .setSmallIcon(R.drawable.ic_assignment_black_24dp)
                .setContentTitle("Title")
                .setContentText("Content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(1234, builder.build());
    }




    private void loadvideos() {


        // Fetch all events and save in videos ArrayList
        videos = sqliteDBHelper.getAllNotPublishedVideos();
        totalVideosScheduled = videos.size();
        if (totalVideosScheduled==0)
        {
            unPublishdVideosTV.setText("All Videos Published");
        }
        else if(totalVideosScheduled==1)
        {
            unPublishdVideosTV.setText(totalVideosScheduled+" Video Unpublished");
        }
        else
        {
            unPublishdVideosTV.setText(totalVideosScheduled+" Videos Unpublished");
        }

    }
    public  void data()
    {
        DataProviderFromActivity myActivity= (DataProviderFromActivity) getActivity();
        TotalSub  = myActivity.getTotalSubscribers();
        TotalViews = myActivity.getTotalViews();
        TotalVideos=myActivity.getTotalVideos();
        LastUpdate=myActivity.getLastUpdate();
        monthlyEngagement=myActivity.getMonthlyEngagement();
        monthlyMinutesWatched=myActivity.getMonthlyMinutesWatched();
    }


    private void Output()
    {
        viewsTV.setText(TotalViews);
        subsTV.setText(TotalSub);
        lastUpdate.setText("Last Update on "+LastUpdate);
        TotalVideosTV.setText(TotalVideos+" Videos Published");
        monthlyEngagementTV.setText(monthlyEngagement+" Audience Engagements");
        monthlyMinutesWatchedTV.setText(monthlyMinutesWatched);
    }

    void goals()
    {
        String upcoming_goal="Upcoming Goal ";

       subGoalTV.setText( upcoming_goal +goalCreate(TotalSub,"subHG","subFG"));
       videoGoalTV.setText(upcoming_goal+goalCreate(TotalVideos,"videoHG","videoFG"));
       viewGoalTV.setText(upcoming_goal+goalCreate(TotalViews,"viewHG","viewFG"));
       monthlyMinutesWatchedGoalTV.setText(upcoming_goal+goalCreate(monthlyMinutesWatched,"minuteswatchedHG","minuteswatchedFG"));
       engagementGoalTV.setText(upcoming_goal+goalCreate(monthlyEngagement,"engagementHG","engagementFG"));


    }

    int goalCreate(String value, String tagHG, String tagFG)
    { int k=0,goal=0,fullgoal=0,halfgoal=0,currentValue=Integer.parseInt(value);
      int[] a =new int[value.length()];
        for (int i=0;i<value.length();i++)
        {
             a[i]= Integer.parseInt(String.valueOf(value.charAt(i)));
            Log.d(LOG_TAG, Arrays.toString(a));
            k=k+1;
        }

           fullgoal= (int) ((a[0]+1)*Math.pow(10,k-1));
           Log.d(LOG_TAG,"FullGoal= "+fullgoal);
           halfgoal= (int) ((a[0])*Math.pow(10,k-1)+(5*Math.pow(10,k-2)));
           Log.d(LOG_TAG,"Half goal= "+halfgoal);
           Log.d(LOG_TAG,"CurrentValue= "+currentValue);
           if (currentValue>halfgoal)
               goal=fullgoal;
           else
               goal=halfgoal;

           saveGoal(tagHG,tagFG,halfgoal,fullgoal);

        Log.d(LOG_TAG,"Goal is "+ goal);
        return goal;
    }

    private void saveGoal(String tagHG, String tagFG, int halfgoal, int fullgoal)
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(tagHG, String.valueOf(halfgoal));
        editor.putString(tagFG, String.valueOf(fullgoal));
        editor.apply();

    }
    private void checkGoal(String tagHG, String tagFG, int currentValue)
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
         String halfgoal = sharedPreferences.getString(tagHG,null);
         String fullgoal=sharedPreferences.getString(tagFG,null);

       if (currentValue>Integer.parseInt(halfgoal)||currentValue>Integer.parseInt(fullgoal))
       {
           //notification
       }
    }

    @Nullable
    private AuthState restoreAuthState() {

        String jsonString = this.requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return AuthState.fromJson(jsonString);
            } catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

}
