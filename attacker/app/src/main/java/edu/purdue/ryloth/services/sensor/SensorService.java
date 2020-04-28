package edu.purdue.ryloth.services.sensor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.fuzzer.intent.enums.IPCType;
import edu.purdue.ryloth.R;
import edu.purdue.ryloth.RApp;
import edu.purdue.ryloth.dmz.DUtils;
import edu.purdue.ryloth.services.sensor.SensorController.Listener;

import static edu.purdue.dagobah.common.Constants.NUM_DELAY_START_ACT;
import static edu.purdue.dagobah.common.Constants.gSensorReads;

public class SensorService extends Service implements Listener {

    private static final String TAG = "ryloth/sensor";

    /** notification channel */
    private static final int FOREGROUND_SENSOR_SERVICE = 1212;
    private static final String CHANNEL_ID = "edu.purdue.ryloth.hearrate";
    private static final String CHANNEL_NAME = "ryloth";
    private static final String CHANNEL_DESCRIPTION = "ryloth/cftest";

    /** sensors */
    private SensorController mSensor;

    private static SensorService sService;
    private static boolean sServiceStarted = false;

    /** async tasks */
    // frequency for checking the buffer
    private final static int INTERVAL = 1000 * 5;           // 5s

    /* ---------------------------------------------------------------
     * Sensor Services Primitives
     * --------------------------------------------------------------- */
    private synchronized void onSensorRequest(Intent intent) {

        Log.d(TAG, "[event] onSensorRequest");
        // create an instance of SensorController
        if ( this.mSensor == null ) {
            this.mSensor = new SensorController(this, this);
        }

        // register listener for sensor
        this.mSensor.registerSensorRequest(this);
    }

    private synchronized void onSensorClear() {
        Log.d(TAG, "[event] onSensorClear");

        // unregister listener for sensor
        if ( this.mSensor != null )
            this.mSensor.unregisterSensorRequest();
    }

    public static synchronized void startSensor(Context context) {
        synchronized (SensorService.class) {
            Log.d(TAG, "Starting sensor");
            Intent intent = new Intent(context, SensorService.class);

            if ( sServiceStarted ) {
                // do nothing
            } else if ( sService == null ) {
                // start service
                sServiceStarted = true;
                if ( Build.VERSION.SDK_INT >= 26 ) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
            } else {
                // sensor service is already running
                sService.onSensorRequest(intent);
            }

        }
    }

    public static synchronized void stopSensor(Context context) {
        Log.d(TAG, "Stopping sensor");
        Intent intent = new Intent(context, SensorService.class);

        if ( sServiceStarted ) {
            if ( Build.VERSION.SDK_INT >= 26 ) {
                sService.stopForeground(true);
                sService.stopSelf();
            } else {
                context.stopService(intent);
            }
        }
    }

    /* ---------------------------------------------------------------
     * Listener
     * --------------------------------------------------------------- */

    @Override
    public void onBeat(SensorEvent event) {
        // do nothing
        gSensorReads++;
    }

    @Override
    public void onSensorError(String error) {
        Log.d(TAG, "onSensorError");
        // wake lock ??
    }

    @Override
    public void onSensorStopped() {

    }


    /* ---------------------------------------------------------------
     * Override Methods
     * --------------------------------------------------------------- */

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // handle sensor request
        onSensorRequest(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, String.format("/destroyed/ reads (%s)", gSensorReads));
        onSensorClear();
        // set static instance variables
        sService = null;
        sServiceStarted = false;
    }


    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        sService = this;
        if ( Build.VERSION.SDK_INT >= 26 ) {
            startForeground(FOREGROUND_SENSOR_SERVICE, makeNotification());
        }

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
