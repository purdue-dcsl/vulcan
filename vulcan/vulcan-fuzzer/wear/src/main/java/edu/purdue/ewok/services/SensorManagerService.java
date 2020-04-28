package edu.purdue.ewok.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SensorManagerService extends Service implements SensorEventListener {

    private static String TAG = "KyloSensorSrv";
    private SensorManager mSensorManager;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Create an instance of {@link SensorManagerService}
     */
    public SensorManagerService() {
    }


    /**
     * Get an instance of the {@link SensorManager} and register a listener for the sensors defined
     * in {@code sensors} array, with {@code delay} sampling period.
     *
     * @param sensors the array of int that identifies each of the sensor to register.
     * @param delay the sampling period.
     */
    public void start(int sensors[], int delay) {
        Log.d(TAG, "SensorManager :: started!");

        if ( mSensorManager == null )
            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        if ( mSensorManager == null )
            return;

        // Register sensors
        for (int sensor : sensors) {
            Sensor sns = mSensorManager.getDefaultSensor(sensor);
            if ( sns != null ) {
                mSensorManager.registerListener(this, sns, delay);
                Log.d(TAG, String.format("Register listener for {%d} sensor", sensor));
            } else {
                Log.d(TAG, String.format("Not possible to register a listener for {%d} sensor", sensor));
            }
        }
    }

    /**
     * Register a listener for the sensors defined in {@code sensors} array with the fastest sampling
     * period possible.
     * @param sensor the array of int that identifies each of the sensor to register.
     */
    public void start(int sensor[]) {
        start(sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Register a listener for the Heart Rate sensor.
     */
    public void start() {
        int[] sensors = {Sensor.TYPE_HEART_RATE};
        start(sensors, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Unregister all the listener from the {@link SensorManager}.
     */
    public void stop() {
        if (mSensorManager != null) {
            Log.d(TAG, "SensorManager :: stopped!");
            mSensorManager.unregisterListener(this);
        }
    }

    /* -------------------------------------------------------------------------------
     * Service
     * ------------------------------------------------------------------------------- */

    /* ---------------------------------------------------------------------------
     * service binder helpers / classes
     * @see: https://developer.android.com/guide/components/bound-services
     * --------------------------------------------------------------------------- */

    /**
     * Class used for the client Binder for {@link SensorManagerService}.
     * Because we know this service always runs in the same processs as it clients, we don't
     * need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SensorManagerService getService() {
            // Return this instance of SensorManagerService so clients can call public methods
            return SensorManagerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "SensorManager:: created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    /* -------------------------------------------------------------------------------
     * SensorEventListener
     * ------------------------------------------------------------------------------- */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, String.format("Sensor ≫ type: {%d} values: %s",
                event.sensor.getType(),
                java.util.Arrays.toString(event.values)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
