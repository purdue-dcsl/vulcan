package edu.purdue.dagobah.fuzzer.intent.specs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to represent an <code>action</code> field on a Intent.
 */
public class Action {

    private static final int MAX_API_LEVEL = 99;

    String name;
    String constant_value;
    int api_level;
    int api_level_deprecated;
    boolean broadcast;
    boolean activity;
    boolean system_protected;
    boolean wearable_specific;
    String mime_type;

    /* ---------------------------------------------------------------------------
     * Constructors
     * --------------------------------------------------------------------------- */

    /**
     * Create an instance of {@link Action} from a {@link JSONObject} with the respective
     * definition.
     * @param json the {@link JSONObject} with the corresponding data
     * @throws JSONException if there is an error while trying to create the instance of the
     * {@link Action} object.
     */
    public Action (JSONObject json) throws JSONException {

        this.name = json.getString("action_name");
        this.constant_value = json.getString("constant_value");
        this.api_level = json.getInt("api_level");

        if ( json.has("api_level_deprecated") )
            this.api_level_deprecated = json.getInt("api_level_deprecated");
        else
            this.api_level_deprecated = MAX_API_LEVEL;

        this.broadcast = json.getBoolean("broadcast");
        this.activity = json.getBoolean("activity");
        this.system_protected = json.getBoolean("protected");
        this.wearable_specific = json.getBoolean("wearable_specific");
        if ("".equals(json.getString("mime_type")))
            this.mime_type = "*/*";
        else
            this.mime_type = json.getString("mime_type");
    }


    /* ---------------------------------------------------------------------------
     * Accessors
     * --------------------------------------------------------------------------- */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConstantValue() {
        return constant_value;
    }

    public void setConstantValue(String constant_value) {
        this.constant_value = constant_value;
    }

    public int getApiLevel() {
        return api_level;
    }

    public void setApiLevel(int api_level) {
        this.api_level = api_level;
    }

    public int getApiLevelDeprecated() {
        return api_level_deprecated;
    }

    public void setApiLevelDeprecated(int api_level_deprecated) {
        this.api_level_deprecated = api_level_deprecated;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isActivity() {
        return activity;
    }

    public void setActivity(boolean activity) {
        this.activity = activity;
    }

    public boolean isSystemProtected() {
        return system_protected;
    }

    public void setSystemProtected(boolean system_protected) {
        this.system_protected = system_protected;
    }
    public String getMimeType(){
        return this.mime_type;
    }
    public boolean isWearableSpecific() {
        return wearable_specific;
    }

    public void setWearableSpecific(boolean wearable_specific) {
        this.wearable_specific = wearable_specific;
    }

    /* ---------------------------------------------------------------------------
     * Helpers
     * --------------------------------------------------------------------------- */

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", constant_value='" + constant_value + '\'' +
                ", api_level=" + api_level +
                ", api_level_deprecated=" + api_level_deprecated +
                '}';
    }

    public boolean isValid() {
        return ( !this.system_protected &&
                this.api_level_deprecated > android.os.Build.VERSION.SDK_INT );
    }

}
