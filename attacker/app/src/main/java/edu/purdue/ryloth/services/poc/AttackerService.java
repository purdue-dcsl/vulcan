package edu.purdue.ryloth.services.poc;

import android.app.Activity;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.purdue.dagobah.common.FuzzCommand;
import edu.purdue.dagobah.fuzzer.intent.IntentFuzzer;
import edu.purdue.dagobah.fuzzer.intent.enums.IPCType;
import edu.purdue.ryloth.RApp;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AttackerService extends IntentService {

    private static final String TAG = "PoC/AttService";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FUZZ = "edu.purdue.ryloth.services.poc.action.FOO";

    private static final String EXTRA_PARAM1 = "edu.purdue.ryloth.services.poc.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "edu.purdue.ryloth.services.poc.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "edu.purdue.ryloth.services.poc.extra.PARAM3";
    private static final String EXTRA_PARAM4 = "edu.purdue.ryloth.services.poc.extra.PARAM4";

    public AttackerService() {
        super("bg-attacker");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFuzz(Context context, String param1, String param2, String param3, int param4) {
        Intent intent = new Intent(context, AttackerService.class);
        intent.setAction(ACTION_FUZZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        intent.putExtra(EXTRA_PARAM3, param3);
        intent.putExtra(EXTRA_PARAM4, param4);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FUZZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                final String param3 = intent.getStringExtra(EXTRA_PARAM3);
                final int param4 = intent.getIntExtra(EXTRA_PARAM4, 0);
                handleActionFuzz(param1, param2, param3, param4);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFuzz(String param1, String param2, String param3, int param4) {
        JSONObject json = new JSONObject();
        try {
            json.put("target", param1);
            json.put("action", param2);
            json.put("strategy", param3);
            json.put("skip", param4);
        } catch (JSONException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }

        FuzzCommand cmd = new FuzzCommand(json);
        doFuzzIntent(cmd);

    }

    /* ---------------------------------------------------------------
     * Attacker Primitives
     * --------------------------------------------------------------- */

    /**
     * Intent Fuzzing campaign.
     * @param cmd the {@link FuzzCommand} with the respective instruction to guide the
     *            fuzzing strategy.
     */
    private void doFuzzIntent(FuzzCommand cmd) {
        String apk = cmd.getTarget();
        IntentFuzzer fuzzer = IntentFuzzer.getInstance(getActivity());
        ArrayList<ComponentName> list = fuzzer.getExportedComponents(IPCType.ACTIVITIES, apk);
        int i=0;

        long start = System.currentTimeMillis();
        for (ComponentName cmp : list) {
            if ( i < cmd.getSkip()) {
                Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s} * skip",
                        apk, i++, list.size(), cmp.getClassName()));
                continue;
            } else {
                Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d {%s}",
                        apk, i++, list.size(), cmp.getClassName()));
            }

            switch (cmd.getStrategy()) {
                case FUZZ_STRATEGY_1:
                    fuzzer.expt1(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_2:
                    fuzzer.expt2(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_3:
                    fuzzer.expt3(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_4:
                    fuzzer.expt4(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_5:
                    fuzzer.expt5(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_6:
                    fuzzer.expt6(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_7:
                    fuzzer.expt7(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_8:
                    break;
                case FUZZ_STRATEGY_9:
                    break;
                case FUZZ_STRATEGY_10:
                    fuzzer.expt10(cmp, IPCType.ACTIVITIES);
                    break;
                case FUZZ_STRATEGY_WEAR:
                    fuzzer.expt5(cmp, IPCType.ACTIVITIES);
                    fuzzer.expt6(cmp, IPCType.ACTIVITIES);
                    fuzzer.expt7(cmp, IPCType.ACTIVITIES);
                    break;
            }
        }
        long end = System.currentTimeMillis();
        Log.d(TAG, String.format("Fuzzing | %s %2d out of %2d done in %ds",
                apk, i, list.size(), Math.round((end - start) / 1000F)));
    }

    Activity getActivity () {
        return RApp.myActivity;
    }


}
