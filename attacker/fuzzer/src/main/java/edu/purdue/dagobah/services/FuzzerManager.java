package edu.purdue.dagobah.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import edu.purdue.dagobah.common.FuzzAction;
import edu.purdue.dagobah.fuzzer.NotifFuzzer;
import edu.purdue.dagobah.fuzzer.TestFuzzer;

public abstract class FuzzerManager extends Service {

    private static final String TAG = "Kylo/FMngr";

    protected int notification_icon;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public FuzzerManager() {
    }

    /* ---------------------------------------------------------------------------
     * service binder helpers / classes
     * @see: https://developer.android.com/guide/components/bound-services
     * --------------------------------------------------------------------------- */

    /**
     * Class used for the client Binder for {@link FuzzerManager}.
     * Because we know this service always runs in the same processs as it clients, we don't
     * need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public FuzzerManager getService() {
            // Return this instance of StatefulFuzzer so clients can call public methods
            return FuzzerManager.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // sanity checks
        if (intent == null || intent.getAction() == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        // get action and parameters from the intent
        switch (FuzzAction.fromPath(intent.getAction())) {

            case ACTION_FUZZ_NOTIF_START:
                NotifFuzzer fuzzer = new NotifFuzzer(this.getApplicationContext(), this.notification_icon);
                //fuzzer.createNotif(1,"test", "test");
                fuzzer.fuzzNotif();
                break;

            case ACTION_FUZZ_TEST:
                TestFuzzer.doFuzz();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
