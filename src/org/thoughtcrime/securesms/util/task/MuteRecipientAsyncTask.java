package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.recipients.LiveRecipient;

import java.lang.ref.WeakReference;

public class MuteRecipientAsyncTask extends AsyncTask<Void, Void, Void> {

    private LiveRecipient recipient;

    private long until;

    private WeakReference<Context> contextWeakReference;

    public MuteRecipientAsyncTask(LiveRecipient recipient, long until, Context contextWeakReference) {
        this.recipient = recipient;
        this.until = until;
        this.contextWeakReference = new WeakReference<>(contextWeakReference);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DatabaseFactory.getRecipientDatabase(contextWeakReference.get())
                .setMuted(recipient.getId(), until);

        return null;
    }
}
