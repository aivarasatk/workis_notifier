package com.workis.pranesejas.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.workis.pranesejas.db.JobContract.JobEntry;
import com.workis.pranesejas.db.JobContract.JobSummaryEntry;

public class JobDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "workis.db";
    private static final int DATABASE_VERSION = 6;

    private final String DROP_NEWEST_JOB_ENTRIES = "DROP TABLE " + JobContract.NewestJobEntries.TABLE_NAME;
    private final String DROP_JOB_ENTRY = "DROP TABLE " + JobEntry.TABLE_NAME;
    private final String DROP_SETTINGS = "DROP TABLE " + JobContract.Settings.TABLE_NAME;
    private final String DROP_AVAILABLE_JOBS = "DROP TABLE " + JobContract.AvailableJobs.TABLE_NAME;
    private final String DROP_JOB_SUMMARY = "DROP TABLE " + JobContract.JobSummaryEntry.TABLE_NAME;


    public JobDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_JOB_SUMMARY = "CREATE TABLE " + JobSummaryEntry.TABLE_NAME + " ( "+
                JobSummaryEntry._ID + " integer primary key autoincrement, " +
                JobSummaryEntry.COLUMN_JOB_COUNT + " integer not null, " +
                JobSummaryEntry.COLUMN_JOB_DATE + " integer not null, " +
                JobSummaryEntry.COLUMN_JOB_VIEW_STATUS + " integer default 0, " +
                JobSummaryEntry.COLUMN_JOB_VISIBILITY + " integer default 1" + " );";

        db.execSQL(SQL_CREATE_JOB_SUMMARY);

        String SQL_CREATE_JOB_ENTRY = "CREATE TABLE " + JobEntry.TABLE_NAME + " ( "+
                JobEntry._ID + " integer primary key autoincrement, " +
                JobEntry.COLUMN_JOB_TITLE + " text not null, " +
                JobEntry.COLUMN_COMPANY + " text not null, " +
                JobEntry.COLUMN_ADDRESS + " text not null, " +
                JobEntry.COLUMN_LOGO_URL + " text default null, " +
                JobEntry.COLUMN_HOUR_PAY + " real not null, " +
                JobEntry.COLUMN_EXPECTED_TOTAL + " integer not null, " +
                JobEntry.COLUMN_JOB_START_TIME + " datetime not null, " +
                JobEntry.COLUMN_JOB_END_TIME + " datetime not null, " +
                JobEntry.COLUMN_JOB_UPDATE_TIME + " integer not null " +" );";

        db.execSQL(SQL_CREATE_JOB_ENTRY);

        String SQL_CREATE_SETTINGS = "CREATE TABLE " + JobContract.Settings.TABLE_NAME + " ( "+
                JobContract.Settings._ID + " integer primary key autoincrement, " +
                JobContract.Settings.COLUMN_SUBSCRIPTION_ID + " integer default 0, " +
                JobContract.Settings.COLUMN_RATE + " double default 0, "+
                JobContract.Settings.COLUMN_MONDAY + " int default 1, "+
                JobContract.Settings.COLUMN_TUESDAY + " int default 1, "+
                JobContract.Settings.COLUMN_WEDNESDAY + " int default 1, "+
                JobContract.Settings.COLUMN_THURSDAY + " int default 1, "+
                JobContract.Settings.COLUMN_FRIDAY + " int default 1, "+
                JobContract.Settings.COLUMN_SATURDAY + " int default 1, "+
                JobContract.Settings.COLUMN_SUNDAY + " int default 1 "+" );";

        db.execSQL(SQL_CREATE_SETTINGS);

        String SQL_CREATE_NEWEST_JOB_ENTRIES = "CREATE TABLE " + JobContract.NewestJobEntries.TABLE_NAME + " ( "+
                JobContract.NewestJobEntries._ID + " integer primary key autoincrement, " +
                JobContract.NewestJobEntries.COLUMN_JOB_ENTRY_FORREIGN_KEY + " integer not null, " +

                "FOREIGN KEY ("+JobContract.NewestJobEntries.COLUMN_JOB_ENTRY_FORREIGN_KEY+")" +
                " REFERENCES "+ JobEntry.TABLE_NAME +"("+JobEntry._ID+") " +
                "ON DELETE CASCADE " + " );";

        db.execSQL(SQL_CREATE_NEWEST_JOB_ENTRIES);

        String SQL_CREATE_AVAILABLE_JOBS = "CREATE TABLE " + JobContract.AvailableJobs.TABLE_NAME + " ( "+
                JobContract.AvailableJobs._ID + " integer primary key autoincrement, " +
                JobContract.AvailableJobs.COLUMN_JOB_TITLE + " text not null, " +
                JobContract.AvailableJobs.COLUMN_COMPANY + " text not null, " +
                JobContract.AvailableJobs.COLUMN_ADDRESS + " text not null, " +
                JobContract.AvailableJobs.COLUMN_LOGO_URL + " text default null, " +
                JobContract.AvailableJobs.COLUMN_HOUR_PAY + " real not null, " +
                JobContract.AvailableJobs.COLUMN_EXPECTED_TOTAL + " integer not null, " +
                JobContract.AvailableJobs.COLUMN_JOB_START_TIME + " datetime not null, " +
                JobContract.AvailableJobs.COLUMN_JOB_END_TIME + " datetime not null " +" );";

        db.execSQL(SQL_CREATE_AVAILABLE_JOBS);

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_NEWEST_JOB_ENTRIES);
        db.execSQL(DROP_JOB_ENTRY);
        db.execSQL(DROP_SETTINGS);
        db.execSQL(DROP_AVAILABLE_JOBS);
        db.execSQL(DROP_JOB_SUMMARY);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
