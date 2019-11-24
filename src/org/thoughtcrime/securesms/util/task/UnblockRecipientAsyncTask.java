package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.jobs.MultiDeviceBlockedUpdateJob;
import org.thoughtcrime.securesms.recipients.LiveRecipient;

import java.lang.ref.WeakReference;

public class UnblockRecipientAsyncTask extends AsyncTask<Void, Void, Void> {

    private LiveRecipient recipient;

    private WeakReference<Context> contextWeakReference;

    public UnblockRecipientAsyncTask(LiveRecipient recipient, WeakReference<Context> contextWeakReference) {
        this.recipient = recipient;
        this.contextWeakReference = contextWeakReference;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DatabaseFactory.getRecipientDatabase(contextWeakReference.get())
                .setBlocked(recipient.getId(), false);
        ApplicationDependencies.getJobManager().add(new MultiDeviceBlockedUpdateJob());

        return null;
    }
}
