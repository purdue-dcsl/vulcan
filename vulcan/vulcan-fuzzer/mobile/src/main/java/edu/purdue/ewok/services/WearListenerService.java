package edu.purdue.ewok.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 *
 */
public class WearListenerService extends WearableListenerService {

    private static final String TAG = "KyloWLS";

    // Paths
    private static final String PATH = "/path";

    public WearListenerService() {
    }

    /* ---------------------------------------------------------------------------
    * communications primitives
    * --------------------------------------------------------------------------- */

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d(TAG, "OnDataChanged");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, String.format("OnMessageReceived {%s}", messageEvent.getPath()));
    }

}
