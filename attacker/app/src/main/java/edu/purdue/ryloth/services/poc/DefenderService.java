package edu.purdue.ryloth.services.poc;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.ryloth.R;
import edu.purdue.ryloth.RApp;
import edu.purdue.ryloth.dmz.DUtils;

public class DefenderService extends IntentService {

    private static final String TAG = "PoC/DefService";

    // frequency for checking the buffer (millisecs)
    private final static int INTERVAL = 1000;
    // delay to destroy/finish activities
    private final static int NUM_DELAY_START_ACT = 300;

    private static boolean sServiceStarted = false;
    private static DefenderService sService;

    /** notification channel */
    private static final int FOREGROUND_SENSOR_SERVICE = 1213;
    private static final String CHANNEL_ID = "edu.purdue.ryloth.def";
    private static final String CHANNEL_NAME = "ryloth";
    private static final String CHANNEL_DESCRIPTION = "ryloth/def";

    public DefenderService() {
        // used to name the worker thread
        super("bg-defender");
    }

    /* ---------------------------------------------------------------
     * Communication Primitives
     * --------------------------------------------------------------- */
    public static synchronized void startService(Context context) {
            Log.d(TAG, String.format("Starting defender mechanism - API %d",
                    Build.VERSION.SDK_INT));
            Intent intent = new Intent(context, DefenderService.class);

            if ( sServiceStarted ) {
                // do nothing
            } else {
                // start service
                sServiceStarted = true;
                context.startService(intent);
                Log.d(TAG, "startService");

            }

    }

    public static synchronized void stopService(Context context) {
        Log.d(TAG, "Stopping defender mechanism");
        Intent intent = new Intent(context, DefenderService.class);

        if ( sServiceStarted ) {
            context.stopService(intent);
        }
    }

    /* ---------------------------------------------------------------
     * Async Task
     * --------------------------------------------------------------- */

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(INTERVAL);
                Log.d(TAG, "Searching");
                doCheckAsync();
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }
        }
    };

    private void doTask() {
        Log.d(TAG, "Enters doTask");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                handler.post(runnable);
            }
        };
        Log.d(TAG, String.format("1 Enters doTask MyApp number %d", Constants.gNumber++));
        timer.schedule(task, 0, INTERVAL);
    }


    private void doCheck() {

        DUtils.log(String.format("Enters doCheck MyApp number (%d) buffer.size (%d)",
                Constants.gNumber++, Constants.gIntentBuffer.size()));
        if ( Constants.POC_MODE != 2 ) return;

        if (!Constants.gIntentBuffer.isEmpty()) {

            Intent intent = Constants.gIntentBuffer.remove(0);
            sendIntent(intent);
            DUtils.log(String.format("Sent intent: %s bufferSize %d", intent, Constants.gIntentBuffer.size()));

        }
    }

    private void sendIntent(Intent intent){

        DUtils.log("SendIntentbyType | " + intent);

        try {

            int code = 1999 + intent.getComponent().hashCode();
            DUtils.logp(Constants.POC_TAG_INTENT_FWD, intent,
                    String.format("buffer.size (%d)", Constants.gIntentBuffer.size()));
            RApp.myActivity.startActivityForResult(intent, code);
            Thread.sleep(NUM_DELAY_START_ACT);
            RApp.myActivity.finishActivity(code);
            DUtils.log("SendIntentbyType | Started Activity: {" + intent.getComponent() + "}");
        }
        catch (Throwable ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }

    }

    /**
     * Launch a search in a new thread (and not in the main application thread).
     */
    private void doCheckAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doCheck();
            }
        }).start();
    }

    /* ---------------------------------------------------------------
     * Override Methods
     * --------------------------------------------------------------- */

    @Override
    public void onCreate() {
        super.onCreate();

        sService = this;
        if ( Build.VERSION.SDK_INT >= 26 ) {
            startForeground(FOREGROUND_SENSOR_SERVICE, makeNotification());
        }
        Log.d(TAG, "Enters onCreate");
        doTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // set static instance variables
        sService = null;
        sServiceStarted = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
    }

    /* ---------------------------------------------------------------
     * Helper Methods
     * --------------------------------------------------------------- */

    @RequiresApi(26)
    private Notification makeNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
    }
}
