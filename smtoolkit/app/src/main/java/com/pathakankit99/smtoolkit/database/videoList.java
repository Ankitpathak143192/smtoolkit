package com.pathakankit99.smtoolkit.database;

import android.util.Log;

public class videoList {

    public static final String TABLE_NAME = "video";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VIDEO_NUMBER = "video_number";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_VIDEO_PUB_DATE = "publish_date";
    public static final String COLUMN_VIDEO_SHOOT_DATE = "shoot_date";
    public static final String COLUMN_VIDEO_STATUS = "video_status";
    public static final String COLUMN_DATE_CREATED = "dateCreated";


    public static final String CREATE_VIDEO_SCHEDULE__TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " VARCHAR,"
                    + COLUMN_VIDEO_NUMBER + " VARCHAR,"
                    + COLUMN_VIDEO_SHOOT_DATE + " VARCHAR,"
                    + COLUMN_VIDEO_PUB_DATE + " VARCHAR,"
                    + COLUMN_VIDEO_STATUS + " VARCHAR,"
                    + COLUMN_DATE_CREATED + " VARCHAR"
                    + ")";

    private long id;
    private String title;
    private String videoPublishDate;
    private String videoShootDate;
    private String videoStatus;
    private String videoNumber;
    private String dateCreated;

    public videoList() {

    }

    public videoList(long id, String title,String  videoNumber, String videoShootDate,  String videoPublishDate,   String videoStatus, String dateCreated) {
        this.id = id;
        this.title = title;
        this.videoNumber=videoNumber;
        this.videoShootDate=videoShootDate;
        this.videoPublishDate=videoPublishDate;
        this.videoStatus=videoStatus;
        this.dateCreated = dateCreated;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoPublishDate() {
        return videoPublishDate;
    }

    public void setVideoPublishDate(String videoPublishDate) { this.videoPublishDate = videoPublishDate; }

    public String getVideoShootDate() {
        return videoShootDate;
    }

    public void setVideoShootDate(String videoShootDate) {
        this.videoShootDate = videoShootDate;
    }

    public String getVideoNumber() {return videoNumber; }

    public void setVideoNumber(String videoNumber) {this.videoNumber=videoNumber;}

    public String getVideoStatus() {
        return videoStatus;
    }

    public void setVideoStatus(String videoStatus) {
        this.videoStatus= videoStatus;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }



    @Override
    public String toString() {
        Log.d("response ","Title: "+title);
        return super.toString();
    }


}

