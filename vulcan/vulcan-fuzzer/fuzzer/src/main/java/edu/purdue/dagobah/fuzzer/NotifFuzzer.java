package edu.purdue.dagobah.fuzzer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import edu.purdue.dagobah.R;

/**
 * https://developer.android.com/training/wearables/notifications/creating
 * https://developer.android.com/training/wearables/notifications/bridger.html
 */
public class NotifFuzzer {

    private static final String TAG = "Kylo/NotifFuzz";

    private static final String CHANNEL_ID = "kylo";
    private static final String CHANNEL_DESC = "kylo";
    private static final int NOTIFI_THRESHOLD = 500;

    private static final int MAX_NOTIF = 500;

    private static int currentId = 0;
    private Context context;
    private int icon;

    /**
     * Create an instance of {@link NotifFuzzer} using the context of the caller (e.g., Service,
     * Activity)
     *
     * @param ctxt the Context of the caller.
     */
    public NotifFuzzer(Context ctxt, int icon) {
        this.context = ctxt;
        this.icon = icon;

        // create channel (for API >= 26)
        createNotifChannel();
    }

    /* ---------------------------------------------------------------------------
     * fuzzer operations
     * --------------------------------------------------------------------------- */

    /**
     * Create a bridged notification between devices.
     * @param title
     * @param text
     */
    public void createNotif(int notifId, String title, String text) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(notifId, builder.build());

        Log.d(TAG, String.format("Creating notification {%d} {%s}", notifId, title));

        // cancel notification
        //notificationManager.cancel(notifId);
    }

    public void createNotif2() {
        String title, text;
        int notifId;

        for (int i=0; i < MAX_NOTIF; i++) {
            notifId = getId();
            title = String.format("test-%d", notifId);
            text = String.format("text notification: %d", notifId);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this.context, CHANNEL_ID)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
            notificationManager.notify(notifId, builder.build());

            Log.d(TAG, String.format("Creating notification {%d} {%s}", notifId, title));

            // cancel notification
//            if (i > NOTIFI_THRESHOLD)
//                notificationManager.cancel(notifId - NOTIFI_THRESHOLD);

        }

    }

    public void fuzzNotif() {
//        String title, text;
//        int notifId;
//
//        for (int i=0; i< MAX_NOTIF; i++) {
//            notifId = getId();
//            title = String.format("test-%d", notifId);
//            text = String.format("text notification: %d", notifId);
//            createNotif(notifId, title, text);
//        }
        createNotif2();
    }

    /* ---------------------------------------------------------------------------
     * internal/helper methods
     * --------------------------------------------------------------------------- */

    /**
     * https://developer.android.com/training/notify-user/build-notification
     */
    private void createNotifChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getId() {
        return currentId++;
    }

}
