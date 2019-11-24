package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.conversation.ConversationFragment;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.OutgoingExpirationUpdateMessage;
import org.thoughtcrime.securesms.recipients.LiveRecipient;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.sms.MessageSender;

import java.lang.ref.WeakReference;

public class ExpirationTimeAsyncTask extends AsyncTask {

    private LiveRecipient liveRecipient;

    private Recipient recipient;

    private Long threadId;

    private int expirationTime;

    private ConversationFragment fragment;

    private WeakReference<Context> contextWeakReference;

    private WeakReference<ConversationActivity> activityWeakReference;


    public ExpirationTimeAsyncTask(LiveRecipient liveRecipient, Recipient recipient, Long threadId, int expirationTime, WeakReference<Context> contextWeakReference, WeakReference<ConversationActivity> activityWeakReference, ConversationFragment fragment) {
        this.liveRecipient = liveRecipient;
        this.recipient = recipient;
        this.threadId = threadId;
        this.expirationTime = expirationTime;
        this.contextWeakReference = contextWeakReference;
        this.activityWeakReference = activityWeakReference;
        this.fragment = fragment;
    }

    public ExpirationTimeAsyncTask(LiveRecipient liveRecipient, Recipient recipient, Long threadId, int expirationTime, WeakReference<Context> contextWeakReference) {
        this.liveRecipient = liveRecipient;
        this.recipient = recipient;
        this.threadId = threadId;
        this.expirationTime = expirationTime;
        this.contextWeakReference = contextWeakReference;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        DatabaseFactory.getRecipientDatabase(contextWeakReference.get()).setExpireMessages(liveRecipient.getId(), expirationTime);
        OutgoingExpirationUpdateMessage outgoingMessage = new OutgoingExpirationUpdateMessage(recipient, System.currentTimeMillis(), expirationTime * 1000L);
        MessageSender.send(contextWeakReference.get(), outgoingMessage, threadId, false, null);

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        activityWeakReference.get().invalidateOptionsMenu();
        if (fragment != null) fragment.setLastSeen(0);
    }
}
