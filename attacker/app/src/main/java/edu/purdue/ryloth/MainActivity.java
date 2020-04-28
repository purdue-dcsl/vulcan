package edu.purdue.ryloth;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzCommand;
import edu.purdue.dagobah.fuzzer.intent.IntentFuzzer;
import edu.purdue.dagobah.fuzzer.intent.enums.IPCType;
import edu.purdue.dagobah.services.SocketServerManager;
import edu.purdue.ryloth.services.poc.AttackerService;
import edu.purdue.ryloth.services.poc.DefenderService;
import edu.purdue.ryloth.services.sensor.SensorService;

import java.util.ArrayList;


// TODO: 10/3 Do a cleanup to the dagobah library
// Clean unnecessary code from in the fuzzer library. Basically, the scope that
// we need from dagobah is very limited (for our restricted use case).

// TODO: 10/8 Add wear listener to communicate w/mobile app
// Add wear listener to allow communication between the paired devices (mobile
// and wearable).

// TODO: 10/8 Activate sensor
// Activate hardware sensors on wearable device.
// 10/18: done!

// TODO: 10/8 Do basic tests (reboot).
// Do basic tests tyring to trigger system reboots on the wearable device.

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private static final String TAG = "CFTry/MainW";

    private static final String TARGET_APP = "edu.purdue.yavin"; // "com.cardiogram.v1";//"edu.purdue.yavin"; //"edu.purdue.ryloth";
    private static final int REQUEST_PERMISSION_BODY_SENSOR = 101;

    protected WearableActivity activity = this;

    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        public void run() {
            doAsync();
        }
    };

    /**
     * Bind Service
     */
    SocketServerManager mSocketServer;
    boolean mBound = false;

    /* ---------------------------------------------------------------------------
     * Override Methods
     * --------------------------------------------------------------------------- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        // Activity
        RApp.myActivity = this;

        // BODY_SENSOR permission
        this.checkAndRequestSensorBodyPermission();
        // start sensors
        SensorService.startSensor(this);

        // start defense service
        if ( Constants.POC_MODE == 2 ) {
            DefenderService.startService(getApplicationContext());
        }

        // Create an instance of the server
        // Server server = new Server();

        // Start fuzzing after a brief delay (5s)
        handler.postDelayed(runnable, 5000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop sensors
        SensorService.stopSensor(this);

        // Stop defender
        if ( Constants.POC_MODE == 2 ) {
            DefenderService.stopService(getApplicationContext());
        }
    }

    /* ---------------------------------------------------------------------------
     * Async calls
     * --------------------------------------------------------------------------- */
    private void doAsync() {
        AttackerService.startActionFuzz(getApplicationContext(),
                RApp.POC_PARAM_TARGET,
                RApp.POC_PARAM_ACTION,
                RApp.POC_PARAM_STRATEGY,
                RApp.POC_PARAM_SKIP);
    }


//    /* ---------------------------------------------------------------------------
//     * Service Connection
//     * --------------------------------------------------------------------------- */
//
//    private class Server extends SocketServerManager {
//
//        public Server() {
//            super();
//            this.connect();
//        }
//
//        /**
//         * Intent Fuzzing campaign.
//         * @param cmd the {@link FuzzCommand} with the respective instruction to guide the
//         *            fuzzing strategy.
//         */
//        @Override
//        public void doFuzzIntent(FuzzCommand cmd) {
//            String apk = cmd.getTarget();
//            IntentFuzzer fuzzer = IntentFuzzer.getInstance(getActivity());
//            ArrayList<ComponentName> list = fuzzer.getExportedComponents(IPCType.ACTIVITIES, apk);
//            int i=0;
//
//            long start = System.currentTimeMillis();
//            for (ComponentName cmp : list) {
//                if ( i < cmd.getSkip()) {
//                    Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s} * skip",
//                            apk, i++, list.size(), cmp.getClassName()));
//                    continue;
//                } else {
//                    Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s}",
//                            apk, i++, list.size(), cmp.getClassName()));
//                }
//
//                switch (cmd.getStrategy()) {
//                    case FUZZ_STRATEGY_1:
//                        fuzzer.expt1(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_2:
//                        fuzzer.expt2(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_3:
//                        fuzzer.expt3(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_4:
//                        fuzzer.expt4(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_5:
//                        fuzzer.expt5(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_6:
//                        fuzzer.expt6(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_7:
//                        fuzzer.expt7(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_8:
//                        break;
//                    case FUZZ_STRATEGY_9:
//                        break;
//                    case FUZZ_STRATEGY_10:
//                        fuzzer.expt10(cmp, IPCType.ACTIVITIES);
//                        break;
//                    case FUZZ_STRATEGY_WEAR:
//                        fuzzer.expt5(cmp, IPCType.ACTIVITIES);
//                        fuzzer.expt6(cmp, IPCType.ACTIVITIES);
//                        fuzzer.expt7(cmp, IPCType.ACTIVITIES);
//                        break;
//                }
//            }
//            long end = System.currentTimeMillis();
//            Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d done in %ds",
//                    apk, i, list.size(), Math.round((end - start) / 1000F)));
//        }
//    }
//
//    /* ---------------------------------------------------------------------------
//     * Service Connection
//     * --------------------------------------------------------------------------- */
//
//    private ServiceConnection mConnectionSocketServer = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            /*
//            SensorManagerService.LocalBinder binder = (SensorManagerService.LocalBinder) service;
//            mSensorManager = binder.getService();
//            mBound = true;
//            Log.d(TAG, "SensorManagerService bounded!");
//            */
//
//            WearSocketServerManager.LocalBinder binder = (WearSocketServerManager.LocalBinder) service;
//            mSocketServer = binder.getService();
//            mBound = true;
//            Log.d(TAG, "WearSocketServerManager bounded!");
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBound = false;
//        }
//    };

    /* ---------------------------------------------------------------------------
     * Helpers
     * --------------------------------------------------------------------------- */

    WearableActivity getActivity () {
        return this.activity;
    }

    void checkAndRequestSensorBodyPermission() {
        if ( ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED ) {

            // Show an explanatio to the user (non block), then try to request the
            // required permission
            Toast.makeText(this, "BODY_SENSOR permission is required", Toast.LENGTH_LONG);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BODY_SENSORS}, REQUEST_PERMISSION_BODY_SENSOR);

        }
    }

}
