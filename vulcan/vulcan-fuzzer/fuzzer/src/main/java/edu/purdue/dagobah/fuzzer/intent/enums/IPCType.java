package edu.purdue.dagobah.fuzzer.intent.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Android Component Type enumeration
 */
public enum IPCType {
    ACTIVITIES,
    BROADCASTS,
    PROVIDERS,
    SERVICES,
    INSTRUMENTATIONS;

    /**
     * Mapping from ipcTypes to Strings for display. Overhead because you can't
     * switch on strings.
     */
    static Map<IPCType, String> ipcTypesToNames = new TreeMap<IPCType, String>();
    static Map<String, IPCType> ipcNamesToTypes = new HashMap<String, IPCType>();

    static {
        ipcTypesToNames.put(IPCType.ACTIVITIES, "Activities");
        ipcTypesToNames.put(IPCType.BROADCASTS, "Broadcasts");
        ipcTypesToNames.put(IPCType.PROVIDERS, "Providers");
        ipcTypesToNames.put(IPCType.SERVICES, "Services");
        ipcTypesToNames.put(IPCType.INSTRUMENTATIONS, "Instrumentations");

        for (Map.Entry<IPCType, String> e : ipcTypesToNames.entrySet()) {
            ipcNamesToTypes.put(e.getValue(), e.getKey());
        }
    }

    public static String toName(IPCType type) {
        return ipcTypesToNames.get(type);
    }

    public static IPCType fromName(String name) {
        return ipcNamesToTypes.get(name);
    }

}