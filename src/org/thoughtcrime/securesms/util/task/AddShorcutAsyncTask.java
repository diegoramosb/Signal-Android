package org.thoughtcrime.securesms.util.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.ShortcutLauncherActivity;
import org.thoughtcrime.securesms.conversation.ConversationActivity;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.recipients.LiveRecipient;
import org.thoughtcrime.securesms.util.BitmapUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class AddShorcutAsyncTask extends AsyncTask<Void, Void, IconCompat> {

    private LiveRecipient recipient;

    private String tag;

    private WeakReference<ConversationActivity> activityWeakReference;

    private WeakReference<Context> contextWeakReference;

    public AddShorcutAsyncTask(LiveRecipient recipient, String tag, WeakReference<ConversationActivity> activityWeakReference, WeakReference<Context> contextWeakReference) {
        this.recipient = recipient;
        this.tag = tag;
        this.activityWeakReference = activityWeakReference;
        this.contextWeakReference = contextWeakReference;
    }

    @Override
    protected IconCompat doInBackground(Void... voids) {
        Context context = activityWeakReference.get().getApplicationContext();
        IconCompat icon    = null;

        if (recipient.get().getContactPhoto() != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(recipient.get().getContactPhoto().openInputStream(context));
                bitmap = BitmapUtil.createScaledBitmap(bitmap, 300, 300);
                icon   = IconCompat.createWithAdaptiveBitmap(bitmap);
            } catch (IOException e) {
                Log.w(tag, "Failed to decode contact photo during shortcut creation. Falling back to generic icon.", e);
            }
        }

        if (icon == null) {
            icon = IconCompat.createWithResource(context, recipient.get().isGroup() ? R.mipmap.ic_group_shortcut
                    : R.mipmap.ic_person_shortcut);
        }

        return icon;
    }

    @Override
    protected void onPostExecute(IconCompat icon) {
        Context context  = activityWeakReference.get().getApplicationContext();
        String  name     = recipient.get().getDisplayName(contextWeakReference.get());

        ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, recipient.get().getId().serialize() + '-' + System.currentTimeMillis())
                .setShortLabel(name)
                .setIcon(icon)
                .setIntent(ShortcutLauncherActivity.createIntent(context, recipient.getId()))
                .build();

        if (ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)) {
            Toast.makeText(context, context.getString(R.string.ConversationActivity_added_to_home_screen), Toast.LENGTH_LONG).show();
        }
    }
}
