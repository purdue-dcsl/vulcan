package edu.purdue.ewok;

import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Malicious app try-out
 */
public class MainAttackerWearActivity extends WearableActivity {

    private String TAG = "KyloAttacker";

    // UI
    private Button mBtn;

    // Control
    private final static int INTERVAL = 1000 * 5;   // 5s
    private static boolean killMe = false;

    // Async Task
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                Log.d(TAG, "Searching");
                doSearchAsync();
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_attacker_wear);

        // UI
        init();

        // Enables Always-on
        setAmbientEnabled();
    }

    /**
     * Init UI
     */
    private void init() {
        mBtn = (Button) findViewById(R.id.button);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger an AsyncTask that will query for a list of connected nodes
                // and send a message to each connected node
                Log.d(TAG, "Sith apprentice, let's do some damage!");
                // TODO: enable or disable attack
            }
        });
    }

    /* ---------------------------------------------------------------------------
     * Events
     * --------------------------------------------------------------------------- */

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "Sith apprentice, I am no longer active, you are on your own.");
        killMe = false;
        doDamageAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.removeCallbacks(runnable);
        killMe = true;
        Log.d(TAG, "Sith apprentice, Well Done! Let's wait for more action.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(runnable);
        killMe = true;
        Log.d(TAG, "Sith apprentice, Great Job!");
    }

    /* ---------------------------------------------------------------------------
     * Operations
     * ---------------------------------------------------------------------------
     */

    /**
     * Launch a search in the device by injecting a KEYCODE_SEARCH.
     * This event cannot be invoked in the main thread, it has to be called in a thread
     * (runnable interface) or in a background service.
     */
    private void doSearch()  {

        // (1)
        // This does not work without INJECT_EVENTS permission (not possible to get for non
        // system apps -- apparently).
        // AndroidRuntime:
        //   * java.lang.SecurityException: Injecting to another application requires INJECT_EVENTS permission
        // ---------------------------------------
        // Instrumentation inst = new Instrumentation();
        // inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH);


        // (2)
        // This does not work since I cannot get the View (neither the current one)
        // ----------------------------------------
        // BaseInputConnection input = new BaseInputConnection(getCurrentFocus(), false);
        // input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH));
        // input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SEARCH));

        // (3)
        // This does not work as intended. The app needs to declare an intent-filter to catch the intent
        // ----------------------------------------
        //  Intent intent = new Intent();
        //  intent.setAction(Intent.ACTION_SEARCH);
        //  intent.setClassName("com.yazio.android", "com.yazio.android.wear.MainActivity")
        //          .setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        //  startActivity(intent);

        // (4)
        // This does not work. Do nothing
        // ----------------------------------------
        // boolean dw = dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH));
        // boolean up = dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SEARCH));

        // Log.d(TAG, String.format("Key Down {%b} Key Up {%b}", dw, up));

        // This does not work. Requires INJECT_EVENTS permission (just for system apps).
        // java.lang.SecurityException: Injecting to another application requires INJECT_EVENTS permission
        // -----------------------------------------
        // try {
        //     Runtime.getRuntime().exec("input keyevent 84");
        // } catch (IOException ex) {
        //    Log.e(TAG, Log.getStackTraceString(ex));
        // }

        // (5)
        // This does not work since the ServiceManager, IWindowManager are not reachable from this
        // context without any priviledges.
        // https://www.pocketmagic.net/injecting-events-programatically-on-android/
        // ----------------------------------------
        // IBinder wmbinder = ServiceManager.getService("window");
        // IWindowManager m_WndManager = IWindowManager.Stub.asInterface( wmbinder );
        // // key down
        // m_WndManager.injectKeyEvent( new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A ),true );
        // // key up
        // m_WndManager.injectKeyEvent( new KeyEvent( KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A ),true );

        // (6)
        // This does not work.
        // This is similar to the previous one, with the exception of using classes that are only
        // available with system priviledges.
        // It's possible to create a IBinder using the ServiceManager (protected class), however
        // I did not have luck with the IWindowManagerClass. This class apparently is created in
        // compilation time from the AIDL. Check this:
        // https://stackoverflow.com/questions/44362852/where-is-located-the-source-code-of-iwindowmanager-java
        // ----------------------------------------
        // try {
        //     Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
        //     Class[] cArg = new Class[1];
        //     cArg[0] = String.class;
        //     Method getServiceMethod = serviceManagerClass.getDeclaredMethod("getService", cArg);
        //     getServiceMethod.setAccessible(true);
        //     IBinder wmbinder = (IBinder) getServiceMethod.invoke(null, Context.WINDOW_SERVICE);
        //
        //     // The following section does not work at all.
        //     Class<?> iWindowManagerClass = Class.forName("android.view.IWindowManager.Stub");
        //     Class[] cArg2 = new Class[1];
        //     cArg[0] = IBinder.class;
        //     Method getServiceMethod2 = serviceManagerClass.getDeclaredMethod("asInterface", cArg2);
        //     getServiceMethod.setAccessible(true);
        //     if ( wmbinder != null)
        //         getServiceMethod.invoke(null, wmbinder);
        //
        //     //IWindowManager m_WndManager = IWindowManager.Stub.asInterface( wmbinder );
        //
        // } catch (Exception ex) {
        //     Log.d(TAG, "Something wrong happened!");
        //     Log.e(TAG, Log.getStackTraceString(ex));
        // }
    }

    /**
     * Launch a search in a new thread (and not in the main application thread).
     */
    private void doSearchAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSearch();
            }
        }).start();
    }

    /**
     * Start the `fun` process.
     */
    private void doDamageAsyncTask() {

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if (killMe)
                    return;
                handler.post(runnable);
            }
        };

        timer.schedule(task, 0, INTERVAL);
    }

}
