package edu.purdue.dagobah.fuzzer.intent.specs;

import java.util.HashMap;
import java.util.Map;

public enum DataType {

    Tint,
    Tlong,
    Tfloat,
    Tdouble,
    TString,
    Tchar,
    TCharSequence,
    Tboolean,
    TURI,
    TObject,
    TintArray,
    TStringArray,
    TStringArrayList,
    TStringArrayArrayList,
    TCharSequenceArrayList,
    TURIArrayList,
    TUnknown;

    static Map<DataType, String> dataTypeToString = new HashMap<>();
    static Map<String, DataType> stringToDataType = new HashMap<>();

    /**
     * Init hashmaps
     */
    static {
        // populate dataTypeToString hashmap
        dataTypeToString.put(Tint, "int");
        dataTypeToString.put(Tlong, "long");
        dataTypeToString.put(Tfloat, "float");
        dataTypeToString.put(Tdouble, "double");
        dataTypeToString.put(TString, "string");
        dataTypeToString.put(Tchar, "char");
        dataTypeToString.put(TCharSequence, "charsequence");
        dataTypeToString.put(Tboolean, "boolean");
        dataTypeToString.put(TURI, "uri");
        dataTypeToString.put(TObject, "object");
        dataTypeToString.put(TStringArray, "string []");
        dataTypeToString.put(TintArray, "int []");
        dataTypeToString.put(TStringArrayList, "string-arraylist");
        dataTypeToString.put(TCharSequenceArrayList, "charsequence-arraylist");
        dataTypeToString.put(TURIArrayList, "uri-arraylist");
        dataTypeToString.put(TStringArrayArrayList, "string []-arraylist");

        // populate stringToDataType hashmap
        for (Map.Entry<DataType, String> e : dataTypeToString.entrySet()) {
            stringToDataType.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Returns the DataType from a given string. If the input string does not match any
     * {@link DataType} the <code>TUnknown</code> is returned.
     *
     * @param type the {@link String} that represents the type.
     * @return the {@link DataType} corresponding to the input.
     */
    public static DataType fromString(String type) {
        if (stringToDataType.containsKey(type.toLowerCase()))
            return stringToDataType.get(type.toLowerCase());
        else
            return TUnknown;
    }

}
