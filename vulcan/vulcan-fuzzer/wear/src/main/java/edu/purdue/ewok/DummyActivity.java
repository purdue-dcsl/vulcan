package edu.purdue.ewok;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * Dummy Activity.
 * This activity does not have any useful code for the Project. The class is mainly used to
 * do some preliminary experiments or test some features needed in the final Project.
 *
 * @author ebarsallo
 */
public class DummyActivity extends WearableActivity {

    private static final String TAG = "DummyWear";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        // ebarsallo
        // exp1IntentWithMismatchTypeExtra(getIntent(), "int");
    }


    /**
     * Date:    7/7/19
     * Purpose: Check the effect of sending an intent with an Extra field with a diff
     *          type of the one expected. For example, send an intent with a EXTRA_FIELD of
     *          type <code>int</code>  when the target is expecting a {@link String}
     *
     *          The sender class is {@link MainWearActivity}
     *
     * @param intent the received {@link Intent}
     */
    private void exp1IntentWithMismatchTypeExtrawithBundle(Intent intent, String type) {
        Log.d(TAG, "== exp1IntentWithMismatchTypeExtrawithBundle | Receiving Intent ==");
        Bundle extras = intent.getExtras();

        if ( extras != null ) {
            // (1) OK    => send {int} receive {int}
            // Log.d(TAG, "I got an extra! " + extras.getInt("EXTRA_FIELD"));
            // (2) Error => send {int} receive (String)
            //     ex    =>  W/Bundle: Key EXTRA_FIELD expected String but value was a java.lang.Integer.  The default value <null> was returned.
            //               W/Bundle: Attempt to cast generated internal exception:
            //               java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
            Log.d(TAG, "I got an extra! " + extras.getString("EXTRA_FIELD"));
        }
    }

    /**
     * Date:    7/7/19
     * Purpose: Check the effect of sending an intent with an Extra field with a diff
     *          type of the one expected. For example, send an intent with a EXTRA_FIELD of
     *          type <code>int</code> when the target is expecting a {@link String}
     *
     *          The sender class is {@link MainWearActivity}
     *
     * @param intent the received {@link Intent}
     */
    private void exp1IntentWithMismatchTypeExtra(Intent intent, String type) {
        Log.d(TAG, "== exp1IntentWithMismatchTypeExtra | Receiving Intent ==");
        Bundle extras = intent.getExtras();

        // Hypothesis:
        // Most of the data types have a default value, hence there's the possibility that the
        // missmatch in the casting does not have any effect.
        // Basically, for types where it's possible to set a default value, is useless to try to
        // send a different type, since their will be no effect.

        // Definitions
        //   1. [type-1]: the data type of the EXTRA_FIELD (expected).
        //   2. [type-2]: the data type send by the fuzzer.
        // Possible Scenarios:
        //   1. If [type-1] has a default value in the getter, try with [type-1] == [type-2]
        //      and extreme values.
        //   2. If [type-2] does not have a default value in the getter, try with a [type-1] !=
        //      [type-2]

        if ( intent != null ) {

            // ------------------------------------------------
            // Numeric
            // ------------------------------------------------

            // (1) OK    => send {int} receive {int}
            Log.d(TAG, "I got an extra (int/int)! " + intent.getIntExtra("EXTRA_FIELD", 0));

            // (2) OK    => send {int} receive {default}
            Log.d(TAG, "I got an extra (" + type + "/long)! " + intent.getLongExtra("EXTRA_FIELD", 0));

            // (3) OK    => send {int} receive {default}
            Log.d(TAG, "I got an extra (" + type + "/double)! " + intent.getDoubleExtra("EXTRA_FIELD", 0.0));

            // (4) OK    => send {int} receive {default}
            Log.d(TAG, "I got an extra (" + type + "/float)! " + intent.getFloatExtra("EXTRA_FIELD", 0.0f));

            // ------------------------------------------------
            // Alphanumeric
            // ------------------------------------------------

            // (5) OK    => send {int} receive {default}
            Log.d(TAG, "I got an extra (" + type + "/char)! " + intent.getCharExtra("EXTRA_FIELD", 'x'));

            // (6) !!!!  => send {empty} receive (null)
            //     ex    =>  W/Bundle: Key EXTRA_FIELD expected String but value was a java.lang.Integer.  The default value <null> was returned.
            //               W/Bundle: Attempt to cast generated internal exception:
            //               java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
            Log.d(TAG, "I got an extra (" + type + "/String)! " + intent.getStringExtra("EXTRA_FIELD"));
            // (7)       => send {int} receive {null}
            Log.d(TAG, "I got an extra (" + type + "/CharSequence)! " + intent.getCharSequenceExtra("EXTRA_FIELD"));

            // ------------------------------------------------
            // Others
            // ------------------------------------------------
            // (8)-(10): => send {int} receive {null}
            Log.d(TAG, "I got an extra (" + type + "/boolean)! " + intent.getBooleanExtra("EXTRA_FIELD", false));
            Log.d(TAG, "I got an extra (" + type + "/Bundle)! " + intent.getBundleExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/Parcelable)! " + intent.getParcelableExtra("EXTRA_FIELD"));

            // ------------------------------------------------
            // Array - Numeric
            // ------------------------------------------------

            // (1) OK    => send {int} receive {int}
            Log.d(TAG, "I got an extra (" + type + "/int Array)! " + intent.getIntArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/long Array)! " + intent.getLongArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/double Array)! " + intent.getDoubleArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/float Array)! " + intent.getFloatArrayExtra("EXTRA_FIELD"));

            // ------------------------------------------------
            // Array - Alphanumeric
            // ------------------------------------------------
            // (1)-(9)   => send {int} receive {default}
            Log.d(TAG, "I got an extra (" + type + "/char Array)! " + intent.getCharArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/String Array)! " + intent.getStringArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/CharSequence Array)! " + intent.getCharSequenceArrayExtra("EXTRA_FIELD"));

            // ------------------------------------------------
            // Array - Others
            // ------------------------------------------------
            Log.d(TAG, "I got an extra (" + type + "/boolean Array)! " + intent.getBooleanArrayExtra("EXTRA_FIELD"));
            Log.d(TAG, "I got an extra (" + type + "/Parcelable Array)! " + intent.getParcelableArrayExtra("EXTRA_FIELD"));



        }
    }
}
