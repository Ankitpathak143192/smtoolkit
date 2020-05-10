package com.pathakankit99.smtoolkit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pathakankit99.smtoolkit.background.BackgroundApiCall;
import com.pathakankit99.smtoolkit.ui.DataProviderFromActivity;

import java.util.concurrent.TimeUnit;

import static com.pathakankit99.smtoolkit.MainActivity.LOG_TAG;

public class app_nav extends AppCompatActivity implements DataProviderFromActivity {
    private static final String CHANNEL_ID ="Channel ID" ;
    String TotalSubCount, TotalViewCount, TotalVideosCount, UpdateDate, UpdateTime, LastUpdate;
    String monthlyEngagement,monthlyMinutesWatched,monthlyLikes,monthlyShares,monthlyComments,monthlyDislikes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_nav);
        readData();

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_videoSchedule, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }


    public  void  onDestroy() {

        super.onDestroy();
       final WorkManager mWorkManager = WorkManager.getInstance();
        //final OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(BackgroundApiCall.class).build();

       //         mWorkManager.enqueue(mRequest);
        PeriodicWorkRequest.Builder apicall =
                new PeriodicWorkRequest.Builder(BackgroundApiCall.class, 15, TimeUnit.MINUTES);
        PeriodicWorkRequest request = apicall.build();
        mWorkManager.enqueue(request);
        mWorkManager.getWorkInfoByIdLiveData(request.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    WorkInfo.State state = workInfo.getState();
                   Log.d(LOG_TAG,"State is "+state);
                }
            }
        });

    }


    public void readData() {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        UpdateDate = sharedPreferences.getString("UpdateDate", null);
        UpdateTime = sharedPreferences.getString("UpdateTime", null);
        TotalSubCount=sharedPreferences.getString("TotalSubscribers", null);
        TotalViewCount=sharedPreferences.getString("TotalViews", null);
        TotalVideosCount=sharedPreferences.getString("TotalVideos", null);
        monthlyMinutesWatched=sharedPreferences.getString("MonthlyEstimatedMinutesWatched",null);
        monthlyComments=sharedPreferences.getString("MonthlyComments",null);
        monthlyDislikes=sharedPreferences.getString("MonthlyDislikes",null);
        monthlyLikes=sharedPreferences.getString("MonthlyLikes",null);
        monthlyShares=sharedPreferences.getString("MonthlyShares",null);


       int monthlyEngagementTemp=Integer.parseInt(monthlyComments)+Integer.parseInt(monthlyLikes)+Integer.parseInt(monthlyDislikes)+Integer.parseInt(monthlyShares);
       monthlyEngagement= String.valueOf(monthlyEngagementTemp);


    }

    @Override
    public String getTotalSubscribers()
    {
        readData();
        return TotalSubCount;
    }

    @Override
    public String getTotalViews() { return TotalViewCount;}

    @Override
    public String getTotalVideos() {return TotalVideosCount;
    }


    @Override
    public String getLastUpdate() {
        LastUpdate= UpdateDate+"   "+UpdateTime;
    Log.d(LOG_TAG,"LastUpdate= "+LastUpdate);
        return LastUpdate;
    }

    @Override
    public String getMonthlyEngagement() {
        return monthlyEngagement;
    }

    @Override
    public String getMonthlyMinutesWatched() {
        return monthlyMinutesWatched;
    }
}
