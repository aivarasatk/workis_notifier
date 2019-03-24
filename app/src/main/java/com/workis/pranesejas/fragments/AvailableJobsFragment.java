package com.workis.pranesejas.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workis.pranesejas.JobCursorAdapter;
import com.workis.pranesejas.MainActivity;
import com.workis.pranesejas.R;
import com.workis.pranesejas.db.DataRetriever;
import com.workis.pranesejas.db.JobContract;
import com.workis.pranesejas.onLoadingIconShow;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class AvailableJobsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>{

    private final int URI_LOADER = 2;
    private View rootView;
    private JobCursorAdapter jobCursorAdapter;
    private ListView listView;
    private SwipeRefreshLayout refreshLayout;
    private boolean loaderInited = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_available_jobs_list, container, false);

        if(savedInstanceState != null){
            if((Boolean)savedInstanceState.getBoolean(getString(R.string.loader_inited)) != null){
                loaderInited = savedInstanceState.getBoolean(getString(R.string.loader_inited));
            }
        }

        refreshLayout = rootView.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        listView = rootView.findViewById(R.id.job_list_view);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                int topRowVerticalPosition =
                        (absListView == null || absListView.getChildCount() == 0) ?
                                0 : absListView.getFirstVisiblePosition() == 0 ? absListView.getChildAt(0).getTop() : -1;
                refreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        if(!loaderInited){
            getLoaderManager().initLoader(URI_LOADER, null, this);
            loaderInited = true;
        }else{
            //reloadindavom del to, kad laika atnaujintu
            getLoaderManager().restartLoader(URI_LOADER, null, this);
        }

        return rootView;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isConnected()){
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.no_connection_text, Toast.LENGTH_SHORT).show();

                }else{
                    getActivity().getContentResolver().delete(JobContract.AvailableJobs.CONTENT_URI, null, null);

                    String[] projection = {
                            JobContract.Settings._ID,
                            JobContract.Settings.COLUMN_SUBSCRIPTION_ID};
                    Cursor settings = getActivity().getContentResolver().query(JobContract.Settings.CONTENT_URI, projection, null, null, null);
                    settings.moveToNext();
                    String[] cities = getResources().getStringArray(R.array.cities_array_ascii);
                    String subscribedCity = cities[settings.getInt(settings.getColumnIndex(JobContract.Settings.COLUMN_SUBSCRIPTION_ID))];

                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("available_jobs").child(subscribedCity);
                    final DataRetriever dataRetriever = new DataRetriever(getActivity().getApplicationContext(), null);
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            AsyncTask<Void, Void, Void> insertToDb = new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    dataRetriever.getSnapshotChildren(dataSnapshot, JobContract.AvailableJobs.CONTENT_URI, System.currentTimeMillis(), false);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void counter) {
                                    super.onPostExecute(counter);
                                    refreshLayout.setRefreshing(false);
                                }
                            };
                            insertToDb.execute();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };
                    dbRef.addListenerForSingleValueEvent(eventListener);
                }

            }
        }, 0);
        //ASYNC TASK TO GET DATA. STOP REFRESH


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case URI_LOADER:{
                String[] projection = {
                        JobContract.AvailableJobs._ID,
                        JobContract.AvailableJobs.COLUMN_ADDRESS,
                        JobContract.AvailableJobs.COLUMN_COMPANY,
                        JobContract.AvailableJobs.COLUMN_EXPECTED_TOTAL,
                        JobContract.AvailableJobs.COLUMN_HOUR_PAY,
                        JobContract.AvailableJobs.COLUMN_JOB_START_TIME,
                        JobContract.AvailableJobs.COLUMN_JOB_END_TIME,
                        JobContract.AvailableJobs.COLUMN_JOB_TITLE,
                        JobContract.AvailableJobs.COLUMN_LOGO_URL,
                };

                DataRetriever dr = new DataRetriever(getActivity().getApplicationContext(),null);
                String rate = dr.getCurrentRate();

                ArrayList<Integer> selectedWeekdays = dr.getWeekdayIDs();
                String dateIn = "";
                String selection;
                if(selectedWeekdays.size() != 0){
                    dateIn += "IN (";
                    for(Integer ID : selectedWeekdays){
                        dateIn += "?,";
                    }
                    dateIn = dateIn.substring(0, dateIn.length() - 1);
                    dateIn += ")";
                    selection = JobContract.AvailableJobs.COLUMN_HOUR_PAY + ">=? AND strftime('%w', " + JobContract.JobEntry.COLUMN_JOB_START_TIME + ") " + dateIn;
                }else{
                    selection = JobContract.AvailableJobs.COLUMN_HOUR_PAY + ">=? AND 0";//nerodom jokiu darbu
                }

                String []selectionArgs = new String[selectedWeekdays.size() + 1];
                selectionArgs[0] = rate;
                for(int i = 0; i < selectedWeekdays.size(); ++i){
                    selectionArgs[i+1] = String.valueOf(selectedWeekdays.get(i));
                }

                return new CursorLoader(getActivity(), JobContract.AvailableJobs.CONTENT_URI,
                        projection, selection, selectionArgs, JobContract.AvailableJobs.COLUMN_JOB_START_TIME + " ASC");
            }
            default:return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        jobCursorAdapter = new JobCursorAdapter(getActivity(), data);

        TextView text = rootView.findViewById(R.id.empty_title_text);
        text.setText("Swipe down to update\n\t(200-400 KB)");

        listView.setEmptyView(text);
        listView.setAdapter(jobCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);

                String url = "https://www.workis.online/";
                intent.setData(Uri.parse(url));

                startActivity(intent);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        jobCursorAdapter.swapCursor(null);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(getResources().getString(R.string.new_city), getActivity().getIntent().getStringExtra(getResources().getString(R.string.new_city)));
        if(loaderInited){
            outState.putBoolean(getString(R.string.loader_inited), true);
        }else{
            outState.putBoolean(getString(R.string.loader_inited), false);
        }
        super.onSaveInstanceState(outState);
    }

    private boolean isConnected(){
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(getActivity() != null && getUserVisibleHint()) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.available_jobs_title);
        }*/
    }
}
