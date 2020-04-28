package edu.purdue.dagobah.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum class for action supported by the fuzzer.
 */
public enum FuzzAction {

    ACTION_FUZZ_INTENT_START,
    ACTION_FUZZ_NOTIF_START,
    ACTION_FUZZ_TEST,
    ACTION_UNKNOWN;

    static final String ACTION_INTENT_START = "vnd.dcsl.action.FUZZ_INTENT_START";
    static final String ACTION_NOTIF_START = "vnd.dcsl.action.FUZZ_NOTIF_START";
    static final String ACTION_TEST = "vnd.dcsl.action.FUZZ_TEST";

    static Map<FuzzAction, String> fuzzActionToPath = new HashMap<>();
    static Map<String, FuzzAction> fuzzPathToAction = new HashMap<>();

    /**
     * Init hashmaps
     */
    static {
        // populate fuzzPathToAction hashmap
        fuzzActionToPath.put(FuzzAction.ACTION_FUZZ_INTENT_START,  ACTION_INTENT_START);
        fuzzActionToPath.put(FuzzAction.ACTION_FUZZ_NOTIF_START,  ACTION_NOTIF_START);
        fuzzActionToPath.put(FuzzAction.ACTION_FUZZ_TEST,  ACTION_TEST);

        // populate fuzzActionToPath hashmap using fuzzPathToAction
        for (Map.Entry<FuzzAction, String> e : fuzzActionToPath.entrySet()) {
            fuzzPathToAction.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Returns the path from a given action.
     * @param action the fuzz action
     * @return the corresponding path
     */
    public static String toPath(FuzzAction action) {
        return fuzzActionToPath.get(action);
    }

    /**
     * Returns the action from a given path.
     * @param path the path
     * @return the corresponding action (@link enum) that matches to the path.
     */
    public static FuzzAction fromPath(String path) {
        if ( fuzzPathToAction.containsKey(path) )
            return fuzzPathToAction.get(path);
        else
            return ACTION_UNKNOWN;
    }

}
