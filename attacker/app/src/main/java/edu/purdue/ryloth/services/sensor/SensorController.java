package edu.purdue.ryloth.services.sensor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import edu.purdue.dagobah.common.FuzzUtils;

import static edu.purdue.dagobah.common.Constants.SENSOR_DELAY;
import static edu.purdue.ryloth.services.sensor.SensorMan.gSensors;
import static edu.purdue.ryloth.services.sensor.SensorMan.gTASensors;

public class SensorController implements SensorEventListener {

    private static final String TAG = "ryloth/sensor-ct";
    private static final String PERMISSION_BODY_SENSORS = Manifest.permission.BODY_SENSORS;

    private static int sSensorListenerCount = 0;

    private final Context mContext;
    private final Listener mListener;

    private SensorManager mSensorManager;

    private boolean mSensorOn;
    private boolean mSensorPermission;


    SensorController(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        Log.d(TAG, "[1] Creating instance of SensorController");

        this.mSensorPermission = isPermissionGranted(context);
        if ( this.mSensorPermission ) {
            // Note. API 29
            // The IDE is forcing to use the constant Context.SENSOR_SERVICE instead of the actual
            // value "sensor"
            this.mSensorManager = (SensorManager)
                    context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            // display sensors
            showSensors();
        } else {
            Log.e(TAG, String.format("[1] %s is not granted", PERMISSION_BODY_SENSORS));
            mSensorManager = null;
        }
    }

    /* ---------------------------------------------------------------
     * Primitives
     * --------------------------------------------------------------- */

    public synchronized void registerSensorRequest(Context context) {

        Log.d(TAG, "[2] Registering Sensor ");

        if ( !this.mSensorPermission ) {
            Log.e(TAG, String.format("[2] %s is not granted", PERMISSION_BODY_SENSORS));
            return;
        }

        if ( mSensorManager == null ) {
            // Note. API 29
            // The IDE is forcing to use the constant Context.SENSOR_SERVICE instead of the actual
            // value "sensor"
            this.mSensorManager = (SensorManager)
                    getContext().getSystemService(Context.SENSOR_SERVICE);
        }

        // register listener for sensors
        this.registerSensorListeners();

    }

    public synchronized void unregisterSensorRequest() {
        if ( this.mSensorOn ) {
            Log.d(TAG, "Stopping sensors");
            this.mSensorManager.unregisterListener(this);
        }
    }

    /* ---------------------------------------------------------------
     * Interface
     * --------------------------------------------------------------- */

    public interface Listener {
        void onBeat(SensorEvent event);
        void onSensorError(String error);
        void onSensorStopped();
    }

    /* ---------------------------------------------------------------
     * Helpers
     * --------------------------------------------------------------- */
    private static boolean isPermissionGranted(Context context) {
        // Note. API 29
        // The IDE is forcing to specific values in the condition, that why
        // instead of 0, we have to use PackageManager.PERMISSION_GRANTED
        return context.checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED;
    }

    private Context getContext() {
        return this.mContext.getApplicationContext();
    }

    /* ---------------------------------------------------------------
     * Override Methods
     * --------------------------------------------------------------- */

    @Override
    public void onSensorChanged(SensorEvent event) {
        // FuzzUtils.log("sensor changed");
        this.mListener.onBeat(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        FuzzUtils.log(String.format("accuracy changes [%d]", accuracy));
    }

    /* ---------------------------------------------------------------
     * Sensor specific operations
     * --------------------------------------------------------------- */

    public void registerSensorListeners() {

        Log.i(TAG, String.format("----------------------------------------------------------"));

        // instantiate sensors if they are enable
        for (Map.Entry entry : gTASensors.entrySet()) {

            boolean enabled = ((Boolean)entry.getValue()).booleanValue();

            Integer type = (Integer)entry.getKey();
            if ( enabled ) {

                // get sensor associated
                Sensor xxx = this.mSensorManager.getDefaultSensor(type);
                gSensors.put(type, xxx);

                // enable listener
                if ( xxx != null ) {
                    if ( this.mSensorManager.registerListener(this, xxx, SENSOR_DELAY) ) {
                        sSensorListenerCount++;
                        this.mSensorOn = true;
                        Log.i(TAG, String.format("sensor {%d} registered", type));
                    } else {
                        this.mSensorManager.unregisterListener(this);
                        Log.i(TAG, String.format("sensor {%d} not registered because an error", type));
                    }
                } else {
                    Log.i(TAG, String.format("sensor {%d} not found", type));
                }
            } else {
                // Log.i(TAG, String.format("sensor {%d} not active", type));
            }

        }
        Log.i(TAG, String.format("----------------------------------------------------------"));
    }

    public void showSensors() {
        // Retrieving all sensors on wearable device
        Log.d(TAG, "Sensor List");
        //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : deviceSensors){

            Log.d(TAG, String.format("Sensors: %s wake-up=%s, handle=%d",
                    sensor,
                    ((sensor.isWakeUpSensor()) ? "{yes}" : "{no}"),
                    getHandle(sensor)));
        }
    }

    /**
     * Returns the handle for an specific {@link Sensor} type using Java reflection.
     *
     * @param sensor the hardware/software {@link Sensor} class.
     * @return the sensor handle.
     */
    private int getHandle(Sensor sensor) {
        Class c;
        Method m;

        try {
            c = Class.forName("android.hardware.Sensor");
            m = c.getMethod("getHandle");

            Integer handle = (Integer) m.invoke(sensor);
            return handle.intValue();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return 0;
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }

    }

}
