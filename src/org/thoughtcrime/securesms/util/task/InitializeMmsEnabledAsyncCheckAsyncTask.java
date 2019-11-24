package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.util.Util;

import java.lang.ref.WeakReference;

public class InitializeMmsEnabledAsyncCheckAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Context> contextWeakReference;

    private boolean isMmsEnabled;

    public InitializeMmsEnabledAsyncCheckAsyncTask(WeakReference<Context> contextWeakReference, boolean isMmsEnabled) {
        this.contextWeakReference = contextWeakReference;
        this.isMmsEnabled = isMmsEnabled;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return Util.isMmsCapable(contextWeakReference.get());
    }

    @Override
    protected void onPostExecute(Boolean isMmsEnabled) {
        this.isMmsEnabled = isMmsEnabled;
    }
}
