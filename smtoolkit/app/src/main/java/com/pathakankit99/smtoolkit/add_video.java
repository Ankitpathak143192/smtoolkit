package com.pathakankit99.smtoolkit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;
import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.adapter.YTScheduleRecyclerViewAdapter;
import com.pathakankit99.smtoolkit.database.SqliteDBHelper;
import com.pathakankit99.smtoolkit.database.videoList;
import com.pathakankit99.smtoolkit.ui.DataProviderFromActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.pathakankit99.smtoolkit.MainActivity.LOG_TAG;

public class add_video extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {



    private  static Date dateHolder;
    private static final String TAG = "debugCheck,AddVideo";
    private String startDateStr;
    private  View view4;
    private String videoStatus;

    int videonumbertemp;
    private EditText videoNameET;
    private EditText videoPublishDateET;
    private EditText videoNumberET;
    private EditText videoShootDateET;

    private Button createVideoButton;
    private Calendar myCalendar;
    private LottieAnimationView animationView;

    private ArrayList<videoList> videos;
    private SqliteDBHelper sqliteDBHelper;

    int totalVideosScheduled=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"Inside addVideo class and onCreate function");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create New Video Schedule");

        videoNameET = findViewById(R.id.vidNameET);
        videoPublishDateET = findViewById(R.id.vidPublishDateET);
        videoNumberET = findViewById(R.id.vidNumberET);
        videoShootDateET=findViewById(R.id.vidShootDateET);

        createVideoButton = findViewById(R.id.createVideoButton);
        view4=findViewById(R.id.linearlayoutRoot2);

        createVideoButton.setOnClickListener(this);
        videoPublishDateET.setOnClickListener(this);
        videoShootDateET.setOnClickListener(this);



        Spinner videoStatus = findViewById(R.id.vidStatusSpinner);
        videoStatus.setOnItemSelectedListener(this);
        initialise();

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.videoStatusOptions, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        videoStatus.setAdapter(adapter);


    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == videoPublishDateET ) {
            String dateTag="videoPublishDate";
            getDate(dateTag);
            Log.d("debugCheck","vidPublish button clicked");
        }
        if (v==videoShootDateET)
        {
            String dateTag="videoShootDate";
            getDate(dateTag);
            Log.d("debugCheck","vidShoot button clicked");
        }

        if (v == createVideoButton) {


            //getting text from edit texts
            String title = videoNameET.getText().toString().trim();
            String videoPublishDate = videoPublishDateET.getText().toString().trim();
            String videoShootDate = videoShootDateET.getText().toString().trim();
            String videoNumber = String.valueOf(videonumbertemp);




            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(videoNumber) && !TextUtils.isEmpty(videoPublishDate) && !TextUtils.isEmpty(videoShootDate) && !TextUtils.isEmpty(videoStatus)) {
                long temp_id = 1;
                videoList videos = new videoList(temp_id, title,videoNumber, videoShootDate, videoPublishDate, videoStatus,Calendar.getInstance().getTime().toString());


                SqliteDBHelper databaseHelper = new SqliteDBHelper(getApplicationContext());
                databaseHelper.createVideos(videos);

                Log.d(TAG,"Title: "+title);
                Log.d(TAG,"video number: "+videoNumber);
                Log.d(TAG,"video publish date: "+videoPublishDate);
                Log.d(TAG,"video shoot date: "+videoShootDate);
                Log.d(TAG,"video status: "+videoStatus);

                Snackbar snackbar = Snackbar
                        .make(view4, "Video Schedule Created Successfully. Total Videos "+databaseHelper.getAllvideos().size(), Snackbar.LENGTH_LONG);
                snackbar.show();
                initialise();

            } else {
                Snackbar snackbar = Snackbar
                        .make(view4, "Please Fill all Fields to continue ", Snackbar.LENGTH_LONG);
                snackbar.show();

            }
        }
    }

    private void loadvideos() {
        Log.d(LOG_TAG,"Inside loadvideos function");
        sqliteDBHelper = new SqliteDBHelper(this);

        // Fetch all events and save in videos ArrayList
        videos = sqliteDBHelper.getAllvideos();
        totalVideosScheduled = videos.size();

    }

    public  void initialise()
    {
        loadvideos();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        videonumbertemp = Integer.parseInt(sharedPreferences.getString("TotalVideos", null))+(totalVideosScheduled+1);
        videoNumberET.setText(String.valueOf(videonumbertemp));
        videoNameET.setText(" ");
        videoPublishDateET.setText(" ");
        videoShootDateET.setText(" ");

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        videoStatus = (String) parent.getItemAtPosition(pos);
        Log.d("debugCheck","Option= "+videoStatus);

    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }



    private void getDate(final String dateTag) {
        myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                try {
                    dateHolder = sdf.parse(sdf.format(myCalendar.getTime()));
                    startDateStr = sdf.format(myCalendar.getTime());
                    getTime(dateTag);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        };
        new DatePickerDialog(add_video.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }



    private void getTime(final String dateTag) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(add_video.this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                boolean isPM = (hourOfDay >= 12);
                String time = " " + String.format("%02d:%02d:00 %s",
                        (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                if(dateTag.contains("videoPublishDate")) {
                    videoPublishDateET.setText(startDateStr.concat(time));
                }

                if (dateTag.contains("videoShootDate"))
                {  videoShootDateET.setText(startDateStr.concat(time));}
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
