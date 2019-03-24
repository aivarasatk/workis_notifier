package com.workis.pranesejas;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.workis.pranesejas.db.JobContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JobCursorAdapter extends CursorAdapter {

    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_SECTIONED_CELL = 1;
    private static final int STATE_REGULAR_CELL = 2;

    private boolean initiated = false;
    private int companyLogoIndex;
    private int titleIndex;
    private int companyNameIndex;
    private int jobAddressIndex;
    private int hourPayIndex;
    private int expectedTotalIndex;
    private int workTimeStartIndex;
    private int workTimeEndIndex;

    private int[] mCellStates;

    static class ViewHolder{
        ImageView companyLogoView ;
        TextView separator;
        TextView titleView;
        TextView companyNameView;
        TextView jobAddressView;
        TextView hourPayView;
        TextView expectedTotalView;
        TextView workTimeView;
        RelativeLayout mainLayout;
    }

    public JobCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mCellStates = (c == null ? null : new int[c.getCount()]);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v =  LayoutInflater.from(context).inflate(R.layout.job_list_item, parent, false);

        if(!initiated){
            companyLogoIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_LOGO_URL);
            titleIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_JOB_TITLE);
            companyNameIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_COMPANY);
            jobAddressIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_ADDRESS);
            hourPayIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_HOUR_PAY);
            expectedTotalIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_EXPECTED_TOTAL);
            workTimeStartIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_JOB_START_TIME);
            workTimeEndIndex = cursor.getColumnIndex(JobContract.JobEntry.COLUMN_JOB_END_TIME);
            initiated = true;
        }
        ViewHolder holder = new ViewHolder();
        holder.companyLogoView = v.findViewById(R.id.company_logo);
        holder.separator = v.findViewById(R.id.separator);
        holder.titleView = v.findViewById(R.id.job_title);
        holder.companyNameView = v.findViewById(R.id.company_name);
        holder.jobAddressView = v.findViewById(R.id.job_address);
        holder.hourPayView = v.findViewById(R.id.hour_pay);
        holder.expectedTotalView = v.findViewById(R.id.expected_total);
        holder.workTimeView = v.findViewById(R.id.work_time);
        holder.mainLayout = v.findViewById(R.id.job_list_main_layout);

        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.titleView.setText(cursor.getString(titleIndex));
        holder.companyNameView.setText(cursor.getString(companyNameIndex));
        holder.jobAddressView.setText(cursor.getString(jobAddressIndex));
        holder.hourPayView.setText(String.valueOf(cursor.getDouble(hourPayIndex)) + " €/val");
        holder.expectedTotalView.setText("Viso: " + String.valueOf(cursor.getInt(expectedTotalIndex)) + " €");

        if(cursor.getString(companyLogoIndex) != null){
            Glide.with(holder.companyLogoView.getContext())//set imageView
                    .load(cursor.getString(companyLogoIndex))
                    .into(holder.companyLogoView);
        }

        String finalDate = "";
        String dateToTest = "";
        try{
            SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat formatOutput = new SimpleDateFormat("HH:mm");
            SimpleDateFormat formatDate= new SimpleDateFormat("yyyy-MM-dd");

            Date newDate = formatInput.parse(cursor.getString(workTimeStartIndex));
            dateToTest = formatDate.format(newDate);

            Date startDate = formatInput.parse(cursor.getString(workTimeStartIndex));
            String dateStart = formatOutput.format(startDate);

            Date endDate = formatInput.parse(cursor.getString(workTimeEndIndex));
            String dateEnd = formatOutput.format(endDate);

            finalDate = dateStart + " - " + dateEnd;

        }catch(Exception e){
            finalDate = "Nerasta!";
        }
        holder.workTimeView.setText(finalDate);


        boolean needSeparator = isSeparatorNeeded(cursor, dateToTest);
        evaluateSeparator(needSeparator, holder, dateToTest, view);

    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        super.swapCursor(cursor);
        mCellStates = (cursor == null ? null : new int[cursor.getCount()]);
        return cursor;
    }

    private boolean isSeparatorNeeded(Cursor cursor, String dateToTest){
        boolean needSeparator = false;
        final int position = cursor.getPosition();
        switch (mCellStates[position]) {
            case STATE_SECTIONED_CELL:
                needSeparator = true;
                break;

            case STATE_REGULAR_CELL:
                needSeparator = false;
                break;

            case STATE_UNKNOWN:
            default:
                // A separator is needed if it's the first itemview of the
                // ListView or if the group of the current cell is different
                // from the previous itemview.
                if (position == 0) {
                    needSeparator = true;
                } else{
                    cursor.moveToPosition(position - 1);

                    String previousDateToTest = "";
                    try{
                        SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        SimpleDateFormat formatDate= new SimpleDateFormat("yyyy-MM-dd");

                        Date newDate = formatInput.parse(cursor.getString(workTimeStartIndex));
                        previousDateToTest = formatDate.format(newDate);
                    }catch (Exception e){
                        cursor.moveToPosition(position);
                    }

                    if (!dateToTest.equals(previousDateToTest)) {
                        needSeparator = true;
                    }

                    cursor.moveToPosition(position);
                }

                // Cache the result
                mCellStates[position] = (needSeparator ? STATE_SECTIONED_CELL : STATE_REGULAR_CELL);
                break;
        }
        return needSeparator;
    }

    private void evaluateSeparator(boolean needSeparator, ViewHolder holder, String dateToTest, View view){
        if (needSeparator) {
            holder.separator.setText(dateToTest);
            holder.separator.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams params = holder.mainLayout.getLayoutParams();

            int itemHeightDp = (int) (view.getResources().getDimension(R.dimen.job_item_height) / view.getResources().getDisplayMetrics().density);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemHeightDp, view.getResources().getDisplayMetrics());

            params.height = height;
            holder.mainLayout.setLayoutParams(params);
        } else {
            holder.separator.setVisibility(TextView.GONE);
            ViewGroup.LayoutParams params = holder.mainLayout.getLayoutParams();

            int itemHeightDp = (int) (view.getResources().getDimension(R.dimen.job_item_height) / view.getResources().getDisplayMetrics().density);
            int separatorHeightDp = (int) (view.getResources().getDimension(R.dimen.separator_height) / view.getResources().getDisplayMetrics().density);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemHeightDp - separatorHeightDp, view.getResources().getDisplayMetrics());

            params.height = height;
            holder.mainLayout.setLayoutParams(params);
        }
    }
}
