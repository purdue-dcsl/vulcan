package edu.purdue.ryloth.dmz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;
import java.util.Map;

import edu.purdue.dagobah.common.Constants;

import static edu.purdue.dagobah.common.Constants.POC_PARAM_INTENT_MAX;
import static edu.purdue.dagobah.common.Constants.POC_PARAM_INTENT_WINDOW;
import static edu.purdue.dagobah.common.Constants.POC_PARAM_TIMEOUT;
import static edu.purdue.dagobah.common.Constants.gIntentBuffer;
import static edu.purdue.dagobah.common.Constants.gIntentCount;
import static edu.purdue.dagobah.common.Constants.gTimeout;

public class DUtils {

    private static final String TAG = "PoC/Attacker-DUs";


    /* ---------------------------------------------------------------------------
     * helpers
     * ---------------------------------------------------------------------------
     */

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }
    }

    /* ---------------------------------------------------------------------------
     * logger
     * ---------------------------------------------------------------------------
     */

    public static void log(String line) {
        if ( Constants.DEBUG_MODE )
            Log.i(TAG, line);
    }

    public static void logp(String tag, Intent intent) {
        logp(tag, intent, "");
    }

    public static void logp(String tag, Intent intent, String msg) {
        String TAG = String.format("%s%s-%05d", Constants.POC_TAG, tag,
                intent.getIntExtra(Constants.POC_TAG_EXTRAID, 0));
        Log.i(TAG, msg);
    }

    /* ---------------------------------------------------------------------------
     * MODE = 2
     * internal buffer for intents
     * ---------------------------------------------------------------------------
     */

    /**
     * Add only if buffer has not reached its maximum: buffer.size() < threshold
     * @param buffer
     * @param intent
     * @return
     */
    public static List<Intent> addIntentsToBuffer(List<Intent> buffer, Intent intent) {
        buffer.add(intent);
        DUtils.logp(Constants.POC_TAG_INTENT_ADD_BUF, intent,
                String.format("buffer.size (%d)", gIntentBuffer.size()));
        return buffer;
    }

    /**
     * Add only if buffer has not reached its maximum: buffer.size() < threshold
     * @param intent
     * @return
     */
    public static List<Intent> addIntentsToBuffer(Intent intent) {
        return addIntentsToBuffer(Constants.gIntentBuffer, intent);
    }

    /* ---------------------------------------------------------------------------
     * MODE = 3
     * dynamic threshold
     * ---------------------------------------------------------------------------
     */
    public static void checkThreshold() throws ThresholdSecurityException {

        long currentMins = System.currentTimeMillis() / ( 1000 * 60);

        // check if the app is in timeout
        if ( gTimeout > 0 ) {
            if ( gTimeout < currentMins * 60)
                throw new ThresholdSecurityException(
                        String.format("app still in timeout for %ds", gTimeout - currentMins * 60));
            else
                gTimeout = 0;
        }


        // check if the threshold have not being raised
        gIntentCount.put(currentMins,
                gIntentCount.getOrDefault(currentMins, 0) + 1) ;

        int val = 0;
        for (Map.Entry<Long,Integer> entry : gIntentCount.entrySet()) {
            if ( (entry.getKey() >= currentMins - POC_PARAM_INTENT_WINDOW) &&
                 (entry.getKey() < currentMins + 1 )) {
                val += entry.getValue();
            }
        }

        if ( val > POC_PARAM_INTENT_MAX ) {
            gTimeout = currentMins * 60 + POC_PARAM_TIMEOUT;
            throw new ThresholdSecurityException(String.format("%d > %d", val, POC_PARAM_INTENT_MAX));
        }

    }

}
