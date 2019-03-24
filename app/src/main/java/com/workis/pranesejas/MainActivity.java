package com.workis.pranesejas;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.workis.pranesejas.data.LoadingIconCallback;
import com.workis.pranesejas.db.DataRetriever;
import com.workis.pranesejas.db.JobContract;

import org.greenrobot.eventbus.EventBus;

import java.time.Year;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{


    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private String[] cities;

    EventBus eventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cities = getResources().getStringArray(R.array.cities_array);

        deleteLocalOldRecords(); //istrinami lokalus ne sios dienos atnaujinimai

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pranešėjas");
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        loadApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_about){
            showAboutPopup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getAboutMessage();
        builder.setMessage(message)
                .setPositiveButton("Gerai", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        builder.create().show();
    }

    private void deleteLocalOldRecords(){//TODO: ISTRINTI JOB ENTRIES
        String where = "strftime('%Y-%m-%d',"+ JobContract.JobEntry.COLUMN_JOB_UPDATE_TIME +"  / 1000, 'unixepoch') != date('now')";
        getContentResolver().delete(JobContract.JobEntry.CONTENT_URI,where, null);
    }



    private void loadNewCityData(String newCity, DataRetriever dataRetriever){
        dataRetriever.loadCityData(newCity);
    }
    private void loadApp(){
        final DataRetriever dataRetriever = new DataRetriever(getApplicationContext(), eventBus);
        eventBus.post(new LoadingIconCallback(View.VISIBLE));
        AsyncTask<Void, Void, Cursor> dbValue = new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... voids) {
                String[] projection = {
                        JobContract.Settings._ID,
                        JobContract.Settings.COLUMN_SUBSCRIPTION_ID,
                        JobContract.Settings.COLUMN_RATE
                };
                return getContentResolver().query(JobContract.Settings.CONTENT_URI, projection, null, null, null);

            }

            @Override
            protected void onPostExecute(Cursor result) {
                if(result == null){
                    eventBus.post(new LoadingIconCallback(View.GONE));
                    return;
                }else if(result.getCount() == 0){//not yet subsribed
                    final int deafultCitySubscriptionID = JobContract.VILNIUS;
                    final String defaultCity = cities[deafultCitySubscriptionID];
                    FirebaseMessaging.getInstance().subscribeToTopic(defaultCity)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        String msg = getString(R.string.msg_subscribe_failed);
                                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                    }else{
                                        dataRetriever.loadCityData(defaultCity);
                                        ContentValues values = new ContentValues();
                                        values.put(JobContract.Settings.COLUMN_SUBSCRIPTION_ID, deafultCitySubscriptionID);
                                        getContentResolver().insert(JobContract.Settings.CONTENT_URI, values);
                                    }
                                }
                            });
                }else if(result.getCount() == 1){
                    eventBus.post(new LoadingIconCallback(View.GONE));
                    //we are already subscribed, do nothing
                    //Log.d("TAG_NO", "already subbed");
                }

                if(getIntent().hasExtra(getResources().getString(R.string.new_city))) {//jei griztam is nustatymu su nauju miestu. perkraunam duomenis
                    String newCity = getIntent().getStringExtra(getResources().getString(R.string.new_city));
                    if (newCity != null) {
                        loadNewCityData(newCity, dataRetriever);
                    }

                }

                /*if(getIntent().hasExtra(getResources().getString(R.string.new_rate))){
                    String rateString = getIntent().getStringExtra(getResources().getString(R.string.new_rate));
                    double rate = Double.parseDouble(rateString);
                    applyRateChangeToJobs(rate, dataRetriever);
                }*/
            }
        };
        dbValue.execute();
    }

    void applyRateChangeToJobs(final double rate, final DataRetriever dataRetriever){
        final Timer newTimer = new Timer();
        TimerTask waitForJobDownload = new TimerTask() {
            @Override
            public void run() {
                if(dataRetriever.getFinishedLoading() || !dataRetriever.isUsed()){
                    //dataRetriever.recountJobs(rate);
                    newTimer.cancel();
                }
            }
        };

        newTimer.schedule(waitForJobDownload, 0, 50);
    }


    public String getAboutMessage() {
        String message = "© ";
        if(Integer.valueOf(R.string.about_text_cr_offset) < Calendar.getInstance().get(Calendar.YEAR)){
            message += getString(R.string.about_text_cr_offset) + "-" + Calendar.getInstance().get(Calendar.YEAR);
        }else{
            message += getString(R.string.about_text_cr_offset);
        }
        message += " Aivaras Atkočaitis";
        return message;
    }
}
