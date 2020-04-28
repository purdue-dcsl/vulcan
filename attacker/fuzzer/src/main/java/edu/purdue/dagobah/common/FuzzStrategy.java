package edu.purdue.dagobah.common;

import java.util.HashMap;
import java.util.Map;

import static edu.purdue.dagobah.common.FuzzStrategy.FUZZ_STRATEGY_KYLO_2;

/**
 * Enum class for action supported by the fuzzer.
 */
public enum FuzzStrategy {

    FUZZ_STRATEGY_1,
    FUZZ_STRATEGY_2,
    FUZZ_STRATEGY_3,
    FUZZ_STRATEGY_4,
    FUZZ_STRATEGY_5,
    FUZZ_STRATEGY_6,
    FUZZ_STRATEGY_7,
    FUZZ_STRATEGY_8,
    FUZZ_STRATEGY_9,
    FUZZ_STRATEGY_10,
    FUZZ_STRATEGY_WEAR,
    FUZZ_STRATEGY_TEST,
    FUZZ_STRATEGY_UNKNOWN;

    static final String FUZZ_STRATEGY_KYLO_1  = "strategy/1";
    static final String FUZZ_STRATEGY_KYLO_2  = "strategy/2";
    static final String FUZZ_STRATEGY_KYLO_3  = "strategy/3";
    static final String FUZZ_STRATEGY_KYLO_4  = "strategy/4";
    static final String FUZZ_STRATEGY_KYLO_5  = "strategy/5";
    static final String FUZZ_STRATEGY_KYLO_6  = "strategy/6";
    static final String FUZZ_STRATEGY_KYLO_7  = "strategy/7";
    static final String FUZZ_STRATEGY_KYLO_8  = "strategy/8";
    static final String FUZZ_STRATEGY_KYLO_9  = "strategy/9";
    static final String FUZZ_STRATEGY_KYLO_10 = "strategy/10";
    static final String FUZZ_STRATEGY_KYLO_WEAR = "strategy/wear";
    static final String FUZZ_STRATEGY_KYLO_TEST = "strategy/test";

    static Map<FuzzStrategy, String> fuzzStrategyToPath = new HashMap<>();
    static Map<String, FuzzStrategy> fuzzPathToStrategy = new HashMap<>();

    /**
     * Init hashmaps
     */
    static {
        // populate fuzzPathToAction hashmap
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_1,  FUZZ_STRATEGY_KYLO_1);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_2,  FUZZ_STRATEGY_KYLO_2);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_3,  FUZZ_STRATEGY_KYLO_3);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_4,  FUZZ_STRATEGY_KYLO_4);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_5,  FUZZ_STRATEGY_KYLO_5);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_6,  FUZZ_STRATEGY_KYLO_6);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_7,  FUZZ_STRATEGY_KYLO_7);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_8,  FUZZ_STRATEGY_KYLO_8);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_9,  FUZZ_STRATEGY_KYLO_9);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_10,  FUZZ_STRATEGY_KYLO_10);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_WEAR,  FUZZ_STRATEGY_KYLO_WEAR);
        fuzzStrategyToPath.put(FuzzStrategy.FUZZ_STRATEGY_TEST,  FUZZ_STRATEGY_KYLO_TEST);

        // populate fuzzActionToPath hashmap using fuzzPathToAction
        for (Map.Entry<FuzzStrategy, String> e : fuzzStrategyToPath.entrySet()) {
            fuzzPathToStrategy.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Returns the path from a given action.
     * @param strategy the fuzz action
     * @return the corresponding path
     */
    public static String toPath(FuzzStrategy strategy) {
        return fuzzStrategyToPath.get(strategy);
    }

    /**
     * Returns the action from a given path.
     * @param path the path
     * @return the corresponding action (@link enum) that matches to the path.
     */
    public static FuzzStrategy fromPath(String path) {
        if ( fuzzPathToStrategy.containsKey(path) )
            return fuzzPathToStrategy.get(path);
        else
            return FUZZ_STRATEGY_UNKNOWN;
    }

}
