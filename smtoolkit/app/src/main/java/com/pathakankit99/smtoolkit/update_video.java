package com.pathakankit99.smtoolkit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

import com.google.android.material.snackbar.Snackbar;
import com.pathakankit99.smtoolkit.R;
import com.pathakankit99.smtoolkit.database.SqliteDBHelper;
import com.pathakankit99.smtoolkit.database.videoList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class update_video extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "debugCheck updateVideo ";
    private static Date videoDate;
    private String startDateStr;

    private EditText videoNameET;
    private EditText videoPublishDateET;
    private EditText videoShootDateET;
    private EditText videoNumberET;

    private Button createVideoButton;
    private Calendar myCalendar;
    private videoList videos;
    private  View view3;
    private SqliteDBHelper sqliteDBHelper;


    String videoStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sqliteDBHelper = new SqliteDBHelper(this);

        videos = sqliteDBHelper.getVideosByID(getIntent().getExtras().getLong("id"));
        getSupportActionBar().setTitle("Update Video Schedule");


        videoNameET = findViewById(R.id.vidNameET);
        videoNameET.setText(videos.getTitle());
        Log.d(TAG,"vidNameET contains "+videoNameET);
        videoPublishDateET = findViewById(R.id.vidPublishDateET);
        videoPublishDateET.setText(videos.getVideoPublishDate());
        Log.d(TAG,"vidPublishDateET contains "+videoPublishDateET);
        videoShootDateET = findViewById(R.id.vidShootDateET);
        videoShootDateET.setText(videos.getVideoShootDate());
        Log.d(TAG,"vidShootDateET contains "+videoShootDateET);
        videoNumberET = findViewById(R.id.vidNumberET);
        videoNumberET.setText(videos.getVideoNumber());
        Log.d(TAG,"vidNumberET contains "+videoNumberET);

        createVideoButton = findViewById(R.id.createVideoButton);
        //animationView=findViewById(R.id.animationView);
        view3=findViewById(R.id.linearlayoutRoot2);

        createVideoButton.setOnClickListener(this);
        videoPublishDateET.setOnClickListener(this);
        videoShootDateET.setOnClickListener(this);
        createVideoButton.setText("Save");
        Spinner videoStatusSpinner = findViewById(R.id.vidStatusSpinner);
        videoStatusSpinner.setPrompt(videos.getVideoStatus());
        videoStatusSpinner.setOnItemSelectedListener(this);


// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.videoStatusOptions, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        videoStatusSpinner.setAdapter(adapter);


    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == videoPublishDateET) {
            String dateTag="videoPublishDate";
            getDate(dateTag);
        }
        if (v==videoShootDateET)
        { String dateTag="videoShootDate";
            getDate(dateTag);
        }

        if (v == createVideoButton) {
            //getting text from edit texts
            String title = videoNameET.getText().toString().trim();
            String videoPublishDate = videoPublishDateET.getText().toString().trim();
            String videoNumber = videoNumberET.getText().toString().trim();
            String videoShootDate=videoShootDateET.getText().toString().trim();

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(videoPublishDate) && !TextUtils.isEmpty(videoNumber) && !TextUtils.isEmpty(videoShootDate)&& !TextUtils.isEmpty(videoStatus)) {
                long temp_id = videos.getId();

                videoList videos = new videoList(temp_id, title,videoNumber, videoShootDate, videoPublishDate, videoStatus,Calendar.getInstance().getTime().toString());
                sqliteDBHelper.updateYTSchedule(videos);

                Snackbar snackbar = Snackbar
                        .make(view3, "Video Schedule updated successfully", Snackbar.LENGTH_LONG);
                snackbar.show();


            } else { Snackbar snackbar = Snackbar
                    .make(view3, "Fill All Fields To Continue", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
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
                    videoDate = sdf.parse(sdf.format(myCalendar.getTime()));
                    startDateStr = sdf.format(myCalendar.getTime());
                    getTime(dateTag);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        };
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }



    private void getTime(final String dateTag) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                boolean isPM = (hourOfDay >= 12);
                String time = " " + String.format("%02d:%02d:00 %s",
                        (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                if (dateTag.contains("videoPublishDate"))
                    videoPublishDateET.setText(startDateStr.concat(time));
                if (dateTag.contains("videoShootDate"))
                    videoShootDateET.setText(startDateStr.concat(time));
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

}
