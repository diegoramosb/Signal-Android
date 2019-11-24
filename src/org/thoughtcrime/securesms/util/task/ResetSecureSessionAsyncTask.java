package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingEndSessionMessage;

import java.lang.ref.WeakReference;

public class ResetSecureSessionAsyncTask extends AsyncTask<OutgoingEndSessionMessage, Void, Long> {

    private WeakReference<Context> contextWeakReference;

    private WeakReference<ConversationActivity> activityWeakReference;

    private long threadId;

    public ResetSecureSessionAsyncTask(WeakReference<Context> contextWeakReference, WeakReference<ConversationActivity> activityWeakReference, long threadId) {
        this.contextWeakReference = contextWeakReference;
        this.activityWeakReference = activityWeakReference;
        this.threadId = threadId;
    }

    @Override
    protected Long doInBackground(OutgoingEndSessionMessage... messages) {
        return MessageSender.send(contextWeakReference.get(), messages[0], threadId, false, null);
    }

    @Override
    protected void onPostExecute(Long result) {
        activityWeakReference.get().sendComplete(result);
    }
}
