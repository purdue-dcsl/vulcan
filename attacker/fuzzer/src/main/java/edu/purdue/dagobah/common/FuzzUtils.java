package edu.purdue.dagobah.common;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import edu.purdue.dagobah.fuzzer.intent.specs.Action;
import edu.purdue.dagobah.fuzzer.intent.specs.Extra;
import edu.purdue.dagobah.fuzzer.intent.specs.IntentSpecs;

import static edu.purdue.dagobah.common.Constants.TAGF;
import static edu.purdue.dagobah.common.Constants.URI_TYPES;
import static edu.purdue.dagobah.common.Constants.WEARABLE_MIME_TYPES;

/**
 * Unsorted list of helper methods and utilities for fuzzing
 */
public class FuzzUtils {

    /**
     * Returns an random {@link String} from an Array of {@link String}.
     * @param t the array of {@link String}
     * @return a {@link String} randomly selected from an array
     */
    public static String getTypeString(String t[]) {
        return t[Constants.rnd.nextInt(t.length)];
    }

    /**
     * Generates pseudo random bytes of data of a given size.
     *
     * @param bufferSize the buffer size
     * @param sizeIsFixed the flag that indicates whether buffersize should be = or < the argument
     *                    specified
     * @return
     */
    public static byte[] getRandomData(int bufferSize, boolean sizeIsFixed) {
        if(!sizeIsFixed)
            bufferSize = Constants.rnd.nextInt(bufferSize)+1;
        byte b[] = new byte[bufferSize];
        for(int i=0; i<bufferSize; i++) {
            b[i] = (byte)Constants.rnd.nextInt(256);
        }
        return b;
    }

    public static double getRandomDouble() {
        return Constants.rnd.nextDouble();
    }

    public static boolean getRandomBoolean() {
        return Constants.rnd.nextBoolean();
    }

    /**
     * Generates a pseudo random URI of a given quality. If the quality is "dumb", the generated
     * URI does not take in consideration the different types of URI supported. Otherwise, with
     * quality "good", the generated URI use as schema any of the supported URI types.
     *
     * A URI consist of three parts:
     * <code>
     * schema/path?query
     * </code>
     *
     * @param type the type of the desired quality. Either "dumb" or "good".
     * @return the random URI.
     */
    public static Uri getRandomUri(String type) {
        String s = "";
        try{
            if(type.equals("dumb")) {
                s = new String(getRandomData(256, false));
                //	System.err.println("******************Uri: "+s+"\n");
                return Uri.parse(s);
            } else if(type.equals("good")) {
                s = getTypeString(URI_TYPES);
                s.concat(new String(getRandomData(1024, false)));
                return Uri.parse(s);
            } else{
                return Uri.parse(type.concat(new String(getRandomData(1024, false))));
            }

        }
        catch(Exception ex) {
            System.err.println("******************An Uri exception occurred.\n"+s+"\n****************\n");
        }
        return null;
    }

    public static String byteArrayToString(byte[] ba) {
        char hexmap[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder hex = new StringBuilder(ba.length * 3);
        for (byte b : ba)
        {
		  /*if(b == 0)
			  hex.append("00."); //"{0:x2}",
		  else
			  hex.append(String.format("%x.", b));*/
            byte t = b;
            hex.append(hexmap[(t>>>4)& 0xf]);
            hex.append(hexmap[ t & 0xf]);
            hex.append(".");
        }
        return hex.toString();
    }

    /* ---------------------------------------------------------------------------
     * Helpers
     * ---------------------------------------------------------------------------
     */

    /**
     * Reads a JSON from an stream (e.g., {@link InputStream}) and returns the content in a
     * {@link String}.
     * @param in the {@link InputStream} with the JSON data.
     * @return the {@link String} contained the JSON data.
     * @throws IOException if the data (encoded in a JSON format) cannot be retrieved from the
     * {@link InputStream}.
     */
    public static String readJSONFromStream(InputStream in) throws IOException {

        int size = in.available();
        byte[] buff = new byte[size];
        in.read(buff);
        in.close();

        return new String(buff, "UTF-8");
    }

    public static Intent getIntentSemiValidRandom (ComponentName cmp, Action action, List<Extra> extras) {

        Intent intent = new Intent();
        // Implicit intents
        intent.setComponent(cmp);
        intent.setAction(action.getConstantValue());

        // Set random values to extra fields based on their data types
        if (extras != null)
            for (Extra extra : extras) {
                intent = extra.setRandomValue(intent);
            }

        return intent;
    }

    /**
     * Returns a list of valid <code>actions</code> to use for the fuzzer. Actions that are
     * system protected or deprecated are ignored.
     */
    public static List<Action> getActions (List<Action> list) {

        // This can be fairly easy with lambda (but lambdas are not supported in Java 7)
        Predicate<Action> condition = new Predicate<Action>()
        {
            @Override
            public boolean test(Action action) {
                return action.isValid();
            }
        };

        Supplier<List<Action>> supplier = new Supplier<List<Action>>() {
            @Override
            public List<Action> get() {
                return new ArrayList<Action>();
            }
        };

        List<Action> filtered = list.stream()
                .filter( condition ).collect(Collectors.toCollection(supplier));
        return filtered;
    }

    public static String pickOneMimeType(String OrigMimeType) {
        int rand = Constants.rnd.nextInt(WEARABLE_MIME_TYPES.length);
        while (WEARABLE_MIME_TYPES[rand].equals(OrigMimeType)) {
            rand = Constants.rnd.nextInt(WEARABLE_MIME_TYPES.length);
        }
        return WEARABLE_MIME_TYPES[rand];
    }

    public static void log (String output) {
        Log.d(TAGF, String.format("%dms (%d/%d) -> %s",
                System.currentTimeMillis() - Constants.START_TIME, Constants.gNumIntents, Constants.gSensorReads, output));
    }


}
