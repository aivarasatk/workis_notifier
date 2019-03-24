package com.workis.pranesejas.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class JobProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int JOB_SUMMARIES = 100;
    private static final int JOB_SUMMARY_ID = 101;

    private static final int JOB_ENTRIES = 200;
    private static final int JOB_ENTRY_ID = 201;

    private static final int CITY_SUBSCRIPTION = 300;

    private static final int NEWEST_JOB_ENTRIES = 400;

    private static final int NEWEST_JOB_ENTRIES_VALUES = 450;

    private static final int AVAILABLE_JOBS = 500;


    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_JOB_SUMMARY, JOB_SUMMARIES);
        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_JOB_SUMMARY + "/#", JOB_SUMMARY_ID);

        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_JOB_ENTRY, JOB_ENTRIES);
        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_JOB_ENTRY + "/#", JOB_ENTRY_ID);

        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_SETTINGS, CITY_SUBSCRIPTION);

        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_NEWEST_JOB_ENTRIES, NEWEST_JOB_ENTRIES);

        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.NEWEST_JOB_ENTRIES_VALUES, NEWEST_JOB_ENTRIES_VALUES);

        uriMatcher.addURI(JobContract.CONTENT_AUTHORITY, JobContract.PATH_AVAILABLE_JOBS, AVAILABLE_JOBS);
    }

    private JobDbHelper jobDbHelper;

    @Override
    public boolean onCreate() {
        jobDbHelper = new JobDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = jobDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);
        switch (match) {
            case JOB_SUMMARIES:
                cursor = database.query(JobContract.JobSummaryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case JOB_SUMMARY_ID:
                selection = JobContract.JobSummaryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(JobContract.JobSummaryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case JOB_ENTRIES:
                cursor = database.query(JobContract.JobEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case JOB_ENTRY_ID:
                selection = JobContract.JobEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(JobContract.JobEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CITY_SUBSCRIPTION:
                cursor = database.query(JobContract.Settings.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NEWEST_JOB_ENTRIES:
                cursor = database.query(JobContract.NewestJobEntries.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NEWEST_JOB_ENTRIES_VALUES:
                SQLiteQueryBuilder _QB = new SQLiteQueryBuilder();
                _QB.setTables(JobContract.JobEntry.TABLE_NAME +
                        " INNER JOIN " + JobContract.NewestJobEntries.TABLE_NAME + " ON " +
                        JobContract.JobEntry.FULL_ID + " = " + JobContract.NewestJobEntries.COLUMN_JOB_ENTRY_FORREIGN_KEY);
                cursor = _QB.query(database, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case AVAILABLE_JOBS:
                cursor = database.query(JobContract.AvailableJobs.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case JOB_SUMMARIES:
                return JobContract.JobSummaryEntry.CONTENT_LIST_TYPE;
            case JOB_SUMMARY_ID:
                return JobContract.JobSummaryEntry.CONTENT_ITEM_TYPE;
            case JOB_ENTRIES:
                return JobContract.JobEntry.CONTENT_ITEM_TYPE;
            case JOB_ENTRY_ID:
                return JobContract.JobEntry.CONTENT_ITEM_TYPE;
            case CITY_SUBSCRIPTION:
                return JobContract.Settings.CONTENT_ITEM_TYPE;
            case NEWEST_JOB_ENTRIES:
                return JobContract.NewestJobEntries.CONTENT_ITEM_TYPE;
            case NEWEST_JOB_ENTRIES_VALUES:
                return JobContract.NewestJobEntries.CONTENT_ITEM_TYPE;
            case AVAILABLE_JOBS:
                return JobContract.AvailableJobs.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = jobDbHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        if(match == JOB_SUMMARIES){
            long res = db.insert(JobContract.JobSummaryEntry.TABLE_NAME, null, values);
            if(res == -1){
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, res);
        }else if(match == JOB_ENTRIES){
            long res = db.insert(JobContract.JobEntry.TABLE_NAME, null, values);
            if(res == -1){
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, res);
        }else if(match == CITY_SUBSCRIPTION){
            long res = db.insert(JobContract.Settings.TABLE_NAME, null, values);
            if(res == -1){
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, res);
        }else if(match == NEWEST_JOB_ENTRIES){
            long res = db.insert(JobContract.NewestJobEntries.TABLE_NAME, null, values);
            if(res == -1){
                return null;
            }
            getContext().getContentResolver().notifyChange(JobContract.NewestJobEntries.CONTENT_URI_JOIN, null);
            return ContentUris.withAppendedId(uri, res);
        }else if(match == AVAILABLE_JOBS){
            long res = db.insert(JobContract.AvailableJobs.TABLE_NAME, null, values);
            if(res == -1){
                return null;
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, res);
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = jobDbHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);
        if(match == JOB_SUMMARIES){
            return db.delete(JobContract.JobSummaryEntry.TABLE_NAME, selection, selectionArgs);
        }else if (match == JOB_ENTRIES){
            return db.delete(JobContract.JobEntry.TABLE_NAME, selection, selectionArgs);
        }else if (match == CITY_SUBSCRIPTION){
            return db.delete(JobContract.Settings.TABLE_NAME, selection, selectionArgs);
        }else if (match == NEWEST_JOB_ENTRIES){
            return db.delete(JobContract.NewestJobEntries.TABLE_NAME, selection, selectionArgs);
        }else if (match == AVAILABLE_JOBS){
            return db.delete(JobContract.AvailableJobs.TABLE_NAME, selection, selectionArgs);
        }

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = jobDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        if(match == JOB_SUMMARIES){
            getContext().getContentResolver().notifyChange(uri, null);
            return db.update(JobContract.JobSummaryEntry.TABLE_NAME, values, selection, selectionArgs);
        }else if (match == JOB_ENTRIES){
            getContext().getContentResolver().notifyChange(uri, null);
            return db.update(JobContract.JobEntry.TABLE_NAME, values, selection, selectionArgs);
        }else if (match == CITY_SUBSCRIPTION){
            getContext().getContentResolver().notifyChange(uri, null);
            return db.update(JobContract.Settings.TABLE_NAME, values, selection, selectionArgs);
        }else if (match == NEWEST_JOB_ENTRIES){
            getContext().getContentResolver().notifyChange(uri, null);
            return db.update(JobContract.NewestJobEntries.TABLE_NAME, values, selection, selectionArgs);
        }else if (match == AVAILABLE_JOBS){
            getContext().getContentResolver().notifyChange(uri, null);
            return db.update(JobContract.AvailableJobs.TABLE_NAME, values, selection, selectionArgs);
        }
        return 0;
    }
}
