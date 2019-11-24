package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.database.DatabaseFactory;

import java.lang.ref.WeakReference;

public class DistributionBroadcastAsyncTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> contextWeakReference;

    private long threadId;

    private int distributionType;

    public DistributionBroadcastAsyncTask(WeakReference<Context> contextWeakReference, long threadId, int distributionType) {
        this.contextWeakReference = contextWeakReference;
        this.threadId = threadId;
        this.distributionType = distributionType;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DatabaseFactory.getThreadDatabase(contextWeakReference.get())
                .setDistributionType(threadId, distributionType);
        return null;
    }
}
