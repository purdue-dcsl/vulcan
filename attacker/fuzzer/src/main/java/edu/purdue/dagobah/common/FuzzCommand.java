package edu.purdue.dagobah.common;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

public class FuzzCommand {

    String target;
    FuzzAction action;
    FuzzStrategy strategy;
    int skip;


    /**
     * Create an instance of {@link FuzzCommand} from a {@link JSONObject} with the respective
     * information
     * @param json the {@link JSONObject}.
     */
    public FuzzCommand(JSONObject json) {
        try {
            this.target = json.getString("target");
            this.action = FuzzAction.fromPath(json.getString("action"));
            this.strategy = FuzzStrategy.fromPath(json.getString("strategy"));
            this.skip = (json.has("skip")) ? json.getInt("skip") : 0;
        } catch (JSONException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            this.target = null;
            this.action = null;
            this.strategy = null;
            this.skip = 0;
        }

    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public FuzzAction getAction() {
        return action;
    }

    public void setAction(FuzzAction action) {
        this.action = action;
    }

    public FuzzStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(FuzzStrategy strategy) {
        this.strategy = strategy;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    @Override
    public String toString() {
        return "FuzzCommand{" +
                "target='" + target + '\'' +
                ", action=" + action +
                ", strategy=" + strategy +
                ", skip=" + skip +
                '}';
    }
}
