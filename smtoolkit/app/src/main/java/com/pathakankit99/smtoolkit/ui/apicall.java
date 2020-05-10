package com.pathakankit99.smtoolkit.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pathakankit99.smtoolkit.MainActivity;
import com.pathakankit99.smtoolkit.app_nav;
import com.pathakankit99.smtoolkit.background.BackgroundApiCall;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class apicall extends  MainActivity {
    static String LOG_TAG = "SMToolkit";
    public static final String SHARED_PREFERENCES_NAME = "AuthStateSaved";
    public String subscriberCount;
    public String videoCount;
    public String viewCount; //Total

    private final MainActivity mMainActivity;
    private AuthState mAuthState;
    private AuthorizationService mAuthorizationService;

    public apicall(@NonNull final MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
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

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat sdf2 = new SimpleDateFormat("hh-mm aa");
                                    String updateDate= sdf.format(new Date());
                                    String updateTime = sdf2.format(new Date());
                                    Log.d(LOG_TAG,"updateDate= "+updateDate+"   "+updateTime);
                                   // Log.d(LOG_TAG,"Context is "+mMainActivity.getApplicationContext());


                                 // SharedPreferences mySharedPreferences = mMainActivity.getParent().getSharedPreferences("preferences", Activity.MODE_PRIVATE);
                                   // DataProviderFromActivity myActivity= (DataProviderFromActivity) mMainActivity.getApplicationContext();

                                  /*  File file = new File(mMainActivity.getApplicationContext().getFilesDir(), "Notice");
                                    if (!file.exists()) {
                                        file.mkdir();
                                    }
                                    try {
                                        File gpxfile = new File(file, "sample");
                                        FileWriter writer = new FileWriter(gpxfile);
                                        writer.append(viewCount);
                                        writer.flush();
                                        writer.close();
                                       // Toast.makeText(mMainActivity, "Saved your text", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) { }


                                  /*  SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(mMainActivity);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("TotalSubscribers", subscriberCount);
                                    editor.putString("TotalViews",viewCount);
                                    editor.putString("TotalVideos",videoCount);
                                    editor.putString("UpdateDate",updateDate);
                                    editor.putString("UpdateTime",updateTime);
                                    editor.apply(); */



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



} //Get Total subscribers