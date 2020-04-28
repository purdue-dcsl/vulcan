package edu.purdue.dagobah.fuzzer.intent.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * FIXME 5/23 Most probably this class is not needed anymore.
 * edu.purdue.dagobah.common.FuzzAction should replace this class
 *
 * @author ebarsallo
 */
@Deprecated
public enum IFuzzAction {
    ACTION_START,
    ACTION_LOAD,
    ACTION_FUZZ_SINGLE,
    ACTION_FUZZ_ALL,
    ACTION_RUN_ALL_EXPERIMENTS,
    ACTION_UNKNOWN;


    static Map<IFuzzAction, String> iFuzzActionToPath = new HashMap<>();
    static Map<String, IFuzzAction> iFuzzPathToAction = new HashMap<String, IFuzzAction>();


    static {
        iFuzzActionToPath.put(IFuzzAction.ACTION_START, "/start");
        iFuzzActionToPath.put(IFuzzAction.ACTION_LOAD, "/load");
        iFuzzActionToPath.put(IFuzzAction.ACTION_FUZZ_SINGLE, "/fuzzsingle");
        iFuzzActionToPath.put(IFuzzAction.ACTION_FUZZ_ALL, "/fuzzall");
        iFuzzActionToPath.put(IFuzzAction.ACTION_RUN_ALL_EXPERIMENTS, "/runall");

        for (Map.Entry<IFuzzAction, String> e : iFuzzActionToPath.entrySet()) {
            iFuzzPathToAction.put(e.getValue(), e.getKey());
        }
    }

    public static String toPath(IFuzzAction action) {
        return iFuzzActionToPath.get(action);
    }

    public static IFuzzAction fromPath(String path) {
        if (iFuzzPathToAction.containsKey(path.toLowerCase()))
            return iFuzzPathToAction.get(path.toLowerCase());
        else
            return ACTION_UNKNOWN;
    }

}


