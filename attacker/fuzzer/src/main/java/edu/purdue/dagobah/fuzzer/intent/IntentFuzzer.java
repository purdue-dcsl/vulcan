package edu.purdue.dagobah.fuzzer.intent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.android.gms.fitness.*;
import com.google.android.gms.fitness.data.DataType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzUtils;
import edu.purdue.dagobah.common.ResourcesManager;
import edu.purdue.dagobah.fuzzer.intent.enums.IFuzzType;
import edu.purdue.dagobah.fuzzer.intent.enums.IPCType;
import edu.purdue.dagobah.fuzzer.intent.specs.Action;
import edu.purdue.dagobah.fuzzer.intent.specs.Extra;
import edu.purdue.dagobah.fuzzer.intent.specs.IntentSpecs;
import edu.purdue.ryloth.dmz.DUtils;
import edu.purdue.ryloth.dmz.ThresholdSecurityException;

import static com.google.android.gms.fitness.FitnessActivities.getMimeType;
import static edu.purdue.dagobah.common.Constants.*;

// FIXME Save Application Context in a global variable.
/**
 * Intent Fuzzer
 * Main Intent Fuzzer class. The fuzzer can target mobile apps (Android OS) and wearable apps
 * (Wear OS). We can distingish two basic operatio which includes two basic operations:
 * <ul>
 *     <li>Generate malformed intents based on four different campaings.</li>
 *     <li>Inject the intents to the target application/device.</li>
 * </ul>
 *
 * Bugs detected by the fuzzer can cause the system to crash (in some causes soft reboots) or
 * performance issues on the device. The class can fuzz a single component or all the components
 * of an application. The fuzzer targets the IPC mechanism, used by components in Android devices
 * to communicate with each other.
 *
 * Based on the Intent Fuzzer developed by NCC Group and the modifications
 * introduced by Amiya K. Maji. Please refer:  Maji, A. K., Arshad, F. A., Bagchi, S., & Rellermeyer,
 * J. S. (2012, June). <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.473.9121">
 * An empirical study of the robustness of inter-component communication in Android</a>.
 * In Dependable Systems and Networks (DSN), 2012 42nd annual IEEE/IFIP International Conference on
 * (pp. 1-12). IEEE.
 *
 * @author NCC Group
 * @author Amiya Maji
 * @author ebarsallo
 *
 * @see <a href="https://www.nccgroup.trust/us/about-us/resources/intent-fuzzer/">Intent Fuzzer</a>
 *
 */
public class IntentFuzzer extends Activity {

    // TODO Adjust to have specific wearable actions/data/mime types
    // Probably this will required to modify existing campaigns or/and to new campaigns.

    private static final String TAG = "FUZZ/IFuzz";

    private static final String DEFAULT_FUZZ_INTENT_TYPE = "Null-old";

    // target apps
    private String TARGET_APPS[] = Constants.TARGET_APPS;

    // activity's Package Manager
    PackageManager mPackageManager;
    // context
    Context context;
    //
    IntentSpecs specs, wearable_specs;
    // resources manager
    ResourcesManager mResManager;


    // list of supported Types
    private ArrayList<String> mTypes = new ArrayList<String>();
    // list of ComponentsName
    private ArrayList<String> mComponentNames = new ArrayList<String>();
    // list of ComponentName for the current IPC type
    private ArrayList<ComponentName> mKnownComponents = new ArrayList<ComponentName>();
    // the list of Providers (essential for building content URI)
    private ArrayList<String> mKnownProviders = new ArrayList<String>();
    // list of type of fuzzing intents
    private ArrayList<String> mFuzzingIntents = new ArrayList<String>();

    // number of collisions on Component Names retrieved
    private int collisions;
    // seed to generate pseudorandom numbers
    private long seed = System.currentTimeMillis();
    private Random rnd = new Random(seed);
    // version #
    private String version = "20190526.1435";
//    ebarsallo: temporal; remove this comment after finish with tests.
//    private JSONObject wearable_acts = Constants.getWearableSpecs();
    /* ---------------------------------------------------------------------------
     * Instance Variables
     * ---------------------------------------------------------------------------
     */

    // blacklist loaded into a HashMap for faster searching
    static Map<String, String> blkList = new HashMap<String, String>();
    static {
        for(String c : BLACKLIST)
        {
            blkList.put(c, c);
        }
    }

    UriGen ug = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    // this is a dummy object for getting acquainted with memory profiling
    Intent dummyIn = null;

    //------------------------
    private static IntentFuzzer instance;

    /**
     * Construct an instance of {@link IntentFuzzer} given an specified Application
     * Activity. This activity, either a mobile activity or a wearable activity, is the one that
     * will initiated the IPC communication.
     *
     * @param activity the Activity that will initiate the IPC fuzz tests.
     */
    private IntentFuzzer(Activity activity) {
        Log.d(TAG, "Constructor");

        this.my = activity;
        this.mPackageManager = activity.getPackageManager();
        this.mResManager = new ResourcesManager(activity);

        this.TARGET_APPS = Constants.TARGET_APPS;

        // init class
        init(activity.getApplicationContext());

        // load constants from raw assets
        setup();

        Log.i(TAG, "Seed: {" + this.seed + "} Version: {" + this.version + "}");
    }

//    private IntentFuzzer(Context context) {
//        Log.d(TAG, "Constructor");
//        this.my = new Activity();
//        this.mPackageManager = context.getPackageManager();
//
//        // init class
//        init(context);
//    }

    /**
     * Setup an instance of {@link IntentFuzzer} using with Android API specification. The config
     * values are loaded from a json file.
     *
     * @see https://developer.android.com/reference/android/content/Intent.html
     * @see https://docs.google.com/spreadsheets/d/1xajUjXCID1ZVSmqDmgCwm9xaHDJFC1XagBZoL1UyfN4/edit?usp=sharing
     *
     */
    private void setup() {
        try {

            Log.d(TAG, "== Setup ==");

            // Android Intent Specification
            // Init mappings between Action, Data, and Extra fields according to the API
            // specification (as described in a JSON file in the resources)
            Log.d(TAG, "Loading general intent specification ...");
            InputStream in = my.getResources()
                    .openRawResource(
                            my.getResources()
                                    .getIdentifier(JSON_SDK_FILENAME, "raw", my.getPackageName())
                    );

            // Parse JSON file and load it into memory
            this.specs = new IntentSpecs(in);
            this.specs.printSpecs(false);

            // Android Intent Specification wearable-specific
            Log.d(TAG, "Loading wearable-specific intent specification ...");
            InputStream wearable_in = my.getResources()
                    .openRawResource(
                            my.getResources()
                                    .getIdentifier(WEARABLE_JSON_SDK_FILENAME, "raw", my.getPackageName())
                    );
            this.wearable_specs = new IntentSpecs(wearable_in);
            this.specs.printSpecs(false);

        } catch (IOException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            Log.e(TAG, "Error: Not possible to load the setup file from the resources");
        } catch (JSONException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            Log.e(TAG, "Error: Not possible to decode the JSON data");
        }
    }

    /**
     * Get an instance of {@link IntentFuzzer} given an specified Application Activity
     * using a singleton pattern. The method is thread safe.
     *
     * @param activity the Activity that will initiate the IPC fuzz tests.
     * @return the instance of {@link IntentFuzzer}
     */
//    public static synchronized IntentFuzzer getInstance(Context context) {
//        if (instance == null) {
//            instance = new IntentFuzzer(context);
//        }
//        return instance;
//    }

    public static synchronized IntentFuzzer getInstance(Activity activity) {
        if (instance == null) {
            instance = new IntentFuzzer(activity);
        }

        return instance;
    }

    public static synchronized IntentFuzzer getInstance() {
        if (instance == null) {
            Log.e(TAG, "IntentFuzzer must be initiated by the Main Activity");
        }
        return instance;
    }
    //------------------------




    /**
     * Construct an instance of {@link IntentFuzzer} given a specified manager. The Intent fuzzer
     * primary focus or objectives are the apps installed on the Android OS. Therefore, the
     * Package Manager from the target device is needed.
     *
     * @param mngr the Package Manager
     */
    public IntentFuzzer(PackageManager mngr) {
        this.mPackageManager = mngr;
    }

