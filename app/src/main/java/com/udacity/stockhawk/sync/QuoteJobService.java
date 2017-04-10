package com.udacity.stockhawk.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import timber.log.Timber;

public class QuoteJobService extends JobService {


    /*
    onStartJob(JobParameters params) is the method that you must use when you begin your task,
    because it is what the system uses to trigger jobs that have already been scheduled.
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Timber.d("Intent handled");
        Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);
        getApplicationContext().startService(nowIntent);
        return true;
    }

    /*
    onStopJob(JobParameters params) is used by the system to cancel pending tasks when a cancel request is received.
    It's important to note that if onStartJob(JobParameters params) returns false, the system assumes there are
    no jobs currently running when a cancel request is received. In other words, it simply won't call
    onStopJob(JobParameters params)
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }


}
