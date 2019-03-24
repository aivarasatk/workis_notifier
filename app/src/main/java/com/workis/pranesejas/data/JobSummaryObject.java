package com.workis.pranesejas.data;

public class JobSummaryObject {

    private long update_time;
    private int job_count;

    public JobSummaryObject(){}
    public JobSummaryObject(long update_time, int job_count) {
        this.update_time = update_time;
        this.job_count = job_count;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }

    public int getJob_count() {
        return job_count;
    }

    public void setJob_count(int job_count) {
        this.job_count = job_count;
    }
}
