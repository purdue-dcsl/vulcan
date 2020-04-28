package edu.purdue.ewok.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import edu.purdue.dagobah.common.FuzzCommand;
import edu.purdue.dagobah.services.SocketServerManager;

public class WearSocketServerManager extends SocketServerManager {
    private static final String TAG = "Kylo/WearSS";


    public WearSocketServerManager() {
        super();
        this.connect();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disconnect();
    }

    @Override
    public void doFuzzIntent(FuzzCommand cmd) {
        Log.e(TAG, "Notification Fuzzer: Not implemented");
    }

    @Override
    public void doFuzzNotif(FuzzCommand cmd) {
        Log.e(TAG, "Notification Fuzzer: Not implemented");
    }

    @Override
    public void doFuzzTest(FuzzCommand cmd) {
        Log.e(TAG, "Notification Fuzzer: Not implemented");
    }


}
