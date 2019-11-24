package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.os.AsyncTask;

import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.conversation.ConversationFragment;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.recipients.LiveRecipient;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;

import java.lang.ref.WeakReference;

public class SendMessageAsyncTask extends AsyncTask<OutgoingTextMessage, Void, Long> {

    private boolean initiatingConversation;

    private boolean forceSms;

    private LiveRecipient recipient;

    private long threadId;

    private long id;

    private WeakReference<Context> contextWeakReference;

    private WeakReference<ConversationFragment> fragmentWeakReference;

    private WeakReference<ConversationActivity> activityWeakReference;

    public SendMessageAsyncTask(boolean initiatingConversation, boolean forceSms, LiveRecipient recipient, long threadId, long id, WeakReference<Context> contextWeakReference, WeakReference<ConversationFragment> fragmentWeakReference, WeakReference<ConversationActivity> activityWeakReference) {
        this.initiatingConversation = initiatingConversation;
        this.forceSms = forceSms;
        this.recipient = recipient;
        this.threadId = threadId;
        this.id = id;
        this.contextWeakReference = contextWeakReference;
        this.fragmentWeakReference = fragmentWeakReference;
        this.activityWeakReference = activityWeakReference;
    }

    @Override
    protected Long doInBackground(OutgoingTextMessage... messages) {
        if (initiatingConversation) {
            DatabaseFactory.getRecipientDatabase(contextWeakReference.get()).setProfileSharing(recipient.getId(), true);
        }

        return MessageSender.send(contextWeakReference.get(), messages[0], threadId, forceSms, () -> fragmentWeakReference.get().releaseOutgoingMessage(id));
    }

    @Override
    protected void onPostExecute(Long result) {
        activityWeakReference.get().sendComplete(result);
    }
}
