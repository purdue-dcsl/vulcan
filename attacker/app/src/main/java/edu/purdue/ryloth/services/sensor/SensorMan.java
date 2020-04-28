package edu.purdue.ryloth.services.sensor;

import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

import edu.purdue.dagobah.common.Constants;

/**
 * A class that handles the constants for the experiments that measures the relation between
 * sensors activity and the system reboots in the target device.
 */
public class SensorMan {

    /** sensor lists */
    // AOSP
    public static final int SENSOR_TYPE_1                       = 1;       // ACCELEROMETER
    public static final int SENSOR_TYPE_2                       = 2;       // MAGNETIC FIELD
    public static final int SENSOR_TYPE_3                       = 3;       // ORIENTATION
    public static final int SENSOR_TYPE_4                       = 4;       // GYROSCOPE
    public static final int SENSOR_TYPE_5                       = 5;       // LIGHT

    public static final int SENSOR_TYPE_6                       = 6;       // PRESSURE
    public static final int SENSOR_TYPE_8                       = 8;       // PROXIMITY
    public static final int SENSOR_TYPE_9                       = 9;       // GRAVITY
    public static final int SENSOR_TYPE_10                      = 10;      // LINEAR ACCELERATION
    public static final int SENSOR_TYPE_11                      = 11;      // ROTATION VECTOR
    public static final int SENSOR_TYPE_14                      = 14;      // MAGNETIC FIELD UNCALIBRATED
    public static final int SENSOR_TYPE_17                      = 17;      // SIGNIFICANT MOTION
    public static final int SENSOR_TYPE_18                      = 18;      // STEP DETECTOR
    public static final int SENSOR_TYPE_19                      = 19;      // STEP COUNTER
    public static final int SENSOR_TYPE_20                      = 20;      // GEO MAGNETIC ROTATION
    public static final int SENSOR_TYPE_21                      = 21;      // HEART RATE
    public static final int SENSOR_TYPE_26                      = 26;      // WRIST TILT
    public static final int SENSOR_TYPE_29                      = 29;      // STATIONARY DETECT
    public static final int SENSOR_TYPE_30                      = 30;      // MOTION DETECT
    public static final int SENSOR_TYPE_34                      = 34;      // OFF BODY DETECTOR
    public static final int SENSOR_TYPE_65572                   = 65572;   // PPG
    public static final int SENSOR_TYPE_65536                   = 65536;   // CUSTOM

    /** sensor scenarios */
    public static final int[] SAPP_SENSORS_SCENARIO_1 = {21};
    public static final int[] SAPP_SENSORS_SCENARIO_2 = {21, 65572};
    public static final int[] SAPP_SENSORS_SCENARIO_3 = {21, 65572, 26};
    public static final int[] SAPP_SENSORS_SCENARIO_4 = {21, 65572, 26, 30};
    public static final int[] SAPP_SENSORS_SCENARIO_5 = {21, 65572, 26, 30, 14};
    public static final int[] SAPP_SENSORS_SCENARIO_10 = {21, 65572, 26, 1, 14, 2, 3, 4, 5, 6};
    public static final int[] SAPP_SENSORS_SCENARIO_15 = {21, 65572, 26, 1, 14, 2, 3, 4, 5, 6, 8, 9, 10, 11, 17};
    public static final int[] SAPP_SENSORS_SCENARIO_ALL = {1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 14, 17,
            18, 19, 20, 21, 26, 29, 30, 34, 65572, 65536};

    public static final int[][] SAPP_SENSORS_SCENARIOS = {
            SAPP_SENSORS_SCENARIO_1,
            SAPP_SENSORS_SCENARIO_2,
            SAPP_SENSORS_SCENARIO_3,
            SAPP_SENSORS_SCENARIO_4,
            SAPP_SENSORS_SCENARIO_5,
            SAPP_SENSORS_SCENARIO_10,
            SAPP_SENSORS_SCENARIO_15,
            SAPP_SENSORS_SCENARIO_ALL
    };

    public static int[] SAPP_SENSORS_SCENARIO;

    public static Map<Integer, Boolean> gTASensors;
    public static Map<Integer, Sensor> gSensors;

    static {
        SAPP_SENSORS_SCENARIO = getSensorScenario(Constants.POC_PARAM_SENSOR_ACTIVATED);
        gTASensors = setSensors(SAPP_SENSORS_SCENARIO);
        gSensors = new HashMap<>();
        for (Map.Entry e : gTASensors.entrySet()) {
            gSensors.put((Integer)e.getKey(), null);
            if ( ((Boolean) e.getValue()).booleanValue() ) Constants.POC_REAL_SENSOR_ACTIVATED++;
        }
    }


    /**
     * Return an array with the sensors handler that are required to be activated for the experiment
     * run.
     *
     * @param num the number of sensors desired to be activated.
     * @return the array with the sensors handler that are required to be activated.
     */
    public static int[] getSensorScenario(int num) {

        int chosen = 0;

        for (int i=0; i<SAPP_SENSORS_SCENARIOS.length; i++) {
            if ( SAPP_SENSORS_SCENARIOS[i].length < num )
                chosen = i;
            else
                break;
        }

        if ( chosen == 0)
            return null;
        else
            return SAPP_SENSORS_SCENARIOS[chosen];
    }


    /**
     * Set the status of the sensors either to <code>true</code> or <code>false</code>.
     *
     * @param sensors the array with the sensors handlers that need to be set to true.
     * @return the array with final status for each sensor handler. Either <code>true</code> if
     * the sensors need to be activated or <code>false</code> otherwise.
     */
    public static Map<Integer, Boolean> setSensors(int[] sensors) {

        Map<Integer, Boolean> array = new HashMap<>();

        array.put (SENSOR_TYPE_1 , false);
        array.put (SENSOR_TYPE_2 , false);
        array.put (SENSOR_TYPE_3 , false);
        array.put (SENSOR_TYPE_4 , false);
        array.put (SENSOR_TYPE_5 , false);
        array.put (SENSOR_TYPE_6 , false);
        array.put (SENSOR_TYPE_8 , false);
        array.put (SENSOR_TYPE_9 , false);
        array.put (SENSOR_TYPE_10, false);
        array.put (SENSOR_TYPE_11, false);
        array.put (SENSOR_TYPE_14, false);
        array.put (SENSOR_TYPE_17, false);
        array.put (SENSOR_TYPE_18, false);
        array.put (SENSOR_TYPE_19, false);
        array.put (SENSOR_TYPE_20, false);
        array.put (SENSOR_TYPE_21, false);
        array.put (SENSOR_TYPE_26, false);
        array.put (SENSOR_TYPE_29, false);
        array.put (SENSOR_TYPE_30, false);
        array.put (SENSOR_TYPE_34, false);
        array.put (SENSOR_TYPE_65572, false);
        array.put (SENSOR_TYPE_65536, false);

        if ( sensors != null)
            for (int num : sensors) {
                array.put(num, true);
            }

        return array;

    }

}