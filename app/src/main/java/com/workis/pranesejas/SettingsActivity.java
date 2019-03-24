package com.workis.pranesejas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.workis.pranesejas.db.JobContract;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final int URI_LOADER = 1;
    boolean settingsChanged = false;

    private Spinner citySelectionSpinner;
    private EditText rateEditText;
    private AlertDialog.Builder builder;
    private boolean[] selectedWeekdays = {true, true, true, true, true, true, true};
    private boolean[] selectedWeekdaysCopy = new boolean[7];
    private boolean selectedDaysChanged = false;

    private int lastSelection;
    private double lastRate;
    private String[] citiesSubscription = {"Vilnius", "Kaunas", "Klaipeda", "Siauliai", "Palanga"};

    private ArrayList<Integer> mSelectedItems;
    //private String[] cities;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            settingsChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        citySelectionSpinner = findViewById(R.id.settings_spinner);
        rateEditText = findViewById(R.id.settings_rate);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        citySelectionSpinner.setAdapter(adapter);

        citySelectionSpinner.setOnTouchListener(mTouchListener);
        findViewById(R.id.settings_rate).setOnTouchListener(mTouchListener);

        LinearLayout weekdaySelectionLayout = findViewById(R.id.settings_weekday_selection_layout);
        initWeekdayDialog();
        weekdaySelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy( selectedWeekdays, 0, selectedWeekdaysCopy, 0, selectedWeekdays.length);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if(savedInstanceState == null){
            getLoaderManager().initLoader(URI_LOADER, null, this);
        }else{
            getLoaderManager().restartLoader(URI_LOADER, null, this);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_save:
                //update db with new values
                Intent mainActivity = new Intent(this, MainActivity.class);
                if(!isConnected()){
                    Toast.makeText(getApplicationContext(), "Neišaugota! Nėra ryšio", Toast.LENGTH_SHORT).show();
                }else{
                    boolean startParentActivity = false;
                    if(rateEditText.getText().toString() != String.valueOf(lastRate)) {
                        double rate = changeRate();
                        if(rate != -1){//if valid rate was typed
                            mainActivity.putExtra(getResources().getString(R.string.new_rate), String.valueOf(rate));
                            startParentActivity = true;
                        }
                    }
                    if(citySelectionSpinner.getSelectedItemPosition() != lastSelection){
                        changeSubscription(citySelectionSpinner.getSelectedItemPosition(), this);
                        mainActivity.putExtra(getResources().getString(R.string.new_city), citiesSubscription[citySelectionSpinner.getSelectedItemPosition()]);
                        startParentActivity = true;
                    }
                    if(selectedDaysChanged){
                        saveSelectedDays();
                        mainActivity.putExtra(getResources().getString(R.string.new_weekdays), "dummy_text");
                        startParentActivity = true;
                    }
                    if(startParentActivity){
                        startActivity(mainActivity);
                        return true;
                    }
                }
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id){
            case URI_LOADER:{
                String[] projection = {
                        JobContract.Settings._ID,
                        JobContract.Settings.COLUMN_SUBSCRIPTION_ID,
                        JobContract.Settings.COLUMN_RATE,
                        JobContract.Settings.COLUMN_MONDAY ,
                        JobContract.Settings.COLUMN_TUESDAY ,
                        JobContract.Settings.COLUMN_WEDNESDAY,
                        JobContract.Settings.COLUMN_THURSDAY,
                        JobContract.Settings.COLUMN_FRIDAY,
                        JobContract.Settings.COLUMN_SATURDAY,
                        JobContract.Settings.COLUMN_SUNDAY
                };
                return new CursorLoader(this, JobContract.Settings.CONTENT_URI,
                        projection, null, null, null);
            }
            default: return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() != 0){
            data.moveToNext();
            lastSelection = data.getInt(data.getColumnIndex(JobContract.Settings.COLUMN_SUBSCRIPTION_ID));
            citySelectionSpinner.setSelection(lastSelection);
            double rate = data.getDouble(data.getColumnIndex(JobContract.Settings.COLUMN_RATE));
            initSelectedWeekdays(data);
            if(rate != 0){
                rateEditText.setText(String.valueOf(rate));
            }
            lastRate = rate;
        }else{
            lastSelection = JobContract.VILNIUS;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void changeSubscription(final int currentSelection, final Activity activity){
        AsyncTask<Void, Void, Void> subscription = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteCurrentData();
                subscribeToCity(currentSelection);
                //DataRetriever dataRetriever = new DataRetriever(activity);
                //dataRetriever.loadCityData(citiesSubscription[currentSelection]);
                return null;
            }
        };
        subscription.execute();
    }

    private void deleteCurrentData(){
        getContentResolver().delete(JobContract.JobEntry.CONTENT_URI, null, null);//istrinam seno miesto irasus
        getContentResolver().delete(JobContract.NewestJobEntries.CONTENT_URI, null, null);//istrinam seno miesto irasus
    }

    private void subscribeToCity(final int currentSelection){
        ContentValues values = new ContentValues();
        values.put(JobContract.Settings.COLUMN_SUBSCRIPTION_ID, currentSelection);
        getContentResolver().update(JobContract.Settings.CONTENT_URI, values, JobContract.Settings._ID + "=1", null);

        FirebaseMessaging.getInstance().unsubscribeFromTopic(citiesSubscription[lastSelection]).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {//when unsubsribed, we can subsribe to a new topic
                if(task.isSuccessful()){
                    FirebaseMessaging.getInstance().subscribeToTopic(citiesSubscription[currentSelection]).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Prenumeruotas naujas miestas: " + citySelectionSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private double changeRate(){
        try{
            String currentRateString = rateEditText.getText().toString();
            double currentRate;
            if(currentRateString.trim().equals("")){
                currentRate = 0;
            }else{
                currentRate = Double.parseDouble(currentRateString);
            }


            ContentValues values = new ContentValues();
            values.put(JobContract.Settings.COLUMN_RATE, currentRate);
            getContentResolver().update(JobContract.Settings.CONTENT_URI, values, JobContract.Settings._ID + "=1", null);
            return currentRate;

        }catch(Exception e){
            return -1;
        }

    }

    private boolean isConnected(){
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void initWeekdayDialog(){
        mSelectedItems = new ArrayList();  // Where we track the selected items
        builder = new AlertDialog.Builder(SettingsActivity.this);
        // Set the dialog title
        builder.setTitle(R.string.select_weekdays)
            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            .setMultiChoiceItems(R.array.weekdays, selectedWeekdays,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                // If the user checked the item, add it to the selected items
                                //mSelectedItems.add(which);
                                selectedWeekdays[which] = true;
                            } else if (mSelectedItems.contains(which)) {
                                // Else, if the item is already in the array, remove it
                                selectedWeekdays[which] = false;
                                //mSelectedItems.remove(Integer.valueOf(which));
                            }
                        }
                    })
            // Set the action buttons
            .setPositiveButton(R.string.settings_dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if(!selectionUnchanged(selectedWeekdays,selectedWeekdaysCopy)){
                        selectedDaysChanged = true;
                    }else{//atstatom originalu pasirinkimu masyva
                        System.arraycopy( selectedWeekdaysCopy, 0, selectedWeekdays, 0, selectedWeekdaysCopy.length);
                    }
                }
            })
            .setNegativeButton(R.string.settings_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //do not save selection to db
                    System.arraycopy( selectedWeekdaysCopy, 0, selectedWeekdays, 0, selectedWeekdaysCopy.length);
                }
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                System.arraycopy( selectedWeekdaysCopy, 0, selectedWeekdays, 0, selectedWeekdaysCopy.length);
            }
        });
    }

    void initSelectedWeekdays(Cursor cursor){
        int mondayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_MONDAY);
        int tuesdayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_TUESDAY);
        int wednesdayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_WEDNESDAY);
        int thursdayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_THURSDAY);
        int fridayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_FRIDAY);
        int saturdayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_SATURDAY);
        int sundayIndex = cursor.getColumnIndex(JobContract.Settings.COLUMN_SUNDAY);

        selectedWeekdays[0] =  cursor.getInt(mondayIndex)==1?true:false;
        selectedWeekdays[1] =  cursor.getInt(tuesdayIndex)==1?true:false;
        selectedWeekdays[2] =  cursor.getInt(wednesdayIndex)==1?true:false;
        selectedWeekdays[3] =  cursor.getInt(thursdayIndex)==1?true:false;
        selectedWeekdays[4] =  cursor.getInt(fridayIndex)==1?true:false;
        selectedWeekdays[5] =  cursor.getInt(saturdayIndex)==1?true:false;
        selectedWeekdays[6] =  cursor.getInt(sundayIndex)==1?true:false;
    }

    private void saveSelectedDays(){
        AsyncTask<Void, Void, Void> newDays = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(JobContract.Settings.COLUMN_MONDAY, selectedWeekdays[0]?1:0);
                values.put(JobContract.Settings.COLUMN_TUESDAY, selectedWeekdays[1]?1:0);
                values.put(JobContract.Settings.COLUMN_WEDNESDAY, selectedWeekdays[2]?1:0);
                values.put(JobContract.Settings.COLUMN_THURSDAY, selectedWeekdays[3]?1:0);
                values.put(JobContract.Settings.COLUMN_FRIDAY, selectedWeekdays[4]?1:0);
                values.put(JobContract.Settings.COLUMN_SATURDAY, selectedWeekdays[5]?1:0);
                values.put(JobContract.Settings.COLUMN_SUNDAY, selectedWeekdays[6]?1:0);
                getContentResolver().update(JobContract.Settings.CONTENT_URI, values, JobContract.Settings._ID + "=1", null);
                return null;
            }
        };
        newDays.execute();
    }

    private boolean selectionUnchanged(boolean[] array1, boolean[] array2){
        for(int i = 0; i < array1.length; ++i){
            if(array1[i] != array2[i]){
                return false;
            }
        }
        return true;
    }
}
