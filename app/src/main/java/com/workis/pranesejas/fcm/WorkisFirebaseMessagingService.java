package com.workis.pranesejas.fcm;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.workis.pranesejas.MainActivity;
import com.workis.pranesejas.R;
import com.workis.pranesejas.data.JobObject;
import com.workis.pranesejas.db.DataRetriever;
import com.workis.pranesejas.db.JobContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;

public class WorkisFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "TAG01";
    private int notificationId = 0;
    private String CHANNEL_ID = "workis_notif_channel";

    private DatabaseReference databaseReference;
    private ValueEventListener childEventListener = null;

    private Integer jobCount = -1;
    private long time;
    private String currentDateUri;
    private String currentCityUri;
    private String currentTimeUri;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final Map<String, String> data = remoteMessage.getData();
        if(data != null && data.size() != 0){
            /*for (Map.Entry<String, String> entry : data.entrySet())
            {
                Log.d(TAG, entry.getKey() + "/" + entry.getValue());
            }
            */
            try{
                jobCount = Integer.parseInt(data.get(JobContract.JOB_COUNT_KEY));
            }catch(Exception e){
                jobCount = -1;
            }

            time = Long.parseLong(data.get(JobContract.TIME_KEY));
            currentDateUri = data.get(JobContract.DATE_URI_KEY);
            currentCityUri = data.get(JobContract.CITY_URI_KEY);
            currentTimeUri = data.get(JobContract.TIME_URI_KEY);

            insertJobEntryToDb();

        }
    }

    private void insertJobEntryToDb(/*final long currentJobSummaryInsertID*/){
        databaseReference = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.job_updates_db_uri)).child(currentDateUri).child(currentCityUri).child(currentTimeUri);


        if(childEventListener == null){
            childEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                    final DataRetriever dr = new DataRetriever(getApplicationContext(), null);
                    AsyncTask<Void, Void, Void> insertToDb = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            double currentRate = getCurrentRate();
                            int updatedJobCount = 0;
                            ArrayList<Integer> weekdayIDs = dr.getWeekdayIDs();
                            ArrayList<Long> newestIDs = new ArrayList<>();
                            for(DataSnapshot timeSnapshot : dataSnapshot.getChildren()){
                                final JobObject job = timeSnapshot.getValue(JobObject.class);
                                ContentValues values = new ContentValues();
                                values.put(JobContract.JobEntry.COLUMN_ADDRESS, job.getAddress());
                                values.put(JobContract.JobEntry.COLUMN_COMPANY, job.getCompany());
                                values.put(JobContract.JobEntry.COLUMN_EXPECTED_TOTAL, job.getExpected_total());
                                values.put(JobContract.JobEntry.COLUMN_HOUR_PAY, job.getRate());
                                values.put(JobContract.JobEntry.COLUMN_JOB_START_TIME, job.getStart_datetime());
                                values.put(JobContract.JobEntry.COLUMN_JOB_END_TIME, job.getEnd_datetime());
                                values.put(JobContract.JobEntry.COLUMN_JOB_TITLE, job.getTitle());
                                values.put(JobContract.JobEntry.COLUMN_LOGO_URL, job.getLogo_url());
                                values.put(JobContract.JobEntry.COLUMN_JOB_UPDATE_TIME, time);
                                Uri uri = getContentResolver().insert(JobContract.JobEntry.CONTENT_URI, values);

                                newestIDs.add(ContentUris.parseId(uri));//TODO: ne pilnas sprendimas
                                if(job.getRate() >= currentRate && isSelectedDate(weekdayIDs, job.getStart_datetime())){

                                    updatedJobCount += 1;
                                }
                            }

                            jobCount = updatedJobCount;

                            if(jobCount > 0) {//TODO: ne pilnas sprendimas
                                getContentResolver().delete(JobContract.NewestJobEntries.CONTENT_URI, null, null);
                            }
                                for(Long id:newestIDs){
                                    ContentValues values = new ContentValues();
                                    values.put(JobContract.NewestJobEntries.COLUMN_JOB_ENTRY_FORREIGN_KEY, id);
                                    getContentResolver().insert(JobContract.NewestJobEntries.CONTENT_URI, values);
                                }
                            //}
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if(jobCount > 0){
                                sendNotification(/*currentJobSummaryInsertID*/);
                            }
                        }
                    };

                    insertToDb.execute();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addListenerForSingleValueEvent(childEventListener);
        }
    }
    private void sendNotification(/*long currentJobSummaryInsertID*/){
        Intent intent = new Intent(this, MainActivity.class);//TODO: OLD WORKIS new Intent(this, JobListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Uri uri = ContentUris.withAppendedId(JobContract.JobSummaryEntry.CONTENT_URI, currentJobSummaryInsertID);
        //intent.setData(uri);

        PendingIntent pendingIntent =
                TaskStackBuilder.create(this)
                        // add all of DetailsActivity's parents to the stack,
                        // followed by DetailsActivity itself
                        .addNextIntentWithParentStack(intent)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Įvyko darbų atnaujinimas")
                .setContentText("Naujų darbų: " + jobCount)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(DEFAULT_ALL)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(time);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
        ++notificationId;

    }

    private void updateJobSummary(int jobCount, long currentSummaryInsertID){
        ContentValues values = new ContentValues();
        values.put(JobContract.JobSummaryEntry.COLUMN_JOB_COUNT, jobCount);
        if(jobCount > 0){
            values.put(JobContract.JobSummaryEntry.COLUMN_JOB_VISIBILITY, 1);
        }
        getContentResolver().update(JobContract.JobSummaryEntry.CONTENT_URI, values, JobContract.JobSummaryEntry._ID + "=" + String.valueOf(currentSummaryInsertID), null);
    }

    private double getCurrentRate(){
        String[] projection = {JobContract.Settings._ID, JobContract.Settings.COLUMN_RATE};
        Cursor cursor = getContentResolver().query(JobContract.Settings.CONTENT_URI, projection, null, null, null);

        cursor.moveToNext();
        double rate = cursor.getDouble(cursor.getColumnIndex(JobContract.Settings.COLUMN_RATE));
        return rate;
    }

    private boolean isSelectedDate(ArrayList<Integer> IDs, String date){
        SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try{
            Date newDate = formatInput.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(newDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            for(Integer ID : IDs){
                if(ID + 1 == dayOfWeek){
                    return true;
                }
            }
        }catch (Exception e){
        }
        return false;

    }
}
