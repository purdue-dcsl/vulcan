package edu.purdue.ryloth;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzUtils;
import edu.purdue.ryloth.dmz.DUtils;

public class RApp extends Application {

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Thread.UncaughtExceptionHandler customUEH = new myUEH();

    /** poc/defender */
    public static Activity myActivity = null;

    /** param/test */
    public static final String POC_PARAM_TARGET = "com.cardiogram.v1";
    public static final String POC_PARAM_ACTION = "vnd.dcsl.action.FUZZ_INTENT_START";
    public static final String POC_PARAM_STRATEGY = "strategy/1";
    public static final int POC_PARAM_SKIP = 0;

    //SharedPreferences gPrefs = getSharedPreferences(DUtils.SHARE_PREF,
    //        Context.MODE_PRIVATE);

    public class myUEH implements Thread.UncaughtExceptionHandler {

        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         * <p>Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         *
         * @param thread the thread
         * @param ex the exception
         */
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            FuzzUtils.log(String.format("-- fatal error: ex --"));
            FuzzUtils.log(Log.getStackTraceString(ex));

            defaultUEH.uncaughtException(thread, ex);
        }
    }


    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * <p>Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.</p>
     *
     * <p>If you override this method, be sure to call {@code super.onCreate()}.</p>
     *
     * <p class="note">Be aware that direct boot may also affect callback order on
     * Android {@link Build.VERSION_CODES#N} and later devices.
     * Until the user unlocks the device, only direct boot aware components are
     * allowed to run. You should consider that all direct boot unaware
     * components, including such {@link ContentProvider}, are
     * disabled until user unlock happens, especially when component callback
     * order matters.</p>
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // replace default uncaught exception handler
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(customUEH);

    }
}
