package com.workis.pranesejas.db;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workis.pranesejas.R;
import com.workis.pranesejas.data.JobObject;
import com.workis.pranesejas.data.JobSummaryObject;
import com.workis.pranesejas.data.LoadingIconCallback;
import com.workis.pranesejas.onLoadingIconShow;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DataRetriever{

    private Context context;
    EventBus eventBus;
    private boolean finishedLoading = false;
    private boolean used = false;

    public DataRetriever(Context activityContext, EventBus bus){
        context = activityContext;
        this.eventBus = bus;
    }

    public boolean getFinishedLoading() {return finishedLoading;}
    public boolean isUsed() {return used;}

    public void loadCityData(final String defaultCity){
        used = true;
        context.getContentResolver().delete(JobContract.AvailableJobs.CONTENT_URI, null, null);
        final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(context.getResources().getString(R.string.job_updates_summary_db_uri)).child(currentDate).child(defaultCity);

        if(eventBus != null){
            eventBus.post(new LoadingIconCallback(View.VISIBLE));
        }

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                AsyncTask<Void, Void, Void> insertToDb = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(context.getResources().getString(R.string.job_updates_db_uri)).child(currentDate).child(defaultCity);
                        loadJobEntries(dbRef);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void uriList) {
                        super.onPostExecute(uriList);

                    }
                };
                insertToDb.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.d("TAG_INSERT_ERROR", "CUT OFF");
            }
        };
        dbRef.addListenerForSingleValueEvent(eventListener);
    }

    public void loadJobEntries(DatabaseReference dbRef){

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                AsyncTask<Void, Void, Integer> insertToDb = new AsyncTask<Void, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Void... voids) {
                        int counter = 0;
                        long entriesCount = dataSnapshot.getChildrenCount();
                        long currentMillis = System.currentTimeMillis();
                        for(DataSnapshot timeSnapshot : dataSnapshot.getChildren()){
                            ArrayList<Long> newestIDs = getSnapshotChildren(timeSnapshot, JobContract.JobEntry.CONTENT_URI, currentMillis, true);
                            ++counter;
                            if(counter == entriesCount){//irasom paskutinio atnaujinimo darbus i "naujausiu" skilti
                                context.getContentResolver().delete(JobContract.NewestJobEntries.CONTENT_URI, null, null);
                                for(Long id:newestIDs){
                                    ContentValues values = new ContentValues();
                                    values.put(JobContract.NewestJobEntries.COLUMN_JOB_ENTRY_FORREIGN_KEY, id);
                                    context.getContentResolver().insert(JobContract.NewestJobEntries.CONTENT_URI, values);
                                }
                            }
                        }
                        return counter;
                    }

                    @Override
                    protected void onPostExecute(Integer counter) {
                        super.onPostExecute(counter);

                        if(eventBus != null){
                            eventBus.post(new LoadingIconCallback(View.GONE));
                        }
                        finishedLoading = true;
                    }
                };
                insertToDb.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.d("TAG_INSERT_ERROR", "CUT OFF ENTRY");
            }
        };
        dbRef.addListenerForSingleValueEvent(eventListener);
    }

    /*public int recountJobs(double rate){
        String[] projection = {JobContract.JobSummaryEntry._ID};
        Cursor cursor = context.getContentResolver().query(JobContract.JobSummaryEntry.CONTENT_URI, projection, null, null, null);
        int currentUpdateCount = 0;
        for(int i = 0; i < cursor.getCount(); ++i){
            cursor.moveToNext();

            long summaryID = cursor.getLong(cursor.getColumnIndex(JobContract.JobSummaryEntry._ID));
            //getting job count
            String[] jobProjection = {JobContract.JobEntry._ID};
            String selection = JobContract.JobEntry.COLUMN_JOB_SUMMARY_FOREIGN_KEY + "=? AND " + JobContract.JobEntry.COLUMN_HOUR_PAY + ">=?";
            Cursor jobEntries = context.getContentResolver().query(JobContract.JobEntry.CONTENT_URI, jobProjection, selection,
                    new String[] {String.valueOf(summaryID), String.valueOf(rate)}, null);
            //updating job summary
            ContentValues values = new ContentValues();
            values.put(JobContract.JobSummaryEntry.COLUMN_JOB_COUNT, jobEntries.getCount());
            if(jobEntries.getCount() == 0){
                values.put(JobContract.JobSummaryEntry.COLUMN_JOB_VISIBILITY, 0);
            }else{
                values.put(JobContract.JobSummaryEntry.COLUMN_JOB_VISIBILITY, 1);
                currentUpdateCount += 1;
            }
            context.getContentResolver().update(JobContract.JobSummaryEntry.CONTENT_URI, values, JobContract.JobSummaryEntry._ID + "=" + String.valueOf(summaryID), null);
        }
        return currentUpdateCount;
    }*/

    public ArrayList<Long> getSnapshotChildren(DataSnapshot timeSnapshot, Uri dataInsertUri, long currentMillis, boolean jobEntry){
        ArrayList<Long> newestIDs = new ArrayList<>();
        for(DataSnapshot jobs : timeSnapshot.getChildren()){
            JobObject job = jobs.getValue(JobObject.class);
            ContentValues values = new ContentValues();
            values.put(JobContract.JobEntry.COLUMN_ADDRESS, job.getAddress());
            values.put(JobContract.JobEntry.COLUMN_COMPANY, job.getCompany());
            values.put(JobContract.JobEntry.COLUMN_EXPECTED_TOTAL, job.getExpected_total());
            values.put(JobContract.JobEntry.COLUMN_HOUR_PAY, job.getRate());
            values.put(JobContract.JobEntry.COLUMN_JOB_START_TIME, job.getStart_datetime());
            values.put(JobContract.JobEntry.COLUMN_JOB_END_TIME, job.getEnd_datetime());
            values.put(JobContract.JobEntry.COLUMN_JOB_TITLE, job.getTitle());
            values.put(JobContract.JobEntry.COLUMN_LOGO_URL, job.getLogo_url());
            if(jobEntry){
                values.put(JobContract.JobEntry.COLUMN_JOB_UPDATE_TIME, currentMillis);
            }

            Uri uri = context.getContentResolver().insert(dataInsertUri, values);

            newestIDs.add(ContentUris.parseId(uri));
        }
        return newestIDs;
    }

    public String getCurrentRate(){
        String[] projection = {JobContract.Settings._ID, JobContract.Settings.COLUMN_RATE};
        Cursor cursor = context.getContentResolver().query(JobContract.Settings.CONTENT_URI, projection, null, null, null);

        if(cursor == null || cursor.getCount() == 0){
            return "0";
        }
        cursor.moveToNext();
        double rate = cursor.getDouble(cursor.getColumnIndex(JobContract.Settings.COLUMN_RATE));
        return String.valueOf(rate);
    }

    public ArrayList<Integer> getWeekdayIDs(){
        String[] projection = {
          JobContract.Settings._ID,
          JobContract.Settings.COLUMN_MONDAY,
          JobContract.Settings.COLUMN_TUESDAY,
          JobContract.Settings.COLUMN_WEDNESDAY,
          JobContract.Settings.COLUMN_THURSDAY,
          JobContract.Settings.COLUMN_FRIDAY,
          JobContract.Settings.COLUMN_SATURDAY,
          JobContract.Settings.COLUMN_SUNDAY
        };

        Cursor cursor = context.getContentResolver().query(JobContract.Settings.CONTENT_URI, projection, null, null, null);
        ArrayList<Integer> IDs = new ArrayList<>();
        if(cursor.getCount() != 0){
            cursor.moveToNext();

            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_SUNDAY)) == 1){
                IDs.add(0);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_MONDAY)) == 1){
                IDs.add(1);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_TUESDAY)) == 1){
                IDs.add(2);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_WEDNESDAY)) == 1){
                IDs.add(3);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_THURSDAY)) == 1){
                IDs.add(4);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_FRIDAY)) == 1){
                IDs.add(5);
            }
            if(cursor.getInt(cursor.getColumnIndex(JobContract.Settings.COLUMN_SATURDAY)) == 1) {
                IDs.add(6);
            }
        }else{
            for(int i = 0; i < 7; ++i){
                IDs.add(i);
            }
        }

        return IDs;
    }
}
