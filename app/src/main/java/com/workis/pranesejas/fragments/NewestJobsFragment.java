package com.workis.pranesejas.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.workis.pranesejas.JobCursorAdapter;
import com.workis.pranesejas.R;
import com.workis.pranesejas.data.LoadingIconCallback;
import com.workis.pranesejas.db.DataRetriever;
import com.workis.pranesejas.db.JobContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class NewestJobsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final int URI_LOADER = 0;
    private JobCursorAdapter jobCursorAdapter;
    private ListView listView;
    private View rootView;
    private boolean loaderInited = false;
    private ProgressBar loadingIcon;

    private EventBus eventBus = EventBus.getDefault();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(getActivity() != null  && getUserVisibleHint()) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.newest_job_title);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            if((Boolean)savedInstanceState.getBoolean(getString(R.string.loader_inited)) != null){
                loaderInited = savedInstanceState.getBoolean(getString(R.string.loader_inited));
            }
        }

        rootView = inflater.inflate(R.layout.activity_job_list, container, false);
        listView = rootView.findViewById(R.id.job_list_view);
        loadingIcon = rootView.findViewById(R.id.loading_spinner);
        loadingIcon.setVisibility(View.GONE);

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case URI_LOADER:{
                String[] projection = {
                        JobContract.JobEntry.FULL_ID,
                        JobContract.JobEntry.COLUMN_ADDRESS,
                        JobContract.JobEntry.COLUMN_COMPANY,
                        JobContract.JobEntry.COLUMN_EXPECTED_TOTAL,
                        JobContract.JobEntry.COLUMN_HOUR_PAY,
                        JobContract.JobEntry.COLUMN_JOB_START_TIME,
                        JobContract.JobEntry.COLUMN_JOB_END_TIME,
                        JobContract.JobEntry.COLUMN_JOB_TITLE,
                        JobContract.JobEntry.COLUMN_LOGO_URL,
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

                return new CursorLoader(getActivity(), JobContract.NewestJobEntries.CONTENT_URI_JOIN,
                        projection, selection, selectionArgs, JobContract.JobEntry.COLUMN_JOB_START_TIME + " ASC");
            }
            default:return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        jobCursorAdapter = new JobCursorAdapter(getActivity(), data);

        TextView emptyView = rootView.findViewById(R.id.empty_title_text);
        emptyView.setText(getResources().getString(R.string.empty_view_title_text));

        listView.setEmptyView(emptyView);
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

    @Subscribe
    public void onEvent(LoadingIconCallback event) {
        loadingIcon.setVisibility(event.visibilityState);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }
}
