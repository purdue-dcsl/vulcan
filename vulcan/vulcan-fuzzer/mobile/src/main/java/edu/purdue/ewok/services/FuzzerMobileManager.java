package edu.purdue.ewok.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.purdue.dagobah.services.FuzzerManager;
import edu.purdue.kylosearch.R;

public class FuzzerMobileManager extends FuzzerManager {

    private static final String TAG = "Kylo/FMMngr";

    public FuzzerMobileManager() {
        // set notification icon
        this.notification_icon = R.drawable.notification_icon;
    }

}
