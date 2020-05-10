package com.pathakankit99.smtoolkit.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pathakankit99.smtoolkit.MainActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MakeApiCallListener3 {


    static String LOG_TAG = "SMToolkit";
    public static final String SHARED_PREFERENCES_NAME = "AuthStateSaved";

    String subscriberCount, videoCount, viewCount; //Get Month based data
    String currentDate;
    String currentMonth;
    String oneMonthBack, tenMonthBack;


    private final Activity mMainActivity;
    private AuthState mAuthState;
    private AuthorizationService mAuthorizationService;

    public MakeApiCallListener3(@NonNull Activity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
        mMainActivity = mainActivity;
        mAuthState = authState;
        mAuthorizationService = authorizationService;
        currentDate=getDate();
        currentMonth=getMonth();
        oneMonthBack=getOneMonthBackDate("yyyy-MM-dd", -30);
        tenMonthBack=getTenMonthBackDate("yyyy-MM",-300);

        final String MonthURL="https://youtubeanalytics.googleapis.com/v2/reports?dimensions=month&endDate="+currentMonth+"-01&ids=channel%3D%3DMINE&metrics=views%2CestimatedMinutesWatched%2CaverageViewDuration%2CaverageViewPercentage%2Ccomments%2Clikes%2Cdislikes%2Cshares%2CsubscribersGained%2CsubscribersLost&sort=month&startDate="+tenMonthBack+"-01";//date should be YY-MM-01
        final String DayURL="https://youtubeanalytics.googleapis.com/v2/reports?dimensions=day&endDate="+currentDate+"&ids=channel%3D%3DMINE&metrics=views%2CestimatedMinutesWatched%2CaverageViewDuration%2CaverageViewPercentage%2Ccomments%2Clikes%2Cdislikes%2Cshares%2CsubscribersGained%2CsubscribersLost&sort=day&startDate="+oneMonthBack;
        //String DayURL="https://youtubeanalytics.googleapis.com/v2/reports?dimensions=day&endDate=2020-05-01&ids=channel%3D%3DMINE&metrics=views%2CestimatedMinutesWatched%2CaverageViewDuration%2CaverageViewPercentage%2Ccomments%2Clikes%2Cdislikes%2Cshares%2CsubscribersGained%2CsubscribersLost&sort=day&startDate=2019-01-01";//
        String TestURL="https://youtubeanalytics.googleapis.com/v2/reports?dimensions=day&endDate=2020-04-30&ids=channel%3D%3DMINE&metrics=views%2CestimatedMinutesWatched%2CaverageViewDuration%2CaverageViewPercentage%2CsubscribersGained&sort=day&startDate=2020-01-01";





        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
            @Override
            public void execute(
                    String accessToken,
                    String idToken,
                    AuthorizationException ex) {
                if (ex != null) {
                    // negotiation for fresh tokens failed, check ex for more details
                    return;
                }
                Log.d("SMToolkit","VALUE of URL:\n"+DayURL);
                mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                        new AsyncTask<String, Void, String>() {
                            @Override
                            protected String doInBackground(String... tokens) {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(MonthURL)
                                        .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                        .build();

                                try {
                                    Response response = client.newCall(request).execute();
                                    String jsonBody = response.body().string();
                                  //  Log.i(LOG_TAG, String.format("Api respose 2 %s", jsonBody));
                                    return jsonBody;

                                } catch (Exception exception) {
                                    Log.w(LOG_TAG, exception);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                try {

                                    JSONObject obj=new JSONObject(result);
                                    Log.d(LOG_TAG,"Parsing JSON Object");
                                    JSONArray rows= obj.getJSONArray("rows").getJSONArray(9);
                                    JSONArray rows2= obj.getJSONArray("rows").getJSONArray(8);
                                    int length=rows.length();
                                    String[] data= new String[length];
                                    String[] data2=new String[length];


                                    for(int i=0;i<length;i++)
                                    {
                                        data[i]=rows.getString(i);
                                        data2[i]=rows2.getString(i);
                                    }
                                    String MonthlyViews=data[1];
                                    String MonthlyEstimatedMinutesWatched=data[2];
                                    String MonthlyAverageViewDuration=data[3];
                                    String MonthlyAverageViewPercentage=data[4];
                                    String MonthlyComments=data[5];
                                    String MonthlyLikes=data[6];
                                    String MonthlyDislikes=data[7];
                                    String MonthlyShares=data[8];
                                    String MonthlySubscribersGained=data[9];
                                    String MonthlySubscribersLost=data[10];

                                    String lastMonthlyViews=data[1];
                                    String lastMonthlyEstimatedMinutesWatched=data[2];
                                    String lastMonthlyAverageViewDuration=data[3];
                                    String lastMonthlyAverageViewPercentage=data[4];
                                    String lastMonthlyComments=data[5];
                                    String lastMonthlyLikes=data[6];
                                    String lastMonthlyDislikes=data[7];
                                    String lastMonthlyShares=data[8];
                                    String lastMonthlySubscribersGained=data[9];
                                    String lastMonthlySubscribersLost=data[10];

                                    SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(mMainActivity);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("MonthlyViews",MonthlyViews);
                                    editor.putString("MonthlyEstimatedMinutesWatched",MonthlyEstimatedMinutesWatched);
                                    editor.putString("MonthlyAverageViewDuration",MonthlyAverageViewDuration);
                                    editor.putString("MonthlyAverageViewPercentage",MonthlyAverageViewPercentage);
                                    editor.putString("MonthlyComments",MonthlyComments);
                                    editor.putString("MonthlyLikes",MonthlyLikes);
                                    editor.putString("MonthlyDislikes",MonthlyDislikes);
                                    editor.putString("MonthlyShares",MonthlyShares);
                                    editor.putString("MonthlySubscribersGained",MonthlySubscribersGained);
                                    editor.putString("MonthlySubscribersLost",MonthlySubscribersLost);
                                    editor.putString("lastMonthlyViews",lastMonthlyViews);
                                    editor.putString("lastMonthlyEstimatedMinutesWatched",lastMonthlyEstimatedMinutesWatched);
                                    editor.putString("lastMonthlyAverageViewDuration",lastMonthlyAverageViewDuration);
                                    editor.putString("lastMonthlyAverageViewPercentage",lastMonthlyAverageViewPercentage);
                                    editor.putString("lastMonthlyComments",lastMonthlyComments);
                                    editor.putString("lastMonthlyLikes",lastMonthlyLikes);
                                    editor.putString("lastMonthlyDislikes",lastMonthlyDislikes);
                                    editor.putString("lastMonthlyShares",lastMonthlyShares);
                                    editor.putString("lastMonthlySubscribersGained",lastMonthlySubscribersGained);
                                    editor.putString("lastMonthlySubscribersLost",lastMonthlySubscribersLost);
                                    editor.apply();


                                    Log.d(LOG_TAG,"Items Parsed "+rows.toString());


                                } catch (JSONException e) {
                                    Log.d(LOG_TAG,"Parsing Error \n",e);
                                    e.printStackTrace();
                                }
                            }

                        }.execute(accessToken);

                    }
                });
            }
        });

    }

    public String getDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = sdf.format(new Date());
        Log.d(LOG_TAG,"SDF Currnt Date is "+currentDate);
        return currentDate;

    }
    public String getMonth()
    {
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
        currentMonth= sdf2.format(new Date());
        Log.d(LOG_TAG,"SDF Currnt month is "+currentMonth);
        return  currentMonth;
    }

    public String getOneMonthBackDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        oneMonthBack=s.format(new Date(cal.getTimeInMillis()));
        Log.d(LOG_TAG,"1 month before date was "+s.format(new Date(cal.getTimeInMillis())));
        return oneMonthBack;

    }
    public String getTenMonthBackDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        tenMonthBack=s.format(new Date(cal.getTimeInMillis()));
        Log.d(LOG_TAG,"10 Month before date was "+s.format(new Date(cal.getTimeInMillis())));
        return tenMonthBack;

    }


} //Get Monthly stats