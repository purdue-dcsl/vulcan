package edu.purdue.dagobah.fuzzer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

// FIXME This service class probably is not required.
@Deprecated
public class IntentFuzzer extends Service {

    private static final String TAG = "Kylo/IFuzz";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public IntentFuzzer() {

    }

    /* ---------------------------------------------------------------------------
     * service binder helpers / classes
     * @see: https://developer.android.com/guide/components/bound-services
     * --------------------------------------------------------------------------- */
    public class LocalBinder extends Binder {
        public IntentFuzzer getService() {
            return IntentFuzzer.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
