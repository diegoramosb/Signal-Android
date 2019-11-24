package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.components.location.SignalPlace;
import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.DraftDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.mms.AttachmentManager;
import org.thoughtcrime.securesms.mms.QuoteId;
import org.thoughtcrime.securesms.util.concurrent.AssertedSuccessListener;
import org.thoughtcrime.securesms.util.concurrent.ListenableFuture;
import org.thoughtcrime.securesms.util.concurrent.SettableFuture;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DraftAsyncTask extends AsyncTask<Void, Void, List<DraftDatabase.Draft>> {

    private long threadId;

    private SettableFuture<Boolean> future;

    private WeakReference<Context> contextWeakReference;

    private WeakReference<ConversationActivity> activityWeakReference;

    private String tag;

    public DraftAsyncTask(long threadId, SettableFuture<Boolean> future, WeakReference<Context> contextWeakReference, WeakReference<ConversationActivity> activityWeakReference, String tag) {
        this.threadId = threadId;
        this.future = future;
        this.contextWeakReference = contextWeakReference;
        this.activityWeakReference = activityWeakReference;
        this.tag = tag;
    }

    @Override
    protected List<DraftDatabase.Draft> doInBackground(Void... params) {
        DraftDatabase draftDatabase = DatabaseFactory.getDraftDatabase(contextWeakReference.get());
        List<DraftDatabase.Draft> results         = draftDatabase.getDrafts(threadId);

        draftDatabase.clearDrafts(threadId);

        return results;
    }

    @Override
    protected void onPostExecute(List<DraftDatabase.Draft> drafts) {
        if (drafts.isEmpty()) {
            future.set(false);
            activityWeakReference.get().updateToggleButtonState();
            return;
        }

        AtomicInteger draftsRemaining = new AtomicInteger(drafts.size());
        AtomicBoolean success         = new AtomicBoolean(false);
        ListenableFuture.Listener<Boolean> listener        = new AssertedSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                success.compareAndSet(false, result);

                if (draftsRemaining.decrementAndGet() <= 0) {
                    future.set(success.get());
                }
            }
        };

        for (DraftDatabase.Draft draft : drafts) {
            try {
                switch (draft.getType()) {
                    case DraftDatabase.Draft.TEXT:
                        activityWeakReference.get().composeText.setText(draft.getValue());
                        listener.onSuccess(true);
                        break;
                    case DraftDatabase.Draft.LOCATION:
                        activityWeakReference.get().attachmentManager.setLocation(SignalPlace.deserialize(draft.getValue()), activityWeakReference.get().getCurrentMediaConstraints()).addListener(listener);
                        break;
                    case DraftDatabase.Draft.IMAGE:
                        activityWeakReference.get().setMedia(Uri.parse(draft.getValue()), AttachmentManager.MediaType.IMAGE).addListener(listener);
                        break;
                    case DraftDatabase.Draft.AUDIO:
                        activityWeakReference.get().setMedia(Uri.parse(draft.getValue()), AttachmentManager.MediaType.AUDIO).addListener(listener);
                        break;
                    case DraftDatabase.Draft.VIDEO:
                        activityWeakReference.get().setMedia(Uri.parse(draft.getValue()), AttachmentManager.MediaType.VIDEO).addListener(listener);
                        break;
                    case DraftDatabase.Draft.QUOTE:
                        SettableFuture<Boolean> quoteResult = new SettableFuture<>();
                        new QuoteRestorationTask(draft.getValue(), quoteResult, contextWeakReference, activityWeakReference, tag).execute();
                        quoteResult.addListener(listener);
                        break;
                }
            } catch (IOException e) {
                Log.w(tag, e);
            }
        }

        activityWeakReference.get().updateToggleButtonState();
    }

    private class QuoteRestorationTask extends AsyncTask<Void, Void, MessageRecord> {

        private final String                  serialized;
        private final SettableFuture<Boolean> future;
        private final WeakReference<Context>  contextWeakReference;
        private final WeakReference<ConversationActivity> activityWeakReference;
        private final String                  tag;

        public QuoteRestorationTask(@NonNull String serialized, @NonNull SettableFuture<Boolean> future, @NonNull WeakReference<Context> contextWeakReference, @NonNull WeakReference<ConversationActivity> activityWeakReference, @NonNull String tag) {
            this.serialized = serialized;
            this.future = future;
            this.contextWeakReference = contextWeakReference;
            this.activityWeakReference = activityWeakReference;
            this.tag = tag;
        }

        @Override
        protected MessageRecord doInBackground(Void... voids) {
            QuoteId quoteId = QuoteId.deserialize(contextWeakReference.get(), serialized);

            if (quoteId != null) {
                return DatabaseFactory.getMmsSmsDatabase(activityWeakReference.get().getApplicationContext()).getMessageFor(quoteId.getId(), quoteId.getAuthor());
            }

            return null;
        }

        @Override
        protected void onPostExecute(MessageRecord messageRecord) {
            if (messageRecord != null) {
                activityWeakReference.get().handleReplyMessage(messageRecord);
                future.set(true);
            } else {
                Log.e(tag, "Failed to restore a quote from a draft. No matching message record.");
                future.set(false);
            }
        }
    }
}

