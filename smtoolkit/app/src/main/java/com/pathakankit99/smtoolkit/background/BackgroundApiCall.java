package com.pathakankit99.smtoolkit.background;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pathakankit99.smtoolkit.MainActivity;
import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.ui.MakeApiCallListener2;
import com.pathakankit99.smtoolkit.ui.apicall;
import com.pathakankit99.smtoolkit.ui.dashboard.DashboardFragment;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import org.json.JSONException;

import static com.pathakankit99.smtoolkit.ui.dashboard.DashboardFragment.SHARED_PREFERENCES_NAME;

/**
 * Created on : Mar 26, 2019
 * Author     : AndroidWave
 */
public class BackgroundApiCall extends Worker {
    private static final String WORK_RESULT = "work_result";



    AuthState mAuthState;
    public BackgroundApiCall(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @NonNull
    @Override
    public Result doWork() {

        mAuthState = restoreAuthState();
        Log.d("SMToolkit","AuthState inside backgroundAPiCall is "+mAuthState);
        MainActivity mainActivity=new MainActivity();

       final apicall apicall1= new apicall(mainActivity, mAuthState, new AuthorizationService(getApplicationContext()));
      // final MakeApiCallListener2 apicall2= new MakeApiCallListener2(mainActivity, mAuthState, new AuthorizationService(getApplicationContext()));
        Thread background = new Thread() {
            public void run() {
                try {

                    // Thread will sleep for 5 seconds
                    sleep(10 * 1000);
                    // After 5 seconds redirect to another intent

                    int TotalViews = Integer.parseInt(apicall1.viewCount);
                    int TotalSubs = Integer.parseInt(apicall1.subscriberCount);

                     checkGoal("subHG","subFG",TotalSubs);
                    checkGoal("viewHG","viewFG", TotalViews);

                    //Remove activity

                } catch (Exception ignored) {
                }
            }
        };
        // start thread
        background.start();




        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
        return Result.success(outputData);
    }
    private void checkGoal(String tagHG, String tagFG, int currentValue)
    {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String halfgoal = sharedPreferences.getString(tagHG,null);
        String fullgoal=sharedPreferences.getString(tagFG,null);
        if(tagHG.contains("subHG")) {
            String Title = "You Hit a Goal. Your Total sub now is ";
            String content = String.valueOf(currentValue);
            Log.d("SMToolkit","TotalSub is "+ currentValue);
            Log.d("SMToolkit","Showing Notification Now");
            showNotification(124,Title, content);
        }
        if(tagHG.contains("viewHG")) {
            String Title = "You Hit a Goal. Your Total Views now is ";
            String content = String.valueOf(currentValue);
            Log.d("SMToolkit","TotalViews is "+ currentValue);
            Log.d("SMToolkit","Showing Notification Now");
            showNotification(123,Title, content);
        }

        if (currentValue>Integer.parseInt(halfgoal)||currentValue>Integer.parseInt(fullgoal))
        {




        }
    }

    private void showNotification(int id, String task, String desc) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";
        String channelName = "task_name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.drawable.ic_assignment_black_24dp);
        manager.notify(id, builder.build());
    }

    @Nullable
    private AuthState restoreAuthState() {
        final String AUTH_STATE = "AUTH_STATE";
        String jsonString = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        Log.d("SMToolkit","Authstate taken out of sharedpreference "+jsonString);

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