package com.pathakankit99.smtoolkit.database;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;

public class SqliteDBHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "yt_schedule_db";


    public SqliteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(videoList.CREATE_VIDEO_SCHEDULE__TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table
        db.execSQL("DROP TABLE IF EXISTS " + videoList.TABLE_NAME);

        // Recreate table
        onCreate(db);
    }

    /**
     * Insert event long.
     * @param videos the videos
     * @return the long created event id
     */
    public Long createVideos(videoList videos) {

        //  writable database instance
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(videoList.COLUMN_TITLE, videos.getTitle());
        values.put(videoList.COLUMN_VIDEO_NUMBER, videos.getVideoNumber());
        values.put(videoList.COLUMN_VIDEO_SHOOT_DATE, videos.getVideoShootDate());
        values.put(videoList.COLUMN_VIDEO_PUB_DATE, videos.getVideoPublishDate());
        values.put(videoList.COLUMN_VIDEO_STATUS, videos.getVideoStatus());
        values.put(videoList.COLUMN_DATE_CREATED, videos.getDateCreated());

        //saving data
        long id = db.insert(videoList.TABLE_NAME, null, values);

        // close db connection
        db.close();

        return id;
    }

    public int updateYTSchedule(videoList videos) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(videoList.COLUMN_TITLE, videos.getTitle());
        values.put(videoList.COLUMN_VIDEO_NUMBER, videos.getVideoNumber());
        values.put(videoList.COLUMN_VIDEO_SHOOT_DATE, videos.getVideoShootDate());
        values.put(videoList.COLUMN_VIDEO_PUB_DATE, videos.getVideoPublishDate());
        values.put(videoList.COLUMN_VIDEO_STATUS, videos.getVideoStatus());
        values.put(videoList.COLUMN_DATE_CREATED,videos.getDateCreated());
        // updating event row

        return db.update(videoList.TABLE_NAME, values, videoList.COLUMN_ID + " = ?",
                new String[]{String.valueOf(videos.getId())});
    }

    /**
     * Gets event by id.
     * @param id the id
     * @return the videos by id
     */
    public videoList getVideosByID(long id) {
        // get readable database
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(videoList.TABLE_NAME,
                new String[]{
                        videoList.COLUMN_ID,
                        videoList.COLUMN_TITLE,
                        videoList.COLUMN_VIDEO_PUB_DATE,
                        videoList.COLUMN_VIDEO_SHOOT_DATE,
                        videoList.COLUMN_VIDEO_STATUS,
                        videoList.COLUMN_VIDEO_NUMBER,
                        videoList.COLUMN_DATE_CREATED},
                videoList.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare videos object
        videoList videos = new videoList(
                Long.parseLong(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_ID))),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_NUMBER)),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_SHOOT_DATE)),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_PUB_DATE)),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_STATUS)),
                cursor.getString(cursor.getColumnIndex(videoList.COLUMN_DATE_CREATED)));

        // close the db connection
        cursor.close();

        return videos;
    }


    /**
     * Gets all video.
     * @return the all video
     */
    public ArrayList<videoList> getAllNotPublishedVideos() {
        ArrayList<videoList> noteArrayList = new ArrayList<>();


        // Select All videos Query
        String selectQuery = "SELECT  * FROM  video WHERE video_status='Not Started'OR video_status='Shooting'OR video_status='Editing' ORDER BY shoot_date ASC";

        //Instance of database
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        //looping all rows
        if (cursor.moveToFirst()) {
            do {
                videoList videos = new videoList();
                videos.setId(Long.parseLong(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_ID)))); // getting the id
                videos.setTitle(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_TITLE)));
                videos.setVideoNumber(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_NUMBER)));
                videos.setVideoShootDate(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_SHOOT_DATE)));
                videos.setVideoPublishDate(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_PUB_DATE)));
                videos.setVideoStatus(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_STATUS)));
                videos.setDateCreated(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_DATE_CREATED)));
                noteArrayList.add(videos); // add to the arrayList
                videos.toString();
            } while (cursor.moveToNext());
        }


        db.close();
        cursor.close();

        return noteArrayList;
    }

    public ArrayList<videoList> getAllvideos() {
        ArrayList<videoList> noteArrayList = new ArrayList<>();

        // Select All videos Query
        String selectQuery = "SELECT  * FROM "
                + videoList.TABLE_NAME
                + " ORDER BY "
                + videoList.COLUMN_VIDEO_PUB_DATE
                + " ASC";

        //Instance of database
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);

        //looping all rows
        if (cursor.moveToFirst()) {
            do {
                videoList videos = new videoList();
                videos.setId(Long.parseLong(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_ID)))); // getting the id
                videos.setTitle(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_TITLE)));
                videos.setVideoNumber(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_NUMBER)));
                videos.setVideoShootDate(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_SHOOT_DATE)));
                videos.setVideoPublishDate(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_PUB_DATE)));
                videos.setVideoStatus(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_VIDEO_STATUS)));
                videos.setDateCreated(cursor.getString(cursor.getColumnIndex(videoList.COLUMN_DATE_CREATED)));
                noteArrayList.add(videos); // add to the arrayList
                videos.toString();
            } while (cursor.moveToNext());
        }


        db.close();
        cursor.close();

        return noteArrayList;
    }

    /**
     * Delete event.
     * @param videos the video
     */
    public void deleteYTSchedule(videoList videos) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(videoList.TABLE_NAME, videoList.COLUMN_ID + " = ?",
                new String[]{String.valueOf(videos.getId())});
        db.close();
    }


}
