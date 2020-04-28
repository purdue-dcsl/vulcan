package edu.purdue.ryloth.services.comm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.ryloth.RApp;
import edu.purdue.ryloth.dmz.DUtils;
import edu.purdue.ryloth.services.sensor.SensorMan;

/**
 *
 *  TODO 2/9/20. Fix this class
 *  TODO 2/9/20. Create helper class to send data to mobile, listener to receive data from mobile (most probable won't be used)
 *  TODO 2/9/20. Create helper class to create chunks of fixed data (according to a param) that are going to be sent to the mobile
 */
public class CommService extends IntentService {

    private static final String TAG = "PoC/CommService";

    // frequency for checking the buffer (millisecs)
    private final static int INTERVAL = 1000;

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_START_COMM = "edu.purdue.ryloth.services.poc.action.START_COMM";

    private static Context mContext;

    public CommService() {
        // used to name the worker thread
        super("bg-comm");
    }

    public static void startActionStart(Context context, String param1, String param2, String param3, int param4) {
        mContext = context;
        Intent intent = new Intent(context, CommService.class);
        intent.setAction(ACTION_START_COMM);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if ( intent != null ) {
            final String action = intent.getAction();
            if (ACTION_START_COMM.equals(action)) {
                handleActionStart();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStart() {

        // setup Data API
        mGoogleApiclient = new GoogleApiClient
                .Builder(mContext)
                .addApi(Wearable.API).build();
        mExecService = Executors.newCachedThreadPool();

        doTask();
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
            sendData(intent);
            DUtils.log(String.format("Sent intent: %s bufferSize %d", intent, Constants.gIntentBuffer.size()));

        }
    }

    private void sendData(Intent intent){

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
     * Communication Primitives
     * --------------------------------------------------------------- */

    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    private GoogleApiClient mGoogleApiclient;
    private ExecutorService mExecService;


    public void sendDataAsync() {
        // Log.d(TAG, "#MW sendSensorData ...");

        // if the interference is enabled, send the sync data in background
        if ( Constants.POC_PARAM_COMM_ACTIVATED ) {

            mExecService.submit(new Runnable() {
                @Override
                public void run() {
                    sendData();
                }
            });

        }
    }


    private void sendData() {
        // Log.d(TAG, "#MW sendData: " + line);

        // Create data item
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/sensors/");
        putDataMapRequest.getDataMap().putByteArray("block", CommMan.getChunk());

        // Put data item in background
        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        if ( Constants.POC_PARAM_SYNC_ENABLED ) {
            // Synchronization between wearable and mobile using Wearable.DataApi
            send(request);
        }

    }

    private boolean validateConnection() {
        if (mGoogleApiclient.isConnected()) return true;

        ConnectionResult result = mGoogleApiclient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    private void send(PutDataRequest data) {
        // Log.d(TAG, "send: " + data);
        if (validateConnection()) {
            // Put data item
            Wearable.DataApi.putDataItem(mGoogleApiclient, data).setResultCallback(
                    new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "#MW Failed to send data item: " + dataItemResult.getStatus().getStatusCode());
                            } else {
                                Log.d(TAG, "#MW Succesfullly sent data item: " + dataItemResult.getStatus().isSuccess());
                            }
                        }
                    });
        }
    }

    /* ---------------------------------------------------------------
     * Override Methods
     * --------------------------------------------------------------- */

    @Override
    public void onCreate() {
        super.onCreate();

        sService = this;
        Log.d(TAG, "Enters onCreate");

        this.mContext = context;

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




}