    /* ---------------------------------------------------------------------------
     * Access method
     * ---------------------------------------------------------------------------
     */

//    public ArrayList<String> getTypes() {
//        return mTypes;
//    }
//
//    public void setTypes(ArrayList<String> types) {
//        this.mTypes = types;
//    }
//
//    public ArrayList<String> getComponentNames() {
//        return mComponentNames;
//    }
//
//    public void setComponentNames(ArrayList<String> componentNames) {
//        this.mComponentNames = componentNames;
//    }
//
//    public ArrayList<ComponentName> getKnownComponentNames() {
//        return mKnownComponents;
//    }
//
//    public void setKnownComponentNames(ArrayList<ComponentName> knownComponents) {
//        this.mKnownComponents = knownComponents;
//    }


    /**
     * Set the target apps
     * @param list the list of target apps
     */
    public void setTargetApp(String list[]) {
        this.TARGET_APPS = list;
    }

    /**
     * Return # of collisions
     * @return the number of collisions
     */
    public int getCollisions() {
        return this.collisions;
    }

    /**
     * Return the types of Fuzzing Intent supported by the tool.
     *
     * @return the types of Fuzzing Intent supported
     */
    public ArrayList<String> getFuzzTypes() {
        return this.mFuzzingIntents;
    }

    /**
     * Return the default type of Fuzzing Intent supported by the tool.
     *
     * @return the IFuzzType instance with the default type of Fuzzing Intent supported.
     */
    public IFuzzType getDefaultType() {
        return IFuzzType.fromName(DEFAULT_FUZZ_INTENT_TYPE);
    }

    /* ---------------------------------------------------------------------------
     * Helper methods
     * ---------------------------------------------------------------------------
     */

    byte[] readFile(int i, int maxBufferSize) {
        String basedir = "t";
        String filename = basedir+i;
        System.err.println("******************Filename: "+filename+"\n");

        //File f = new File(filename);
        int offset = rnd.nextInt(getFileSize(i));
        byte b[] = new byte[rnd.nextInt(maxBufferSize)+1];//(int)sz];
        try
        {
            final InputStream is = getResources().getAssets().open(filename);


            //FileInputStream fi = new FileInputStream(f);
            is.skip(offset);
            is.read(b);
            //mOut.setText("Offset: "+offset+" buffer_size: "+b.length+"\n");
            //mOut.append(new String(b));
            is.close();
        }
        catch(Exception ex)
        {
            System.err.println("******************Error reading file.****************\n");
        }
        return b;
    }

    //due to problem with finding sizes of resources files in Android we are using
    //this static map from Fuzz input files
    int getFileSize(int i) {
        if(i%3 == 1)
            return 1000;
        if(i%3 == 2)
            return 10000;
        else
            return 100000;

    }



    boolean isBlackListed(String name) {
        if (blkList.get(name) != null)
            return true;
        else
            return false;
    }

    int getComponentNameIndex(String clazz) throws Exception {

        for (int i=0; i < mKnownComponents.size(); i++) {
            if (mKnownComponents.get(i).getClassName().equals(clazz)) {
                return i;
            }
        }

        throw new Exception("Cannot find classname: " + clazz);
    }

    ComponentName getComponentName(String clazz) throws Exception {

        for (int i=0; i < mKnownComponents.size(); i++) {
            if (mKnownComponents.get(i).getClassName().equals(clazz)) {
                return mKnownComponents.get(i);
            }
        }

        throw new Exception("Cannot find classname: " + clazz);
    }



    /* ---------------------------------------------------------------------------
     * Helper methods (new)
     * ---------------------------------------------------------------------------
     */


    public void printComponentNames() {
        int i=0;
        for (ComponentName comp : mKnownComponents) {
            Log.d(TAG, String.format("print | {%d} {%s} {%s}",
                    i++, comp.getPackageName(), comp.getClassName()));
        }
    }

    /**
     * Retrieve the <code>Components Names</code> installed on the the target device given a
     * component type (e.g. Activities, Services, Content Providers, Broadcast Receivers).
     *
     * @param type the current Component Type.
     * @return
     */
    public ArrayList<String> getComponentNames(IPCType type) {
        Log.d(TAG, String.format("getComponentNames | type {%s}", type));

        int diff = 0;

        // temporal
        // getTemp();

        // Export Component Names from Package Manager target device
        mKnownComponents.clear();
        mKnownComponents.addAll(getExportedComponents(type));

        // Verify collisions.
        mComponentNames.clear();
        for (ComponentName n : mKnownComponents) {
            if (mComponentNames.contains(n.getClassName()))
                diff++;
            else
                mComponentNames.add(n.getClassName());
        }

        // Update collisions on Component Names
        this.collisions = diff;

        return mComponentNames;
    }

    /**
     * Class fuzzer init
     */
    void init(Context context) {

        // Init Fuzz Intent type list
        mFuzzingIntents.add("Null-old");
        mFuzzingIntents.add("Random-old");
        mFuzzingIntents.add("Semivalid-old");
        mFuzzingIntents.add("Semivalid-act-or-data(1)");
        mFuzzingIntents.add("Blank-act-or-data(2)");
        mFuzzingIntents.add("Random-act-or-data(3)");
        mFuzzingIntents.add("Random-extras(4)");
        mFuzzingIntents.add("Implicit-rand-data(5)");

        // Populate Provider Authority list
        this.populateProviders();

        // Init URI generator
        ug = new UriGen(context.getFilesDir().getPath(), rnd, mKnownProviders, context);
    }

    /* ---------------------------------------------------------------------------
     *
     * ---------------------------------------------------------------------------
     */

//    /**
//     * Takes a list of component names, and uses it to populate the
//     * <code>components</code> list. This list is used to render a human readable list
//     * of components by Class name.
//     *
//     * @param newComponents the {@link ComponentName} list for an specific type.
//     *
//     * @return int  the number of component name collisions.
//     */
//    public int setKnownComponents(ArrayList<ComponentName> newComponents) {
//        int diff = 0;
//
//        mKnownComponents.clear();
//        mKnownComponents.addAll(newComponents);
//        mComponentNames.clear();
//
//        // Check for collisions
//        for (ComponentName n : mKnownComponents) {
//            if (mComponentNames.contains(n.getClassName()))
//                diff++;
//            else
//                mComponentNames.add(n.getClassName());
//        }
//
//        return diff;
//    }


    private boolean filterPackage(String name) {
        return filterPackage(name, this.TARGET_APPS);
    }

    private boolean filterPackage(String name, String[] target) {

        for (String prefix : target)
            if (name.startsWith(prefix)) {
                //Log.d(TAG, String.format("filterPackage | {%s} {%s}", prefix, name));
                return true;
            }

        return false;
    }


//    protected void getTemp() {
//        ArrayList<String> list = new ArrayList<>();
//
//        // middleware
//        list.add ("com.google.android.apps.fitness");
//        list.add ("com.sillens.shapeupclub");
//        list.add ("com.runtastic.android");
//        list.add ("com.strava");
//        list.add ("se.perigee.android.seven");
//        list.add ("com.dungelin.heartrate");
//        list.add ("com.northpark.drinkwater");
//        list.add ("com.accuweather.android");
//        list.add ("com.weather.Weather");
//        list.add ("net.nurik.roman.muzei");
//        list.add ("com.google.android.calculator");
//        list.add ("com.google.android.apps.maps");
//        list.add ("com.google.android.deskclock");
//
//
//        // top-25 apps non built-in packages (Android 7.1.1)
//        list.clear();
//
//        ArrayList<ComponentName> act = new ArrayList<>();
//        ArrayList<ComponentName> srv = new ArrayList<>();
//
//        for (String app : list) {
//            Constants.TARGET_APP = app;
//
//            act.clear();
//            srv.clear();
//
//            act.addAll(getExportedComponents(IPCType.ACTIVITIES));
//            srv.addAll(getExportedComponents(IPCType.SERVICES));
//
//            Log.d(TAG, String.format("\tbuilt-in:\t%s \t%d \t%d", app, act.size(), srv.size()));
//        }
//    }


