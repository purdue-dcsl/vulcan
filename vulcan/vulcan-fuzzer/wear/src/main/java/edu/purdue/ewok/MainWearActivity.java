package edu.purdue.ewok;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import edu.purdue.dagobah.common.FuzzCommand;
import edu.purdue.dagobah.fuzzer.intent.IntentFuzzer;
import edu.purdue.dagobah.fuzzer.intent.enums.IPCType;
import edu.purdue.dagobah.services.SocketServerManager;
import edu.purdue.ewok.services.SensorManagerService;
import edu.purdue.ewok.services.WearSocketServerManager;

public class MainWearActivity extends WearableActivity {

    private TextView mTextView;
    private final String TAG = "KyloMainWear";
    protected WearableActivity activity = this;


    private final String LOWBIT_AMBIENT = "com.google.android.wearable.compat.extra.LOWBIT_AMBIENT";
    private final String BURN_IN_PROTECTION = "com.google.android.wearable.compat.extra.BURN_IN_PROTECTION";

    /**
     * Bind Service
     */
    SensorManagerService mSensorManager;
    SocketServerManager mSocketServer;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        mTextView = (TextView) findViewById(R.id.text);


        // Testing 
//        String apk = "com.google.android.apps.walletnfcrel";
//        IntentFuzzer fuzzer = IntentFuzzer.getInstance(getActivity());
//        ArrayList<ComponentName> list = fuzzer.getExportedComponents(IPCType.ACTIVITIES, apk);
//        int i=0;
//        for (ComponentName cmp : list) {
//            Log.d(TAG, String.format(" Fuzzing | {%d} out of {%d} {%s}",
//                    i++, list.size(), cmp.getClassName()));
//            fuzzer.expt5(cmp, IPCType.ACTIVITIES);
//            fuzzer.expt6(cmp, IPCType.ACTIVITIES);
//            fuzzer.expt7(cmp, IPCType.ACTIVITIES);
//        }


        // Prelim Exp #1
        // exp1IntentWithMismatchTypeExtra();

        // Create an instance of the server
        Server server = new Server();

        // Enables Always-on
        setAmbientEnabled();
        Log.d(TAG, "onCreate");
    }

    /* ---------------------------------------------------------------------------
     * Override methods (life-cycle)
     * --------------------------------------------------------------------------- */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        // Bind to SensorManager service
        // Intent intent = new Intent(this, SensorManagerService.class);
        // bindService(intent, mConnectionSensorManager, Context.BIND_AUTO_CREATE);

        // Bind to SocketServer service
        // Intent intent = new Intent(this, WearSocketServerManager.class);
        // bindService(intent, mConnectionSocketServer, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
    }


    /* ---------------------------------------------------------------------------
     * Helpers
     * --------------------------------------------------------------------------- */
    WearableActivity getActivity () {
        return this.activity;
    }

    /* ---------------------------------------------------------------------------
     * Service Connection
     * --------------------------------------------------------------------------- */

    private class Server extends SocketServerManager {

        public Server() {
            super();
            this.connect();
        }

        /**
         * Intent Fuzzing campaign.
         * @param cmd the {@link FuzzCommand} with the respective instruction to guide the
         *            fuzzing strategy.
         */
        @Override
        public void doFuzzIntent(FuzzCommand cmd) {
            String apk = cmd.getTarget();
            IntentFuzzer fuzzer = IntentFuzzer.getInstance(getActivity());
            ArrayList<ComponentName> list = fuzzer.getExportedComponents(IPCType.ACTIVITIES, apk);
            int i=0;

            long start = System.currentTimeMillis();
            for (ComponentName cmp : list) {
                if ( i < cmd.getSkip()) {
                    Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s} * skip",
                            apk, i++, list.size(), cmp.getClassName()));
                    continue;
                } else {
                    Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s}",
                            apk, i++, list.size(), cmp.getClassName()));
                }

                switch (cmd.getStrategy()) {
                    case FUZZ_STRATEGY_1:
                        fuzzer.expt1(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_2:
                        fuzzer.expt2(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_3:
                        fuzzer.expt3(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_4:
                        fuzzer.expt4(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_5:
                        fuzzer.expt5(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_6:
                        fuzzer.expt6(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_7:
                        fuzzer.expt7(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_8:
                        break;
                    case FUZZ_STRATEGY_9:
                        break;
                    case FUZZ_STRATEGY_10:
                        fuzzer.expt10(cmp, IPCType.ACTIVITIES);
                        break;
                    case FUZZ_STRATEGY_WEAR:
                        fuzzer.expt5(cmp, IPCType.ACTIVITIES);
                        fuzzer.expt6(cmp, IPCType.ACTIVITIES);
                        fuzzer.expt7(cmp, IPCType.ACTIVITIES);
                        break;
                }
            }
            long end = System.currentTimeMillis();
            Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d done in %ds",
                    apk, i, list.size(), Math.round((end - start) / 1000F)));
        }
    }

    /* ---------------------------------------------------------------------------
     * Service Connection
     * --------------------------------------------------------------------------- */

    private ServiceConnection mConnectionSocketServer = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            /*
            SensorManagerService.LocalBinder binder = (SensorManagerService.LocalBinder) service;
            mSensorManager = binder.getService();
            mBound = true;
            Log.d(TAG, "SensorManagerService bounded!");
            */

            WearSocketServerManager.LocalBinder binder = (WearSocketServerManager.LocalBinder) service;
            mSocketServer = binder.getService();
            mBound = true;
            Log.d(TAG, "WearSocketServerManager bounded!");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    /* ---------------------------------------------------------------------------
     * Preliminary Experiments
     * --------------------------------------------------------------------------- */

    /**
     * Date:    7/7/19
     * Purpose: Check the effect of sending an intent with an Extra field with a diff
     *          type of the one expected. For example, send an intent with a EXTRA_FIELD of
     *          type <code>int</code> when the target is expecting a {@link java.lang.String}
     *
     *          The target class is {@link DummyActivity}
     */
    private void exp1IntentWithMismatchTypeExtra() {
        Intent intent = new Intent(MainWearActivity.this, DummyActivity.class);
        intent.setAction("CUSTOM_ACTION");

        intent.putExtra("EXTRA_FIELD", 1);
        Log.d(TAG, "== Sending Intent ==");
        startActivity(intent);
    }


}
