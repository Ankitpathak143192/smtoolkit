
package com.pathakankit99.smtoolkit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.api.services.youtube.YouTubeScopes;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {
    public static String LOG_TAG = "SMToolkit";
    public static final String SHARED_PREFERENCES_NAME = "AuthStateSaved";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";

    String TotalSubscriberCount, TotalVideoCount, TotalViewCount; //Total
    // state
    AuthState mAuthState;
    Button login;
    Button logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.log_in);
        logout = findViewById(R.id.log_out);

        createNotificationChannel();

        enablePostAuthorizationFlows();

        // wire click listeners
        login.setOnClickListener(new AuthorizeListener());
        mAuthState = restoreAuthState();
        if (mAuthState != null && mAuthState.isAuthorized()) {
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);

        }
        else
        {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
        }
        ReadFile();



    }
    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("SMToolkit", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void WriteFile() {

        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString("Subscribers", TotalSubscriberCount)
                .putString("Views",TotalViewCount)
                .putString("Videos",TotalVideoCount)
                .apply();

    }

    public void ReadFile() {

        TotalSubscriberCount = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString("TotalSubscribers", null);
        TotalViewCount = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString("TotalViews", null);
        TotalVideoCount = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString("TotalVideos", null);

    }


    private void enablePostAuthorizationFlows() {
        mAuthState = restoreAuthState();
        if (mAuthState != null && mAuthState.isAuthorized()) {

            if (logout.getVisibility() == View.GONE) {
                logout.setVisibility(View.VISIBLE);
                logout.setOnClickListener(new SignOutListener(this));
                Thread background = new Thread() {
                    public void run() {
                        try {
                            // Thread will sleep for 5 seconds
                            sleep(3 * 1000);

                            // After 5 seconds redirect to another intent
                            Intent i = new Intent(getBaseContext(), app_nav.class);
                            startActivity(i);

                            //Remove activity
                            finish();
                        } catch (Exception ignored) {
                        }
                    }
                };
                // start thread
                background.start();
            }
        } else {
            login.setVisibility(View.VISIBLE);

            logout.setVisibility(View.GONE);
        }
    }

    private void handleAuthorizationResponse(@NonNull Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);

        if (response != null) {
            Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.w(LOG_TAG, "Token Exchange failed", exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);
                            Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                        }
                    }
                }
            });
        }

        // code from the step 'Handle the Authorization Response' goes here.

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.pathakankit99.smtoolkit.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }

    private void persistAuthState(@NonNull AuthState authState) {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .apply();
        enablePostAuthorizationFlows();
    }

    private void clearAuthState() {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

    @Nullable
    private AuthState restoreAuthState() {
        String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
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

    /**
     * Kicks off the authorization flow.
     */
    public static class AuthorizeListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {

            AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                    Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
            );

           // String clientId = "197783100258-0f3788lu5u9crbgthketqtqtvkbfurui.apps.googleusercontent.com";
            String clientId="197783100258-blsqj3e7tsq40uj5irpjouskfg647r7p.apps.googleusercontent.com";
            Uri redirectUri = Uri.parse("com.pathakankit99.smtoolkit:/oauth2callback");
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                    serviceConfiguration,
                    clientId,
                    AuthorizationRequest.RESPONSE_TYPE_CODE,
                    redirectUri
            );

            builder.setScope(YouTubeScopes.YOUTUBE_READONLY);
            AuthorizationRequest request = builder.build();
            AuthorizationService authorizationService = new AuthorizationService(view.getContext());

            // code from the step 'Create the Authorization Request',
            // and the step 'Perform the Authorization Request' goes here.
            String action = "com.pathakankit99.smtoolkit.HANDLE_AUTHORIZATION_RESPONSE";
            Intent postAuthorizationIntent = new Intent(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
            authorizationService.performAuthorizationRequest(request, pendingIntent);

        }
    }

    public static class SignOutListener implements Button.OnClickListener {

        private final MainActivity mMainActivity;

        public SignOutListener(@NonNull MainActivity mainActivity) {
            mMainActivity = mainActivity;
        }

        @Override
        public void onClick(View view) {
            mMainActivity.mAuthState = null;
            mMainActivity.clearAuthState();
            mMainActivity.enablePostAuthorizationFlows();
        }
    }

   /* public class MakeApiCallListener {
        String subscriberCount, videoCount, viewCount; //Total

        private final MainActivity mMainActivity;
        private AuthState mAuthState;
        private AuthorizationService mAuthorizationService;

        public MakeApiCallListener(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
            mMainActivity = mainActivity;
            mAuthState = authState;
            mAuthorizationService = authorizationService;


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
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @SuppressLint("StaticFieldLeak")
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                            new AsyncTask<String, Void, String>() {
                                @Override
                                protected String doInBackground(String... tokens) {
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .url("https://www.googleapis.com/youtube/v3/channels?part=snippet%2CcontentDetails%2Cstatistics&mine=true")
                                            .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                            .build();

                                    try {
                                        Response response = client.newCall(request).execute();
                                        String jsonBody = response.body().string();
                                        Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
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
                                            JSONObject items=obj.getJSONArray("items").getJSONObject(0);
                                            Log.d(LOG_TAG,"Items Parsed");
                                            JSONObject statistics=items.getJSONObject("statistics");
                                            viewCount = statistics.getString("viewCount");
                                            subscriberCount = statistics.getString("subscriberCount");
                                            videoCount = statistics.getString("videoCount");
                                            Log.d(LOG_TAG,"Views= "+viewCount);
                                            Log.d(LOG_TAG,"Subscribers= "+subscriberCount);
                                            Log.d(LOG_TAG,"Videos= "+videoCount);

                                            getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                                                    .putString("TotalSubscribers", subscriberCount)
                                                    .putString("TotalViews",viewCount)
                                                    .putString("TotalVideos",videoCount)
                                                    .apply();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                }

                            }.execute(accessToken);

                        }
                    });
                }
            });

        }


    } //Get Total subscribers*/
  /* public static class MakeApiCallListener2  {
        String subscriberCount, videoCount, viewCount; //Get Month based data
        String currentDate;
        String currentMonth;
        String oneMonthBack, tenMonthBack;


        private final MainActivity mMainActivity;
        private AuthState mAuthState;
        private AuthorizationService mAuthorizationService;

        public MakeApiCallListener2(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
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
                                            .url(DayURL)
                                            .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                            .build();

                                    try {
                                        Response response = client.newCall(request).execute();
                                        String jsonBody = response.body().string();
                                        Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
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
                                        JSONObject items=obj.getJSONArray("items").getJSONObject(0);
                                        Log.d(LOG_TAG,"Items Parsed");
                                        JSONObject statistics=items.getJSONObject("statistics");
                                        viewCount = statistics.getString("viewCount");
                                        subscriberCount = statistics.getString("subscriberCount");
                                        videoCount = statistics.getString("videoCount");
                                        Log.d(LOG_TAG,"Views= "+viewCount);
                                        Log.d(LOG_TAG,"Subscribers= "+subscriberCount);
                                        Log.d(LOG_TAG,"Videos= "+videoCount);
                                    } catch (JSONException e) {
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


    } //Get daily stats  */
   /* public static class MakeApiCallListener3 {
        String subscriberCount, videoCount, viewCount; //Get Month based data
        String currentDate;
        String currentMonth;
        String oneMonthBack, tenMonthBack;


        private final MainActivity mMainActivity;
        private AuthState mAuthState;
        private AuthorizationService mAuthorizationService;

        public MakeApiCallListener3(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
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
                                        Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
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
                                        JSONObject items=obj.getJSONArray("items").getJSONObject(0);
                                        Log.d(LOG_TAG,"Items Parsed");
                                        JSONObject statistics=items.getJSONObject("statistics");
                                        viewCount = statistics.getString("viewCount");
                                        subscriberCount = statistics.getString("subscriberCount");
                                        videoCount = statistics.getString("videoCount");
                                        Log.d(LOG_TAG,"Views= "+viewCount);
                                        Log.d(LOG_TAG,"Subscribers= "+subscriberCount);
                                        Log.d(LOG_TAG,"Videos= "+videoCount);
                                    } catch (JSONException e) {
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


    } //Get Monthly stats */


}
