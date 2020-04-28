package edu.purdue.ewok;

import android.app.Application;
import android.content.Context;

/**
 * WearApplication
 */
public class WearApplication extends Application {
    private String TAG = "KyloApp";
    private static Context context;

    /**
     * Returns the application context (stored previously in a static variable).
     * @return the application's {@link Context}.
     */
    public static Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
    }
}