    public ArrayList<ComponentName> getExportedComponents(IPCType type) {
        return getExportedComponents(type, this.TARGET_APPS);
    }

    public ArrayList<ComponentName> getExportedComponents(IPCType type, String apk) {
        String[] target = {apk};
        return getExportedComponents(type, target);
    }

    /**
     * For any type, provide the registered instances based on what the package
     * manager has on file. Only provide exported components.
     *
     * @param type the IPC requested, activity, broadcast, etc.
     *
     * @return
     */
    public ArrayList<ComponentName> getExportedComponents(IPCType type, String[] target) {
        Log.d(TAG, String.format("getExportedComponents | type {%s}", type));

        ArrayList<ComponentName> found = new ArrayList<ComponentName>();
        PackageManager pm = this.mPackageManager;

        for (String apk : this.TARGET_APPS)
            Log.d(TAG, String.format("getExportedComponents | target {%s}", apk));

        // Retrieve components installed on device from the Android Package Manager
        // GET_DISABLED_COMPONENTS was deprecated in API 24, for MATCH_DISABLED_COMPONENTS
        for (PackageInfo pi : pm
                .getInstalledPackages(PackageManager.GET_ACTIVITIES
                        | PackageManager.GET_RECEIVERS
                        | PackageManager.GET_INSTRUMENTATION
                        | PackageManager.GET_PROVIDERS
                        | PackageManager.GET_SERVICES)) {
            PackageItemInfo items[] = null;

            switch (type) {
                case ACTIVITIES:
                    items = pi.activities;
                    break;
                case BROADCASTS:
                    items = pi.receivers;
                    break;
                case SERVICES:
                    items = pi.services;
                    break;
                case PROVIDERS:
                    items = pi.providers;
                    break;
                case INSTRUMENTATIONS:
                    items = pi.instrumentation;
            }

            if (items != null)
                for (PackageItemInfo item : items) {
//                     Log.d(TAG, String.format("getExportedComponents | {%d} {%s} {%s}",
//                            i++, pi.packageName, item.name));

                    if (filterPackage(pi.packageName, target)) {
                        Log.d(TAG, "getExportedComponents | found "
                                + "{" + pi.packageName + "}"
                                + "{" + item.name + "}");

                        found.add(new ComponentName(pi.packageName, item.name));
                    }
                }

        }

        return found;
    }

    /**
     * Populates the provider authority list: <code>mKnownProviders</code> from the PackageManager
     * of the target device.
     */
    protected void populateProviders()
    {
        PackageManager pm = this.mPackageManager;

        for (PackageInfo pack : pm.getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    String a = provider.authority;
                    if(a.contains(";"))
                    {
                        String[] aa = a.split(";");
                        for(String x : aa)
                        {
                            mKnownProviders.add(x);
                        }
                    }
                    else
                        mKnownProviders.add(a);
                }
            }
        }

