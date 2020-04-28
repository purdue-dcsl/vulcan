package edu.purdue.dagobah.fuzzer.intent.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Intent Fuzzing Strategies
 */
public enum IFuzzType {
    NULL,
    RANDOM,
    SEMIVALID,
    EXPT1,
    EXPT2,
    EXPT3,
    EXPT4,
    EXPT5,
    EXPT6,
    EXPT7,
    EXPT8;


    /**
     * Mapping from ipcTypes to Strings for display. Overhead because you can't
     * switch on strings.
     */
    static Map<String, IFuzzType> fuzzNamesToTypes = new HashMap<String, IFuzzType>();
    static Map<IFuzzType, String> fuzzTypesToNames = new TreeMap<IFuzzType, String>();

    static {
        fuzzNamesToTypes.put("Null-old", IFuzzType.NULL);
        fuzzNamesToTypes.put("Random-old", IFuzzType.RANDOM);
        fuzzNamesToTypes.put("Semivalid-old", IFuzzType.SEMIVALID);
        fuzzNamesToTypes.put("Semivalid-act-or-data(1)", IFuzzType.EXPT1);
        fuzzNamesToTypes.put("Blank-act-or-data(2)", IFuzzType.EXPT2);
        fuzzNamesToTypes.put("Random-act-or-data(3)", IFuzzType.EXPT3);
        fuzzNamesToTypes.put("Random-extras(4)", IFuzzType.EXPT4);
        fuzzNamesToTypes.put("wearable-fuzzed-extras(5)", IFuzzType.EXPT5);
        fuzzNamesToTypes.put("wearable-semi-valid-random-data(6)", IFuzzType.EXPT6);
        fuzzNamesToTypes.put("wearable-semi-valid-random-mimetype(7)", IFuzzType.EXPT7);
        fuzzNamesToTypes.put("wearable-valid-action(8)", IFuzzType.EXPT8);

        for (Map.Entry<String, IFuzzType> e : fuzzNamesToTypes.entrySet()) {
            fuzzTypesToNames.put(e.getValue(), e.getKey());
        }
    }

    public static String toName(IFuzzType type) {
        return fuzzTypesToNames.get(type);
    }

    public static IFuzzType fromName(String name) {
        return fuzzNamesToTypes.get(name);
    }

}