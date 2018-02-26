package com.shalu.mykitchendiaries;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import static com.shalu.mykitchendiaries.ui.NotifyMeFragment.refreshPlacesData;

public class FirebaseJobService extends JobService {
    private AsyncTask mBackgroundTask;
    @Override
    public boolean onStartJob(final JobParameters job) {
        mBackgroundTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Context context = FirebaseJobService.this;
                refreshPlacesData(context);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                /*
                 * Once the AsyncTask is finished, the job is finished. To inform JobManager that
                 * you're done, you call jobFinished with the jobParamters that were passed to your
                 * job and a boolean representing whether the job needs to be rescheduled. This is
                 * usually if something didn't work and you want the job to try running again.
                 */

                jobFinished(job, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