//        for(String s : mKnownProviders) {
//            System.out.println("Found provider authority: "+s);
//        }
    }

    /* ---------------------------------------------------------------------------
     * Main Methods (Fuzz Actions)
     * Initiate the fuzzing. The following methods are invoked by the Main Activity
     * ---------------------------------------------------------------------------
     */

    // this function is called by "Fuzz Single" button and
    // also internally called by "Run Component"

    /**
     *
     * @param type
     * @param IFuzzType
     * @param clazz
     * @return
     */
    public String runSingle(IPCType type, IFuzzType IFuzzType, String clazz)
    {
        String out = "";
        ComponentName cmp;

        try {

            Log.i(TAG, "Seed: {" + this.seed + "} Version: {" + this.version + "}");

            Log.d(TAG, String.format("runSingle | Now fuzzing: %s %s %s",
                    IPCType.toName(type), clazz, dateFormat.format(new Date())));
            Log.d(TAG, String.format("runSingle | Current Fuzz Type: {%s}", IFuzzType));

            cmp = getComponentName(clazz);
            switch(IFuzzType)
            {
                case NULL:
                    out = fuzzNullSingle(type, clazz);
                    break;
                case RANDOM:
                    out = sendIntent(type, clazz);
                    break;
                case SEMIVALID:
                    out = sendIntent(type, clazz);
                    break;
                case EXPT1:
                    out = expt1(cmp, type);
                    break;
                case EXPT2:
                    out = expt2(cmp, type);
                    break;
                case EXPT3:
                    out = expt3(cmp, type);
                    break;
                case EXPT4:
                    out = expt4(cmp, type);
                    break;
                default:
                    break;
            }

            Thread.sleep(1000);
        }
        catch(Exception ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            out += "An exception occurred: \n"+ex.getMessage();
        }

        return out;
    }

    public String runAllExpts(IPCType type) {
        return runAllExpts(type, (ComponentName[]) mKnownComponents.toArray());
    }

    public String runAllExpts(IPCType type, ComponentName[] components) {

        // Check limits
        /*
        if (begin < 0 || end > mKnownComponents.size() || begin > end) {
            return String.format("Invalid begin {%d}, end {%d} sequence in runAll." +
                    "Component Size: {%d}", begin, end, mKnownComponents.size());
        }*/

        IFuzzType[] experiments = {
                IFuzzType.NULL,
                IFuzzType.RANDOM,
                IFuzzType.SEMIVALID,
                IFuzzType.EXPT1,
                IFuzzType.EXPT2,
                IFuzzType.EXPT3,
                IFuzzType.EXPT4,
                IFuzzType.EXPT5,
                IFuzzType.EXPT6,
                IFuzzType.EXPT7,
                IFuzzType.EXPT8,
        };

        // Run experiments for all Fuzz Types
        for (IFuzzType fuzz : experiments) {

            Log.i(TAG, "Seed: {" + this.seed + "} Version: {" + this.version + "}");

            Log.d(TAG, String.format("runAllExpts | Begin Fuzz type: {%s} {%s} (size: %d)",
                    IFuzzType.toName(fuzz), type, components.length));

            runExpt(type, fuzz, components);
        }

        Log.d(TAG, "runAllExpts | End of Experiments");
        return "";
    }

    /**
     * Execute the fuzzing strategy to the components specified in the range of the provided list
     * of {@link ComponentName}.
     *
     * @param type the type of the component
     * @param IFuzzType the fuzz strategy
     * @param components the list of Component Names to fuzz
     */
    private void runExpt(IPCType type, IFuzzType IFuzzType, ComponentName[] components) {

        Log.d(TAG, String.format("runExp | type {%s} fuzz {%s} cmp (%s)",
                type, IFuzzType, components.getClass()));

        // For each component in the array {@link components} of the type {@link type}
        // call the respective fuzz strategy based on {@link IFuzzType}
        int i=0;
        for(ComponentName component : components) {

            String className = component.getClassName();

            Log.d(TAG, String.format("runExp | Now fuzzing: {%s} at %s (%d out of %d)",
                    className, dateFormat.format(new Date()), i, components.length));

            if (isBlackListed(className)) {
                Log.d(TAG, String.format("runExp | Skipping Blacklisted Component: %s", className));
                continue;
            }

            switch(IFuzzType){
                case NULL:
                    Log.d(TAG, "runExp | In Null (not executed)");
                    break;
                case RANDOM:
                    Log.d(TAG, "runExp | In Random (not executed)");
                    break;
                case SEMIVALID:
                    Log.d(TAG, "runExp | In Semivalid (not executed)");
                    break;
                case EXPT1:
                    // FIC A: Semi-valid Action and Data;
                    expt1(component, type);
                    break;
                case EXPT2:
                    // FIC B: Blank Action or Data
                    expt2(component, type);
                    break;
                case EXPT3:
                    // FIC C: Random Action or Data
                    expt3(component, type);
                    break;
                case EXPT4:
                    // FIC D: Random Extras
                    expt4(component, type);
                    break;
                case EXPT5:
                    expt5(component, type);
                case EXPT6:
                    expt6(component, type);
                case EXPT7:
                    expt7(component, type);
                default:
                    break;
            }

            i++;
        }

        Log.d(TAG, String.format("runExp | Finished running Expt at: %s",
                dateFormat.format(new Date())));
    }


    /* ---------------------------------------------------------------------------
     * Fuzzing Strategies
     * ---------------------------------------------------------------------------
     */


    public void showParams() {

        Log.d(TAGF, String.format("----------------------------------------------------------"));
        Log.d(TAGF, String.format("- NUM_FREQ_GC:                  %5d", NUM_FREQ_GC));
        Log.d(TAGF, String.format("- NUM_DELAY_GC:                 %5d", NUM_DELAY_GC));
        Log.d(TAGF, String.format("- NUM_DELAY_START_ACT:          %5d", NUM_DELAY_START_ACT));
        Log.d(TAGF, String.format("- NUM_DELAY_START_SRV:          %5d", NUM_DELAY_START_SRV));
        Log.d(TAGF, String.format("- NUM_DELAY_START_BST:          %5d", NUM_DELAY_START_BST));
        Log.d(TAGF, String.format("- NUM_INTENT_REPEAT             %5d", NUM_INTENT_REPEAT));
        Log.d(TAGF, String.format("----------------------------------------------------------"));
        Log.d(TAGF, String.format("- BUFFER_SIZE                   %5d", BUFFER_SIZE));
        Log.d(TAGF, String.format("- POC_PARAM_DELAY_INTENT        %5d", POC_PARAM_DELAY_INTENT));
        Log.d(TAGF, String.format("----------------------------------------------------------"));
        Log.d(TAGF, String.format("- POC_MODE                      %5d", POC_MODE));
        Log.d(TAGF, String.format("- POC_PARAM_SENSOR_ACTIVATED    %5d", POC_PARAM_SENSOR_ACTIVATED));
        Log.d(TAGF, String.format("- # SENSOR ACTIVATED            %5d", POC_REAL_SENSOR_ACTIVATED));
        Log.d(TAGF, String.format("- SENSOR_DELAY                  %5d", POC_PARAM_SENSOR_DELAY));
        Log.d(TAGF, String.format("----------------------------------------------------------"));

    }


    /**
     * Campaign: Semi valid {@code Action} and {@code Data}. A valid {@code Action} and a valid
     * {@code Data URI} are generated separately, but the combination of them may be invalid.
     *
     * @param component
     * @param type
     * @return
     */
    public String expt1 (ComponentName component, IPCType type) {

        int numIntent = 0;
        int gcCount = 0;
        int id = 0;
        boolean repeat = false;
        HashMap<String, Integer> map = new HashMap<>();

        showParams();

        //Log.d(TAG, String.format("expt1 | Part 1 #uri {%d} #actions {%d} #total{%d}",
        //        URI_TYPES.length, ACTIONS.length, URI_TYPES.length * ACTIONS.length));

        List<Intent> intents = new ArrayList<>();
        for(int i=1; i < URI_TYPES.length; i++) {

            Uri uri = ug.getUri(URI_TYPES[i], 0);
            for (Action action : specs.getActions()) {

                /* set component, set action, set data */
                Intent intent = new Intent();

                if ( POC_MODE == 0 ) {
                    // explicit Intent (inmediate target: defender app)
                    intent.setComponent(new ComponentName(POC_DEF_PKG, POC_DEF_CLZ));
                    // action
                    intent.setAction(action.getConstantValue());
                    // data
                    intent.setData(uri);
                    // intended target
                    intent.putExtra(POC_INTENT_EXTRA_CMP_PKG, component.getPackageName());
                    intent.putExtra(POC_INTENT_EXTRA_CMP_CLZ, component.getClassName());
                } else if ( POC_MODE == 1 ) {
                    // implicit Intent, therefore, no target component
                    // intended target
                    intent.putExtra(POC_INTENT_EXTRA_CMP_PKG, component.getPackageName());
                    intent.putExtra(POC_INTENT_EXTRA_CMP_CLZ, component.getClassName());
                    // B/cast action
                    type = IPCType.BROADCASTS;
                    intent.setAction(POC_INTENT_BCAST_ACTION);
                    intent.addCategory(POC_INTENT_BCAST_CAT);
                    intent.putExtra(POC_INTENT_EXTRA_ACTION, action.getConstantValue());
                    // data
                    intent.putExtra(POC_INTENT_EXTRA_DATA, uri);
                } else if ( POC_MODE == 2 ){
                    // explicit intent
                    intent.setComponent(component);
                    // action
                    intent.setAction(action.getConstantValue());
                    // data
                    intent.setData(uri);
                } else if ( POC_MODE == 3 ) {
                    // explicit Intent
                    intent.setComponent(component);
                    // action
                    intent.setAction(action.getConstantValue());
                    // data
                    intent.setData(uri);
                } else {
                    // explicit Intent
                    intent.setComponent(component);
                    // action
                    intent.setAction(action.getConstantValue());
                    // data
                    intent.setData(uri);
                }

                // set extra fields
                intent.putExtra(POC_TAG_EXTRAID, id++);
                intents.add(intent);

            }
        }


        for (int i=0; i<intents.size(); i++) {
            gNumIntents++;

            Intent intt;
            if ( NUM_INTENT_REPEAT > 0 )
                intt = intents.get(NUM_INTENT_REPEAT);
            else
                intt = intents.get(i);

            intt.putExtra(POC_TAG_EXTRAID, gNumIntents);

            DUtils.log(String.format("expt1 | n [%d/%d] type {%s} intent {%s}",
                    numIntent, intt.getIntExtra(POC_TAG_EXTRAID, 0), IPCType.toName(type), intt));

            mResManager.getAvailableResources();

            // temporal
            // if ( i > 100 ) return "Intents sent: "+numIntent;

            /* send intent */
            if ( POC_MODE == 2 ) {
                // modo 2 we are using a buffer, and the intent are sent by a bg service
                // add intent to the buffer
                DUtils.logp(POC_TAG_INTENT_SEND, intt, String.format("xx"));

                if ( gIntentBuffer.size() <= BUFFER_SIZE ) {

                    DUtils.addIntentsToBuffer(intt);
                    DUtils.log(String.format("new %d -> %s. intentBuffer %d bufferSize %d",
                            gNumber++, intt, gIntentBuffer.size(), BUFFER_SIZE));

                }
                else{
                    DUtils.logp(POC_TAG_INTENT_DROP, intt,
                            String.format("dropped %d buffer.size (%d)", ++gDropped, gIntentBuffer.size()));
                }
            } else {
                // send the intent
                String ex = sendIntentByType(intt, type, null);
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex): 0;

                    Log.d(TAG, String.format("expt1 | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* ignores validation if the exception is because of the threshold defense mechanism */
                    if (!ex.equals("ThresholdSecurityException")) {
                        /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                        if (c > NUM_RUN_SKIP_EXCEPTION) {
                            Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                            return "Intents sent: " + numIntent;
                        }
                    }

                    map.put(ex, ++c);
                }
            }

            numIntent++;

            if (numIntent % NUM_FREQ_GC == 0) {
                try {
                    Log.d(TAGF, String.format("--- forcing gc %d --- ",++gcCount));
                    System.gc();
                    Thread.sleep(NUM_DELAY_GC);
                }
                catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }

        Log.d(TAG, "expt1 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IA");
        System.gc();

        Log.d(TAG, "expt1 | Sent "+numIntent+" Intents\n");
        return "Intents sent: "+numIntent;
    }


    /**
     * Campaign: Blank {@code Action} or {@code Data}. Either the {@code Action} or {@code Data}
     * is specified, but not both. All the other fields are left blank.
     *
     * @param component
     * @param type
     * @return
     */
    public String expt2 (ComponentName component, IPCType type) {

        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();;

        Log.d(TAG, String.format("expt2 | #actions {%d} #uri-types {%d} #k {%d} #total{%d}",
                ACTIONS.length, URI_TYPES.length, NUM_CONTENT,
                ACTIONS.length + URI_TYPES.length + NUM_CONTENT));

        // Experiment 2(a): set action, keep data blank
        for (String action : ACTIONS) {

            Intent intent = new Intent();
            //set component, set action
            intent.setAction(action);
            intent.setComponent(component);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.d(TAG, String.format("expt2/A | n {%d} type {%s} intent {%s} action {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), action));

            /* Send Intent */
            String ex = sendIntentByType(intent, type, null);
            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex): 0;

                Log.d(TAG, String.format("expt2/A | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }

            numIntent++;
        }

        Log.d(TAG, "expt2 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIA");
        System.gc();


        /* reset map */
        map = new HashMap<>();

        // Experiment 2(b):
        // Set data, keep action blank, all uri types except "content:"
        for(int i=1; i < URI_TYPES.length; i++) {

            Uri uri = ug.getUri(URI_TYPES[i], 0);
            Intent intent = new Intent();
            //set component, set data
            intent.setData(uri);
            intent.setComponent(component);

            Log.d(TAG, String.format("expt2/B | n {%d} type {%s} intent {%s} uri {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), URI_TYPES[i]));

            /* Send Intent */
            String ex = sendIntentByType(intent, type, map);
            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex): 0;

                Log.d(TAG, String.format("expt2/B | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }


            numIntent++;
        }

        Log.d(TAG, "expt2 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIA");
        System.gc();


        /* reset map */
        map = new HashMap<>();

        // URI type is "content:"
        for(int i=0; i < NUM_CONTENT; i++) {

            Uri u = ug.getUri(URI_TYPES[0],  rnd.nextInt(mKnownProviders.size())); //used to be i
            Intent intent = new Intent();
            //set component, set data
            intent.setData(u);
            intent.setComponent(component);

            Log.d(TAG, String.format("expt2/C | n {%d} type {%s} intent {%s} uri {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), URI_TYPES[i]));

            /* Send Intent */
            String ex = sendIntentByType(intent, type, map);
            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex): 0;

                Log.d(TAG, String.format("expt2/C | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, c++);
            }


            numIntent++;
        }
        Log.d(TAG, "expt2 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIC");
        System.gc();

        Log.d(TAG, "expt2 | Sent "+numIntent+" Intents\n");
        return "Intents sent: "+numIntent;
    }

    /**
     * Campaign: Random {@code Action} or {@code Data}. Either the {@code Action} or the
     * {@code Data URI} is valid, and the other is set randomly.
     *
     * @param component the target {@link ComponentName}
     * @param type the type of the target {@link ComponentName}
     * @return
     */
    public String expt3 (ComponentName component, IPCType type) {

        int numIntent = 0;
        Map<String, Integer> map = new HashMap<>();

        Log.d(TAG, String.format("expt3/A | #actions {%d} #k {%d} #total{%d}",
                ACTIONS.length, NUM_RANDOM, ACTIONS.length * NUM_RANDOM));

        // Experiment 3(a): select an Action, send random Data
        for (String action : ACTIONS) {
            for(int count = 0; count < NUM_RANDOM; ) {
                Intent intent = new Intent();
                Uri u = FuzzUtils.getRandomUri("dumb");

                /* a URI could not be generated */
                if (u == null) {
                    Log.e(TAG, "expt3/A | Could not create random URI");
                    continue;
                }

                /* set component, set action, set data */
                intent.setAction(action);
                intent.setData(u);
                intent.setComponent(component);

                Log.d(TAG, String.format("expt3/A | n {%d} type {%s} intent {%s} action {%s}",
                        numIntent, IPCType.toName(type), intent.getPackage(), action));

                /* Send Intent */
                String ex = sendIntentByType(intent, type, null);
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex): 0;

                    Log.d(TAG, String.format("expt3/A | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                    if (c > NUM_RUN_SKIP_EXCEPTION) {
                        Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                        return "Intents sent: " + numIntent;
                    }

                    map.put(ex, ++c);
                }

                numIntent++;
                count++;
            }

            if (numIntent % NUM_FREQ_GC == 0) {
                Log.d(TAG, "expt3/A | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IIIA");
                System.gc();
            }
        }
        Log.d(TAG, "expt3 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIIA");
        System.gc();

        /* reset map */
        map = new HashMap<>();

        Log.d(TAG, String.format("expt3/B | #uri-types {%d} #k {%d} #total{%d}",
                URI_TYPES.length, NUM_RANDOM, URI_TYPES.length * NUM_RANDOM));

        // Experiment 3(b): select a semi-valid Data and set Action random
        // all uri types except "content:"
        for(int i=1; i < URI_TYPES.length; i++) {
            Uri uri = ug.getUri(URI_TYPES[i], 0);
            for(int j=0; j < NUM_RANDOM; j++) {

                String action = new String(FuzzUtils.getRandomData(128, false));
                Intent intent = new Intent();

                /* set component, set data */
                intent.setAction(action);
                intent.setData(uri);
                intent.setComponent(component);

                Log.d(TAG, String.format("expt3/B | n {%d} type {%s} intent {%s} uri {%s}",
                        numIntent, IPCType.toName(type), intent.getPackage(), uri));

                /* Send Intent */
                String ex = sendIntentByType(intent, type, null);
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex): 0;

                    Log.d(TAG, String.format("expt3/B | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                    if (c > NUM_RUN_SKIP_EXCEPTION) {
                        Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                        return "Intents sent: " + numIntent;
                    }

                    map.put(ex, ++c);
                }

                numIntent++;

                if(numIntent % NUM_FREQ_GC == 0) {
                    Log.d(TAG, "expt3/B | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IIIB");
                    System.gc();
                }
            }
        }
        Log.d(TAG, "expt3/B | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIIB");
        System.gc();

        /* reset map */
        map = new HashMap<>();

        Log.d(TAG, String.format("expt3/C | #content {%d} #k {%d} #total{%d}",
                NUM_CONTENT, NUM_RANDOM, NUM_CONTENT * NUM_RANDOM));

        // uri type is "content:"
        // mKnownProviders.size() is replaced with a fixed count, we cannot test for all
        // content-providers, no time
        for(int i=0; i< NUM_CONTENT; i++) {

            Uri uri = ug.getUri(URI_TYPES[0],  rnd.nextInt(mKnownProviders.size())); // used to be i
            for(int j=0; j < NUM_RANDOM; j++) {

                String action = new String(FuzzUtils.getRandomData(128, false));
                Intent intent = new Intent();

                /* set component, set data */
                intent.setAction(action);
                intent.setData(uri);
                intent.setComponent(component);

                Log.d(TAG, String.format("expt3/C | n {%d} type {%s} intent {%s} uri {%s}",
                        numIntent, IPCType.toName(type), intent.getPackage(), uri));

                /* Send Intent */
                String ex = sendIntentByType(intent, type, null);
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex): 0;

                    Log.d(TAG, String.format("expt3/B | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                    if (c > NUM_RUN_SKIP_EXCEPTION) {
                        Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                        return "Intents sent: " + numIntent;
                    }

                    map.put(ex, ++c);
                }

                numIntent++;

                if(numIntent % NUM_FREQ_GC == 0) {
                    Log.d(TAG, "expt3/C | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IIIC");
                    System.gc();
                }
            }

        }
        Log.d(TAG, "expt3/C | ******** FORCING GARBAGE COLLECTION AFTER EXPT IIIC");
        System.gc();

        Log.d(TAG, "Sent " + numIntent + " Intents");
        return "Intents sent: " + numIntent;
    }

    /**
     * Campaign: Random extras for each action defined. We create a valid pair ({@code Action},
     * {@code Data}) with a set 1-5 {@code extra} fields with random values.
     * The effectiveness of this campaign is low, because most probably the component won't
     * be aware of these {@code extra} fields since their names are randomly generated.
     *
     * @param component the target {@link ComponentName}
     * @param type the type of the target {@link ComponentName}
     * @return
     */
    public String expt4 (ComponentName component, IPCType type) {

        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();

        Log.d(TAG, String.format("expt4 | #actions {%d} #k {%d} #total{%d}",
                ACTIONS.length, NUM_RANDOM, ACTIONS.length * NUM_RANDOM));

        Log.d(TAG, "expt4 | Fuzzing " +
                "actions {"  + ACTIONS.length  + "} " +
                "k {"        + NUM_RANDOM          + "} " +
                "total {"    + ACTIONS.length * NUM_RANDOM + "}");

        for (int k = 0; k < NUM_RANDOM; k++) {
            ActionDataPairs pairs = new ActionDataPairs(my.getApplicationContext());

            // for each action/data pair
            for(String action : ACTIONS) {
                Intent intent = new Intent();
                Uri uri = pairs.get(action);

                /* add component, add action, add data */
                intent.setAction(action);
                if (uri != null)
                    intent.setData(uri);
                intent.setComponent(component);

                /* set Extras */
                int num_extras = rnd.nextInt(5);
                for (int c=0; c < num_extras; c++) {
                    intent.putExtra(new String(FuzzUtils.getRandomData(64, false)),
                            FuzzUtils.getRandomData(256, false));
                }

                Log.d(TAG, String.format("expt4 | n {%d} type {%s} intent {%s} uri {%s} action {%s}",
                        numIntent, IPCType.toName(type), intent.getPackage(), uri, action));

                /* Send Intent */
                String ex = sendIntentByType(intent, type, null);
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex): 0;

                    Log.d(TAG, String.format("expt4 | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                    if (c > NUM_RUN_SKIP_EXCEPTION) {
                        Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                        return "Intents sent: " + numIntent;
                    }

                    map.put(ex, ++c);
                }

                numIntent++;

                if (numIntent % NUM_FREQ_GC == 0) {
                    try {
                        Log.d(TAG, "expt4 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                        System.gc();
                        Thread.sleep(NUM_DELAY_GC);
                    }
                    catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }

            }
        }
        Log.d(TAG, "expt4 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
        System.gc();

        System.err.println("expt4 | Sent "+numIntent+" Intents\n");
        return "Intents sent: "+numIntent;
    }

    /**
     * Campaign:
     * A valid action with the required extra fields, which consist of fuzzed values.
     *
     * @param component the target {@link ComponentName}
     * @param type the type of the target {@link ComponentName}
     * @return
     */

    public String expt5 (ComponentName component, IPCType type) {
        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (Action action : wearable_specs.getActions()) {
            List<Extra> extras = wearable_specs.getExtrafromAction(action.getName());
            if (extras == null)
                continue;
            Intent intent = FuzzUtils.getIntentSemiValidRandom(component, action, extras);

            Log.d(TAG, String.format("expt5 | n {%d} type {%s} intent {%s} action {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), action));
            String ex = sendIntentByType(intent, type, null);

            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex) : 0;

                Log.d(TAG, String.format("expt5 | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }

            numIntent++;
            if (numIntent % NUM_FREQ_GC == 0) {
                try {
                    Log.d(TAG, "expt5 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                    System.gc();
                    Thread.sleep(NUM_DELAY_GC);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            Log.d(TAG, "expt5 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
            System.gc();
        }
        Log.d(TAG, "expt5 | Sent " + numIntent + " Intents\n");
        return "Intents sent: " + numIntent;
    }

    /**
     * Campaign:
     * <ul>
     *     <li>A semi-valid pair {action, data}, with a valid action and a valid data of the
     *     expected MIME type. The data has randomly generated values.</li>
     *     <li>A semi-valid pair {action, data}, with a valid action and a valid data of a MIME
     *     type different from the one expected.</li>
     *     <li>A valid action with no other information, if the action does not require any
     *     additional data or extra fields.</li>
     * </ul>
     *
     * @param component the target {@link ComponentName}
     * @param type the type of the target {@link ComponentName}
     * @return
     */
    public String expt6 (ComponentName component, IPCType type) {
        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (Action action : wearable_specs.getActions()) {
            List<Extra> extras = wearable_specs.getExtrafromAction(action.getName());
            if (extras == null)
                continue;
            Intent intent = FuzzUtils.getIntentSemiValidRandom(component, action, extras);
            intent.setType(action.getMimeType()); // getMimeTypes returns either the expected Mime or */*
            intent.setData(FuzzUtils.getRandomUri(intent.getType()));
            Log.d(TAG, String.format("expt6 | n {%d} type {%s} intent {%s} action {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), action));
            String ex = sendIntentByType(intent, type, null);

            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex) : 0;

                Log.d(TAG, String.format("expt6 | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }

            numIntent++;
            if (numIntent % NUM_FREQ_GC == 0) {
                try {
                    Log.d(TAG, "expt6 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                    System.gc();
                    Thread.sleep(NUM_DELAY_GC);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            Log.d(TAG, "expt6 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
            System.gc();
        }
        Log.d(TAG, "expt6 | Sent " + numIntent + " Intents\n");
        return "Intents sent: " + numIntent;
    }

    public String expt7 (ComponentName component, IPCType type) {
        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (Action action : wearable_specs.getActions()) {
            List<Extra> extras = wearable_specs.getExtrafromAction(action.getName());
            if (extras == null)
                continue;
            Intent intent = FuzzUtils.getIntentSemiValidRandom(component, action, extras);
            intent.setType(FuzzUtils.pickOneMimeType(action.getMimeType()));
            intent.setData(FuzzUtils.getRandomUri(intent.getType()));
            Log.d(TAG, String.format("expt7 | n {%d} type {%s} intent {%s} action {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), action));
            String ex = sendIntentByType(intent, type, null);

            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex) : 0;

                Log.d(TAG, String.format("expt7 | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }

            numIntent++;
            if (numIntent % NUM_FREQ_GC == 0) {
                try {
                    Log.d(TAG, "expt7 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                    System.gc();
                    Thread.sleep(NUM_DELAY_GC);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            Log.d(TAG, "expt7 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
            System.gc();
        }
        Log.d(TAG, "expt7 | Sent " + numIntent + " Intents\n");
        return "Intents sent: " + numIntent;
    }
    /**
     * Campaign: Semi valid {@code Action}, {@code Data} and {@code Extra} fields.
     *
     * <ul>
     *     <li>Either a semi-valid {{@code action}, {@code data}} pair with fuzzed {@code Extra}
     *     field values.</li>
     * </ul>
     *
     * For this specific campaign, the fuzzer will only generates Intents that have an {@code Action}
     * with required or optional {@code Data} or {@Extra} Fields.
     *
     * Note. This is partial implementation.
     *
     * @param component the component that is being targeted by the fuzzer
     * @param type the type of the component (e.g., Activity, Service, Broadcaster Receiver)
     * @return
     */
    public String expt10 (ComponentName component, IPCType type) {

        int numIntent = 0;
        int gcCount = 0;
        HashMap<String, Integer> map = new HashMap<>();

        Log.d(TAG, String.format("expt10 | Part xxx #uri {%d} #actions {%d} #total{%d}",
                URI_TYPES.length, ACTIONS.length, URI_TYPES.length * ACTIONS.length));

        for (Action action : specs.getActions()) {

            List<Extra> extras = specs.getExtrafromAction(action.getName());
            if ( extras == null )
                continue;

            // FIXME. This has to be evaluated since we don't want an explossion of Intents w/option 1.
            // Depending of how many Extra field are associated to actions we should take a decision
            // of which option use. For now, option 2 is implemented.

            // Option 1.
            // Do all possible combination of Intents, varying in each combination the fuzzed value
            // (and type) of a different Extra field. For example, if we have 3 Extra fields and we
            // generates 4 diff vals (null, string, double, boolean) in total we will have:
            // 4 * 4 * 4 = 64 intents.
            // Option 2.
            // Create Intents varying the fuzz value (and type) of all the Extra field at the same
            // time. For the same example as above, we have 4 intents in total.

            //IntentSpecs.Extra[] extrasArray = (IntentSpecs.Extra[]) extrasList.toArray();

            // Generating combinations of intents using Option 2.
            // Note that case 0 is already considered in previous fuzzing campaign, so it is ignored
            // here.

            // FIXME. /eba/ 7/5/19 Devise the best way to exploit data types.
            // FIXME. /eba/ 7/10/19 Replace policy according to hypothesis in DummyActivity.
            //        The proposed plan is more effective than the actual implementation.
            //        Finish by 7/11/19.
            // For example, if the EXTRA_FIELD is type String, sending a number probably won't have
            // a huge impact. However, if instead the type is a Number, sending a String could
            // create some problems to the target application.
            Intent intent = FuzzUtils.getIntentSemiValidRandom(component, action, extras);
            Log.d(TAG, String.format("expt10 | n {%d} type {%s} intent {%s} action {%s}",
                    numIntent, IPCType.toName(type), intent.getPackage(), action));

            /* send intent */
            String ex = sendIntentByType(intent, type, null);

            // Control the number of exception per component.
            // If the # of exception is greater than NUM_RUN_SKIP_EXCEPTION, the experiment is
            // skipped
            if (ex != null) {
                int c = map.containsKey(ex) ? map.get(ex): 0;

                Log.d(TAG, String.format("expt10 | %s (%d/%d)",
                        ex, c, NUM_RUN_SKIP_EXCEPTION));

                /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                if (c > NUM_RUN_SKIP_EXCEPTION) {
                    Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                    return "Intents sent: " + numIntent;
                }

                map.put(ex, ++c);
            }

            numIntent++;
            if (numIntent % NUM_FREQ_GC == 0) {
                try {
                    Log.d(TAG, "expt10 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                    System.gc();
                    Thread.sleep(NUM_DELAY_GC);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            Log.d(TAG, "expt10 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
            System.gc();

        }


        // FIXME. /eba/ 7/5/19 Consider semi-valid compaigns with Action + Data

        Log.d(TAG, "expt10 | Sent "+numIntent+" Intents\n");
        return "Intents sent: "+numIntent;
    }

    /* ---------------------------------------------------------------------------
     *
     * ---------------------------------------------------------------------------
     */
    // FIXME Do a cleanup of this class
//    public String expt11 (ComponentName component, IPCType type) {
//        ResourcesManager rm = new ResourcesManager(this.context)
//        return "";
//    }


    public String expt21 (ComponentName component, IPCType type) {

        int numIntent = 0;
        HashMap<String, Integer> map = new HashMap<>();

        for(int i=1; i < URI_TYPES.length; i++) {

            Uri uri = ug.getUri(URI_TYPES[i], 0);
            for (Action action : specs.getActions()) {

                List<Extra> extras = specs.getExtrafromAction(action.getName());
                //if (extras == null)
                //    continue;

                Intent intent = FuzzUtils.getIntentSemiValidRandom(component, action, extras);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); // Omar Added
                Log.d(TAG, String.format("expt11 | n {%d} type {%s} intent {%s} action {%s}",
                        numIntent, IPCType.toName(type), intent.getPackage(), action));

                mResManager.getAvailableResources();

                /* send intent */
                String ex = null;
                if (action.isBroadcast()){
                    ex = sendIntentByType(intent, IPCType.BROADCASTS, null);
                }
                else if (action.isActivity()){
                    ex = sendIntentByType(intent, IPCType.ACTIVITIES, null);
                }

                // Control the number of exception per component.
                // If the # of exception is greater than NUM_RUN_SKIP_EXCEPTION, the experiment is
                // skipped
                if (ex != null) {
                    int c = map.containsKey(ex) ? map.get(ex) : 0;

                    Log.d(TAG, String.format("expt11 | %s (%d/%d)",
                            ex, c, NUM_RUN_SKIP_EXCEPTION));

                    /* skip fuzzing if we pass the limit of repeated case allowed for that exception */
                    if (c > NUM_RUN_SKIP_EXCEPTION) {
                        Log.d(TAG, "Skipping after " + numIntent++ + " Intents");
                        return "Intents sent: " + numIntent;
                    }

                    map.put(ex, ++c);
                }

                numIntent++;
                if (numIntent % NUM_FREQ_GC == 0) {
                    try {
                        Log.d(TAG, "expt11 | ******** FORCING GARBAGE COLLECTION WITHIN EXPT IV");
                        System.gc();
                        Thread.sleep(NUM_DELAY_GC);
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                Log.d(TAG, "expt11 | ******** FORCING GARBAGE COLLECTION AFTER EXPT IV");
                System.gc();

            }
        }

        Log.d(TAG, "expt11 | Sent "+numIntent+" Intents\n");
        return "Intents sent: " +numIntent;
    }


    /* ---------------------------------------------------------------------------
     *
     * ---------------------------------------------------------------------------
     */

    /**
     * Fuzz all components of a given type of IPC. The type must be matched to the currently
     * populated known components as this code is tightly coupled to the UIs implementation.
     *
     * @param type the type of IPC
     * @return String that gives a summary of what was done
     */
    public String fuzzAll(IPCType type, IFuzzType IFuzzType) throws Exception {
        Log.d(TAG, String.format("fuzzAll | {%s} {%s} init",
                IPCType.toName(type), IFuzzType.toName(IFuzzType)));

        int count;
        String out="";

        switch (IFuzzType) {
            case NULL:
                switch (type) {
                    case ACTIVITIES:
                    {
                        count = nullFuzzAllActivities(mKnownComponents);
                        out = "Started: " + count + " Activities";
                        break;
                    }

                    case BROADCASTS:
                    {
                        count = nullFuzzAllBroadcasts(mKnownComponents);
                        out = "Sent: " + count + " broadcasts";
                        break;
                    }

                    case SERVICES:
                    {
                        count = nullFuzzAllServices(mKnownComponents);
                        out = "Started: " + count + " services";
                        break;
                    }

                    default:
                        out = "Not Implemented";
                }
            case RANDOM:
            {
                sendIntentToAll(type);
                out = "Sent random Intent to all";
                break;
            }

            case SEMIVALID:
            {
                sendIntentToAll(type);
                out = "Sent semi-valid Intent to all";
                break;
            }

            default:
                out = "Not implemented.";
        }

        Log.d(TAG, String.format("fuzzAll | {%s} {%s} done",
                IPCType.toName(type), IFuzzType.toName(IFuzzType)));
        return out;
    }

    /**
     * Fuzz a single Component Name of a given type of IPC. The type must be matched to the
     * currently populated known components as this code is tightly couple to the UIs
     * implementation.
     *
     * @param type the type of IPC
     * @param clazz the classname of the Component Name
     * @return String that gives a summary of what was done
     */
    public String fuzzNullSingle(IPCType type, String clazz) {
        ComponentName toTest = null;
        Intent intent = new Intent();

        for (ComponentName c : mKnownComponents) {
            if (c.getClassName().equals(clazz)) {
                toTest = c;
                break;
            }
        }
        intent.setComponent(toTest);

        Log.d(TAG, String.format("fuzzNullSingle {%s} {%s} {%d}",
                clazz, toTest, mKnownComponents.size()));

        try {
            return sendIntentByType(intent, type);
        } catch (Throwable th) {

        }

        return "";
    }

    /**
     *
     * @param comps
     * @return
     * @throws Exception
     */
    protected int nullFuzzAllActivities(List<ComponentName> comps) throws Exception {
//        printComponentList(comps);
        int count = 0;
        int begin = 0;
        int limit = comps.size();

        for (int i = begin; i < limit; i++) {
            Intent in = new Intent();
            in.setComponent(comps.get(i));

            // Check if the classname is not in the black list
            if (isBlackListed(comps.get(i).getClassName())) {
                Log.d(TAG, "nullFuzzAllActivities | skipping activity: " + i);
                continue;
            } else {
                try {

                    Log.d(TAG, String.format("nullFuzzAllActivities | Null fuzzing activity: " +
                            "(%d of %d) %s", i, limit, comps.get(i).toString()));
                    Log.d(TAG, "nullFuzzAllActivities | I have current focus:  "
                            + my.hasWindowFocus());

                    my.startActivityForResult(in, 1999+i);
                    Thread.sleep(750);

                    // Dismiss any alert that popped up
                    Intent dismiss = new Intent();
                    dismiss.setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    my.sendBroadcast(dismiss);

                    my.finishActivity(1999+i);
                }
                catch (Exception ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    new Exception("Cannot launch: " + comps.get(i) + "\n" + ex.getMessage(), ex);
                }
            }
            count++;
        }
        return count;
    }

    /**
     *
     * @param comps
     * @return
     */
    int nullFuzzAllBroadcasts(List<ComponentName> comps) throws Exception {
        int count = 0;
        int begin = 0;
        int limit = comps.size();

        for (int i = begin; i < limit; i++) {
            Log.d(TAG, String.format("nullFuzzAllBroadcasts | Null fuzzing broadcast: " +
                    "(%d of %d) %s", i, limit, comps.get(i).toString()));

            Intent in = new Intent();
            in.setComponent(comps.get(i));
            try {
                my.sendBroadcast(in);
                Thread.sleep(500);
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
                new Exception("Cannot launch: " + comps.get(i) + "\n" + ex.getMessage(), ex);
            }
            count++;
        }
        return count;
    }

    /**
     *
     * @param comps
     * @return
     * @throws Exception
     */
    int nullFuzzAllServices(List<ComponentName> comps) throws Exception {
        int count = 0;
        int begin = 0;
        int limit = comps.size();

        for (int i = begin; i < limit; i++) {
            Log.d(TAG, String.format("nullFuzzAllServices | Null fuzzing services: " +
                    "(%d of %d) %s", i, limit, comps.get(i).toString()));

            Intent in = new Intent();
            in.setComponent(comps.get(i));
            try {
                my.startService(in);
                Thread.sleep(1000);
                my.stopService(in);
            } catch (Exception ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
                new Exception("Cannot launch: " + comps.get(i), ex);
            }

            count++;
        }
        return count;
    }



    /* ---------------------------------------------------------------------------
     * Intent helper
     * ---------------------------------------------------------------------------
     */

    public Activity my;

    private Intent buildRandomIntent(ComponentName toTest) {
        Intent i = new Intent();
        i.setComponent(toTest);

        /**************Fuzzing action**********/
        //i.setAction(getTypeString(iActions));
        i.setAction(new String(FuzzUtils.getRandomData(128, false)));
        /**************Fuzzing Uri*************/
        i.setData(FuzzUtils.getRandomUri("dumb"));
        /**************Fuzzing Extras**********/
        //i.putExtra(getTypeString(iExtras), getRandomData(512, false));
        int num_extras = rnd.nextInt(5);
        for(int c=0; c < num_extras; c++) {
            i.putExtra(new String(FuzzUtils.getRandomData(64, false)),
                    FuzzUtils.getRandomData(256, false));
        }

        //	i.putExtra("EXTRA_KEY_EVENT", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        //	i.putExtra("EXTRA_KEY_EVENT", new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        return i;
    }

    /**
     *
     * The function support fuzzing components other than activities.
     *
     * @param type
     * @return
     */
    public String sendIntent(IPCType type, String clazz) {

        ComponentName toTest = null;

        for (ComponentName c : mKnownComponents) {
            if (c.getClassName().equals(clazz)) {
                toTest = c;
                break;
            }
        }

        String outStr = "\nJust testing!\nPackage name: ";
        if(toTest != null)
        {
            outStr += toTest.getPackageName() + "\n" + clazz + "\n";

            for(int k=0; k<5; k++) {
                Intent intent = buildRandomIntent(toTest);
//                outStr += sendIntentByType(intent, type);
            }
        }
        return outStr;
    }

    //1-25, 25-70, 70-130, 130-185, 185-210, 210-250, 250-294
    public void sendIntentToAll(IPCType type)
    {

        for (String clazz : mComponentNames) {

            try {
                Log.d(TAG, String.format("sendItentToAll | Now fuzzing: %s", clazz));
                if (isBlackListed(clazz)) {
                    Log.d(TAG, String.format("sendIntentToAll | Skipping component {%s}", clazz));
                    continue;
                } else {
                    String out = sendIntent(type, clazz);
                    //mOut.append(out);
                }
            }
            catch (Exception ex) {
                Log.e(TAG, "An exception ocurred");
                Log.e(TAG, Log.getStackTraceString(ex));
            }
        }

//        for(int i=0; i < mKnownComponents.size(); i++)// mKnownComponents.size(); i++)
//        {
//            try{
//                mIntentSpin.setSelection(i);
//                System.err.println("-----------------------------------------------------------\n");
//                System.err.println("Now fuzzing: "+i+" "+mIntentSpin.getSelectedItem().toString()+"\n");
//                System.err.println("-----------------------------------------------------------\n");
//                if(isBlackListed(mIntentSpin.getSelectedItem().toString()))//mIntentSpin.getSelectedItem().toString()))
//                {
//                    System.err.println("**************Skipping component: "+i+"\n");
//                    continue;
//                }
//                else
//                {
//                    String out = sendIntent(type);
//                    mOut.append(out);
//                }
//            }
//            catch(Exception ex)
//            {
//                System.err.println("**************An exception occurred "+ex.getMessage()+"\n");
//            }
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: {" + data + "}");
    }


    /**
     * Initiate an IPC of a given type from the Activity by sending an Intent.
     * Invoked internally by fuzzer methods.
     *
     * @param intent the Intent that is going to be sent.
     * @param type the type of IPC.
     * @param count the map to keep track of each Exception count during the experiment.
     *
     * @return {@link String} that gives a summary the action.
     */
    private String sendIntentByType(Intent intent, IPCType type, Map<String, Integer> count) {

        try {

            if ( POC_MODE == 3 ) {
                // check if the app is not under timeout
                // if we have superpass the limit in the timeframe, throw the exception
                DUtils.checkThreshold();
            }

            sendIntentByType(intent, type);

        } catch (ThresholdSecurityException ex) {
            Log.e(TAG, "sendIntent | #POCThresholdSecurityException POC security has been raised", ex);
            return ex.getClass().getSimpleName();
        } catch (SecurityException ex) {
            Log.e(TAG, "sendIntent | #SecurityException A security exception occurred while fuzzing", ex);
            return ex.getClass().getSimpleName();
        } catch (InterruptedException ex) {
            Log.e(TAG, "sendIntent | #InterruptedException An interrupted exception occurred while fuzzing", ex);
            return ex.getClass().getSimpleName();
        } catch (Exception ex) {
            Log.e(TAG, "sendIntent | #Exception An exception occurred while fuzzing", ex);
            return ex.getClass().getSimpleName();
        } catch (Error e) {
            Log.e(TAG, "sendIntent | #Error An error occurred while fuzzing", e);
            return e.getClass().getSimpleName();
        } catch (Throwable t) {
            Log.e(TAG, "SendIntent | #Throwable An unexpected error ocurred while fuzzing", t);
            return t.getClass().getSimpleName();
        }

        // No Exception (null)
        return null;
    }

    // new
    private String sendIntentByType(Intent intent, IPCType type) throws Throwable {

        DUtils.log("SendIntentbyType | " + intent);

        try {

            // Initiate IPC according component type
            switch (type) {
                case ACTIVITIES:

                    int code = 1999 + intent.getComponent().hashCode();
                    DUtils.logp(Constants.POC_TAG_INTENT_SEND, intent);
                    my.startActivityForResult(intent, code);
                    Thread.sleep(NUM_DELAY_START_ACT);
                    my.finishActivity(code);

                    DUtils.log("SendIntentbyType | Started Activity: {" + intent.getComponent() + "}");
                    return "Started: " + intent.getPackage();

                case BROADCASTS:
                    DUtils.logp(Constants.POC_TAG_INTENT_SEND, intent);
                    my.sendBroadcast(intent);
                    Thread.sleep(NUM_DELAY_START_BST);

                    DUtils.log("SendIntentbyType | Sent broadcast: {" + intent.getPackage() + "}");
                    return "Sent broadcast: " + intent.getPackage();

                case SERVICES:
                    my.startService(intent);
                    Thread.sleep(NUM_DELAY_START_SRV);
                    // Stopping service
                    my.stopService(intent);

                    Log.d(TAG, "SendIntentbyType | Started service: {" + intent.getPackage() + "}");
                    return "Started: " + intent.getPackage();

                case PROVIDERS:
                    // uh - providers don't use Intents...what am I doing...
                    Log.d(TAG, "Not implemeted");
                    return "Not Implemented";

                case INSTRUMENTATIONS:
                    my.startInstrumentation(intent.getComponent(), null, null);
                    // not intent based you could fuzz these params, if anyone cared.
                    Log.d(TAG, "Not implemeted");
                    return "Not Implemented";
            }
        }
        catch (Throwable ex) {
            throw ex;
        }

        return "";
    }

    protected Intent fuzzBroadcast(ComponentName toTest) {
        Intent i = new Intent();
        i.setComponent(toTest);
        sendBroadcast(i);
        return i;
    }


}