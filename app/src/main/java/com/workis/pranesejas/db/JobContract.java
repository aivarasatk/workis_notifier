package com.workis.pranesejas.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class JobContract {
    private JobContract(){}

    public final static String JOB_COUNT_KEY = "job_count";
    public final static String TIME_KEY = "time";
    public final static String DATE_URI_KEY = "date_uri";
    public final static String CITY_URI_KEY = "city_uri";
    public final static String TIME_URI_KEY = "time_uri";

    //miestu prenumeratos indeksai
    public final static int VILNIUS = 0;
    public final static int KAUNAS = 1;
    public final static int KLAIPEDA = 2;
    public final static int SIAULIAI = 3;
    public final static int PALANGA = 4;

    public static final class JobSummaryEntry implements BaseColumns {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOB_SUMMARY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOB_SUMMARY;

        public final static String TABLE_NAME = "JobSummary";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_JOB_COUNT = "job_count";

        public final static String COLUMN_JOB_DATE = "date";

        public final static String COLUMN_JOB_VIEW_STATUS = "viewStatus";

        public final static String COLUMN_JOB_VISIBILITY = "visibility";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_JOB_SUMMARY);
    }

    public static final class JobEntry implements BaseColumns {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOB_ENTRY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOB_ENTRY;

        public final static String TABLE_NAME = "JobEntry";

        public final static String _ID = BaseColumns._ID;

        public final static String FULL_ID = TABLE_NAME + "." + BaseColumns._ID;

        public final static String COLUMN_LOGO_URL = "logo_url";

        public final static String COLUMN_JOB_TITLE = "title";

        public final static String COLUMN_COMPANY = "company";
        public final static String COLUMN_ADDRESS = "address";
        public final static String COLUMN_HOUR_PAY = "hour_pay";
        public final static String COLUMN_EXPECTED_TOTAL = "expected_total";
        public final static String COLUMN_JOB_START_TIME = "start_datetime";
        public final static String COLUMN_JOB_END_TIME = "end_datetime";
        public final static String COLUMN_JOB_UPDATE_TIME = "job_update_time";
        public final static String COLUMN_JOB_SUMMARY_FOREIGN_KEY = "job_summary_id";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_JOB_ENTRY);
    }

    public static final class Settings implements BaseColumns {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SETTINGS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SETTINGS;

        public final static String TABLE_NAME = "Settings";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_SUBSCRIPTION_ID = "subscription_id";
        public final static String COLUMN_RATE = "rate";

        public final static String COLUMN_MONDAY = "monday";
        public final static String COLUMN_TUESDAY = "tuesday";
        public final static String COLUMN_WEDNESDAY = "wednesday";
        public final static String COLUMN_THURSDAY = "thursday";
        public final static String COLUMN_FRIDAY = "friday";
        public final static String COLUMN_SATURDAY = "saturday";
        public final static String COLUMN_SUNDAY = "sunday";


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SETTINGS);
    }

    public static final class NewestJobEntries implements BaseColumns {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWEST_JOB_ENTRIES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWEST_JOB_ENTRIES;

        public final static String TABLE_NAME = "NewestJobEntries";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_JOB_ENTRY_FORREIGN_KEY = "job_entry_id";


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NEWEST_JOB_ENTRIES);
        public static final Uri CONTENT_URI_JOIN = Uri.withAppendedPath(BASE_CONTENT_URI, NEWEST_JOB_ENTRIES_VALUES);

    }

    public static final class AvailableJobs implements BaseColumns {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AVAILABLE_JOBS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AVAILABLE_JOBS;

        public final static String TABLE_NAME = "AvailableJobs";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_LOGO_URL = "logo_url";

        public final static String COLUMN_JOB_TITLE = "title";

        public final static String COLUMN_COMPANY = "company";
        public final static String COLUMN_ADDRESS = "address";
        public final static String COLUMN_HOUR_PAY = "hour_pay";
        public final static String COLUMN_EXPECTED_TOTAL = "expected_total";
        public final static String COLUMN_JOB_START_TIME = "start_datetime";
        public final static String COLUMN_JOB_END_TIME = "end_datetime";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_AVAILABLE_JOBS);
    }

    public static final String CONTENT_AUTHORITY = "com.workis.pranesejas";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_JOB_SUMMARY = "JobSummary";
    public static final String PATH_JOB_ENTRY = "JobEntry";
    public static final String PATH_SETTINGS = "Settings";
    public static final String PATH_NEWEST_JOB_ENTRIES = "NewestJobEntries";
    public static final String PATH_AVAILABLE_JOBS = "AvailableJobs";
    public static final String NEWEST_JOB_ENTRIES_VALUES = "NewestJobEntriesValues";

}
