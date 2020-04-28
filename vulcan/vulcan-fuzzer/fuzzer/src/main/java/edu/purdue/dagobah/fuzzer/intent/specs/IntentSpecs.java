package edu.purdue.dagobah.fuzzer.intent.specs;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.purdue.dagobah.common.FuzzUtils;

import static edu.purdue.dagobah.common.Constants.JSON_SDK_ACTION;
import static edu.purdue.dagobah.common.Constants.JSON_SDK_ACTION_EXTRA_FIELDS;
import static edu.purdue.dagobah.common.Constants.JSON_SDK_DATA;
import static edu.purdue.dagobah.common.Constants.JSON_SDK_EXTRA_FIELD;

/**
 *
 * @see <a href="https://developer.android.com/reference/android/content/Intent.html">Android
 * Developer - API Reference: Intent</a>
 */
public class IntentSpecs {

    private static String TAG = "FUZZ/ISpec";

    /**
     * Basic structs to map the relation between <code>Actions</code>, <code>Category</code>
     * and <code>Extra fields</code>
     */

    static Map<String, String> intentActionCategory = new HashMap<>();
    static Map<String, String> intentActionData = new HashMap<>();
    static List<Action> intentActions = new ArrayList<>();
    static Map<String, List<Extra>> intentActionExtras = new HashMap<>();
    static Map<String, Extra> intentExtraFields = new HashMap<>();

    /**
     * Create an instance of {@link IntentSpecs} and init the mappings with the info based on the
     * Android Specification. the initial data is just a small set for testing purpose.
     */
    IntentSpecs() {

        Extra extraBugReport = new Extra (DataType.TString, Intent.EXTRA_BUG_REPORT);
        Extra extraAssistPackage = new Extra (DataType.TString, Intent.EXTRA_ASSIST_PACKAGE);
        Extra extraAssistContext = new Extra (DataType.TString, Intent.EXTRA_ASSIST_CONTEXT);
        Extra extraAssistReferrer = new Extra (DataType.TString, Intent.EXTRA_REFERRER, false);

        intentActionExtras.put(Intent.ACTION_BUG_REPORT, new ArrayList<Extra>(Arrays.asList(extraBugReport)));
        intentActionExtras.put(Intent.ACTION_ASSIST, new ArrayList<Extra>(Arrays.asList(extraAssistPackage,
                extraAssistContext, extraAssistReferrer)));

    }

    /**
     * Create an instance of {@link IntentSpecs} and init the mappings with the info based on the
     * Android Specification. The initial data is taken from a JSON file.
     * @param in the {@link InputStream} to the JSON file that contains the specification data.
     * @throws IOException if the resource cannot be found.
     * @throws JSONException if there is any problem while trying to unmarshall the data.
     */
    public IntentSpecs (InputStream in) throws IOException, JSONException {

        JSONObject json = new JSONObject(FuzzUtils.readJSONFromStream(in));

        // TODO. 7/14/19 Populate Action & Data structs
        Log.d(TAG, "Loading actions ...");
        JSONArray actions = json.getJSONArray(JSON_SDK_ACTION);
        for (int i=0; i<actions.length(); i++) {
            JSONObject action = actions.getJSONObject(i);
            Log.d(TAG, String.format("%s %s", JSON_SDK_ACTION, action.toString()));
            intentActions.add(new Action (action));
        }

        Log.d(TAG, "Loading data ...");
        JSONArray datas = json.getJSONArray(JSON_SDK_DATA);
        for (int i=0; i<datas.length(); i++) {
            JSONObject data = datas.getJSONObject(i);
            Log.d(TAG, String.format("%s %s", JSON_SDK_DATA, data.toString()));
        }

        Log.d(TAG, "Loading extra fields ...");
        JSONArray extras = json.getJSONArray(JSON_SDK_EXTRA_FIELD);
        for (int i=0; i<extras.length(); i++) {
            JSONObject extra = extras.getJSONObject(i);
            Log.d(TAG, String.format("%s %s", JSON_SDK_EXTRA_FIELD, extra.toString()));

            // creating the extra field with optional as false (this characteristic is attached
            // to the Action / Extra Field relation.
            Extra ex = new Extra(extra.getString("type"),
                    extra.getString("extra_name"),
                    extra.getString("constant_value"),false);
            intentExtraFields.put(extra.getString("extra_name"), ex);
        }

        Log.d(TAG, "Loading mapping between action and extra fields ...");
        JSONArray action_extras = json.getJSONArray(JSON_SDK_ACTION_EXTRA_FIELDS);
        for (int i=0; i<action_extras.length(); i++) {
            JSONObject action_extra = action_extras.getJSONObject(i);
            Log.d(TAG, String.format("%s %s", JSON_SDK_ACTION_EXTRA_FIELDS, action_extra.toString()));

            String action = action_extra.getString("action_name");
            String extra_key = action_extra.getString("extra_name");
            Extra extra = intentExtraFields.get(extra_key);
            extra.setOptional(action_extra.getBoolean("optional"));

            if (intentActionExtras.containsKey(action)) {
                List<Extra> list = intentActionExtras.get(action);
                list.add(extra);
                intentActionExtras.put(action, list);
            } else {
                intentActionExtras.put(action,
                        new ArrayList<Extra>(Arrays.asList(extra)));
            }
        }
    }

    public void printSpecs(boolean details) {

        Log.d(TAG, "== STATS ==");
        Log.d(TAG, String.format("Actions:               %d", intentActions.size()));
        Log.d(TAG, String.format("Extra Fields:          %d", intentExtraFields.size()));
        Log.d(TAG, String.format("Action-Extra Fields:   %d", intentActionExtras.size()));
        Log.d(TAG, "== STATS ==");

        if ( details ) {

            // action - extras
            for (Map.Entry<String, List<Extra>> entry: intentActionExtras.entrySet()) {
                String action = entry.getKey();
                String output = String.format(" === %s === [%d]", action, entry.getValue().size());
                for (Extra extra : entry.getValue()) {
                    output = output.concat(String.format("\n\t%s", extra));
                }
                Log.d(TAG, output);
            }

            // actions
            for (Action action : getActions()) {
                Log.d(TAG, action.toString());
            }

        }

    }

    /**
     * Returns {@code Extra} fields associated to an {@code Action}.
     * @param action the Intent action.
     * @return the {@link List} of {@link Extra} with the corresponding information of the
     * {@code Extra} mapped to the specified {@code Action}.
     */
    public List<Extra> getExtrafromAction(String action) {
        return intentActionExtras.get(action);
    }

    public List<Action> getActions() {
        return FuzzUtils.getActions(intentActions);
    }

}
