package edu.purdue.dagobah.common;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;

import com.google.android.gms.fitness.data.DataType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.content.Intent.ACTION_VIEW;

/**
 * Intent Fuzzer's Constants.
 *
 * FIXME 5/23 The need of this class needs to be corrected.
 * Some of these values need to be passed as arguments to avoid compile/package everytime we need
 * to test an application.
 *
 * @author ebarsallo
 */

public class Constants {

    /* ---------------------------------------------------------------------------
     * Server Configuration
     * ---------------------------------------------------------------------------
     */
    public static final int SERVER_PORT = 7332;

    /* ---------------------------------------------------------------------------
     * Overall
     * ---------------------------------------------------------------------------
     */
    public static final long seed = System.currentTimeMillis();
    public static final Random rnd = new Random(seed);

    public final static long START_TIME = System.currentTimeMillis();
    public static int gNumIntents = 0;
    public static int gSensorReads = 0;

    /* ---------------------------------------------------------------------------
     * PoC
     * ---------------------------------------------------------------------------
     */

    /** params (configuration) */
    // 0: Defender using Intents, buffer limit
    // 1: Defender using B/cast, buffer limit
    // 2: Defender inside the attacker, buffer limit
    // 3: Defender inside the attacker, drop messages
    public static final int POC_MODE = -1;
    public static final String POC_DEF_PKG = "edu.purdue.yavin";
    public static final String POC_DEF_CLZ = "edu.purdue.yavin.MainActivity";

    /** constants */
    public static boolean DEBUG_MODE = true;
    public static final String POC_TAG = "PoCTAG";
    public static final String POC_TAG_INTENT_SEND = "0";
    public static final String POC_TAG_INTENT_ADD_BUF = "1";
    public static final String POC_TAG_INTENT_FWD = "2";
    public static final String POC_TAG_INTENT_DROP = "3";

    public static final String POC_TAG_EXTRAID = "PoCExtraID";

    /** intent tags */
    public static final String POC_INTENT_BCAST_ACTION = "edu.purdue.ryloth.action.INTENT";
    public static final String POC_INTENT_BCAST_CAT    = "edu.purdue.ryloth.category.POC";
    public static final String POC_INTENT_EXTRA_ACTION =  "PoC_ACTION";
    public static final String POC_INTENT_EXTRA_DATA   =  "PoC_DATA";
    public static final String POC_INTENT_EXTRA_CMP_PKG = "PoC_CMP_PACKAGE";
    public static final String POC_INTENT_EXTRA_CMP_CLZ = "PoC_CMP_CLASS";

    /** data structs */
    public static List<Intent> gIntentBuffer = new ArrayList<>();
    public static final int BUFFER_SIZE = 50000;            // unlimited buffer
    public static final boolean POC_PARAM_COMM_ACTIVATED = false;
    public static int POC_REAL_SENSOR_ACTIVATED = 0;

    public static Activity myActivity = null;

    /** stats for dynamic PoC */
    public static final int POC_PARAM_DELAY_INTENT = 4000;

    public static final int POC_PARAM_INTENT_MAX = 50;      //
    public static final int POC_PARAM_INTENT_WINDOW = 1;    //
    public static final int POC_PARAM_TIMEOUT = 60;         // timeout in secs

    public static HashMap<Long, Integer> gIntentCount = new HashMap<>();
    public static long gTimeout = 0;


    /** stats */
    public static int gNumber  = 0;
    public static int gDropped = 0;

    /** sensors */
    // Sensor rate
    //  * SensorManager.SENSOR_DELAY_FASTEST:     0ms
    //  * SensorManager.SENSOR_DELAY_GAME:       20ms
    //  * SensorManager.SENSOR_DELAY_DELAY_UI:   67ms
    //  * SensorManager.SENSOR_DELAY_NORMAL:    200ms
    public static int POC_PARAM_SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
    // Sensor # activated
    //  * use 0 for no sensors
    //  * use a big number, such as 99, for all sensors in the device
    //  * or any specific # according to the constant defined in the invoker. Consider that
    //    for after 5 sensors, the increments should be in 5.
    public static int POC_PARAM_SENSOR_ACTIVATED = 0;

    /** communication */
    public static boolean POC_PARAM_SYNC_ENABLED = true;

    // Size of data chunks sync between devices
    public static int POC_PARAM_COMM_SIZE = 0;


    /* ---------------------------------------------------------------------------
     * Intent Fuzzing
     * ---------------------------------------------------------------------------
     */

    // # of intents needed before call Garbage Collector (e.g., 100 for DSN '18)
    public static final int NUM_FREQ_GC = 300;
    // # of URI to generate (experiment 1)
    public static final int NUM_RANDOM = 3;
    // # of random content URI to be generated
    public static final int NUM_CONTENT = 5;
    // # of same exception accepted for each component
    public static final int NUM_RUN_SKIP_EXCEPTION = 10;
    // # of repeated exception accepted (default)
    public static final int NUM_RUN_SKIP_EXCEPTION_DEFAULT = 99999;
    // # of repeated exception accepted (per exception)
    public static final Map<String, Integer> NUM_RUN_SKIP_PER_EXCEPTION;
    static
    {
        NUM_RUN_SKIP_PER_EXCEPTION = new HashMap<>();
        NUM_RUN_SKIP_PER_EXCEPTION.put("SecurityException", 2);
    }

    // # of ms of delay every time the Garbage Collector is invoked
    public static final int NUM_DELAY_GC = 100;
    // # of ms of delay every time an Activity is started
    public static final int NUM_DELAY_START_ACT = 100;          // before: 300
    // # of ms of delay every time an Broadcast is started
    public static final int NUM_DELAY_START_BST = 0;
    // # of ms of delay every time an Activity is started
    public static final int NUM_DELAY_START_SRV = 100;
    // # of intent to repeat over and over
    public static final int NUM_INTENT_REPEAT = -1;

    // Provider Authority
    public static final String PROVIDER_AUTHORITY = "edu.purdue.dagobah.ryloth.common.provider";


    public static final String TAGF = "ryloth/mm";


    /*
     String constants from Android Intent Documentation
     */

    // Filename of the json with the Android SDK Specification for Intents
    public static final String JSON_SDK_FILENAME = "specs";
    public static final String WEARABLE_JSON_SDK_FILENAME = "wearable_specs";

    // JSON parent tags
    public static final String JSON_SDK_ACTION = "action";
    public static final String JSON_SDK_DATA   = "data";
    public static final String JSON_SDK_EXTRA_FIELD         = "extra_field";
    public static final String JSON_SDK_ACTION_EXTRA_FIELDS = "action_extra";

    // wearable specific actions
    public static String[] ACTIONS_DEBUG = {
            "android.intent.action.BUG_REPORT"
    };

    // FIXME eba 7/19/19 Most of the constants that are below are going to be replaced by the new classes
    public static String[] ACTIONS = {
            "android.app.action.ADD_DEVICE_ADMIN",
            "android.app.action.MANAGED_PROFILE_PROVISIONED",
            "android.app.action.PROVISIONING_SUCCESSFUL",
            "android.app.action.PROVISION_MANAGED_DEVICE",
            "android.app.action.PROVISION_MANAGED_PROFILE",
            "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD",
            "android.app.action.SET_NEW_PASSWORD",
            "android.app.action.START_ENCRYPTION",
            "android.appwidget.action.APPWIDGET_BIND",
            "android.appwidget.action.APPWIDGET_CONFIGURE",
            "android.appwidget.action.APPWIDGET_PICK",
            "android.bluetooth.adapter.action.REQUEST_DISCOVERABLE",
            "android.bluetooth.adapter.action.REQUEST_ENABLE",
            "android.content.pm.action.CONFIRM_PIN_APPWIDGET",
            "android.content.pm.action.CONFIRM_PIN_SHORTCUT",
            "android.content.pm.action.SESSION_DETAILS",
            "android.intent.action.ALL_APPS",
            "android.intent.action.ANSWER",
            "android.intent.action.APPLICATION_PREFERENCES",
            "android.intent.action.APP_ERROR",
            "android.intent.action.ASSIST",
            "android.intent.action.ATTACH_DATA",
            "android.intent.action.BUG_REPORT",
            "android.intent.action.CALL",
            "android.intent.action.CALL_BUTTON",
            "android.intent.action.CARRIER_SETUP",
            "android.intent.action.CHOOSER",
            "android.intent.action.CREATE_DOCUMENT",
            "android.intent.action.CREATE_LIVE_FOLDER",
            "android.intent.action.CREATE_SHORTCUT",
            "android.intent.action.DELETE",
            "android.intent.action.DIAL",
            "android.intent.action.DISMISS_ALARM",
            "android.intent.action.EDIT",
            "android.intent.action.EVENT_REMINDER",
            "android.intent.action.FACTORY_TEST",
            "android.intent.action.GET_CONTENT",
            "android.intent.action.INSERT",
            "android.intent.action.INSERT_OR_EDIT",
            "android.intent.action.INSTALL_FAILURE",
            "android.intent.action.INSTALL_PACKAGE",
            "android.intent.action.MAIN",
            "android.intent.action.MANAGE_NETWORK_USAGE",
            "android.intent.action.MEDIA_SEARCH",
            "android.intent.action.MUSIC_PLAYER",
            "android.intent.action.OPEN_DOCUMENT",
            "android.intent.action.OPEN_DOCUMENT_TREE",
            "android.intent.action.PASTE",
            "android.intent.action.PICK",
            "android.intent.action.PICK_ACTIVITY",
            "android.intent.action.POWER_USAGE_SUMMARY",
            "android.intent.action.PROCESS_TEXT",
            "android.intent.action.QUICK_VIEW",
            "android.intent.action.RINGTONE_PICKER",
            "android.intent.action.RUN",
            "android.intent.action.SEARCH",
            "android.intent.action.SEARCH_LONG_PRESS",
            "android.intent.action.SEND",
            "android.intent.action.SENDTO",
            "android.intent.action.SEND_MULTIPLE",
            "android.intent.action.SET_ALARM",
            "android.intent.action.SET_TIMER",
            "android.intent.action.SET_WALLPAPER",
            "android.intent.action.SHOW_ALARMS",
            "android.intent.action.SHOW_APP_INFO",
            "android.intent.action.SHOW_TIMERS",
            "android.intent.action.SNOOZE_ALARM",
            "android.intent.action.SYNC",
            "android.intent.action.SYSTEM_TUTORIAL",
            "android.intent.action.UNINSTALL_PACKAGE",
            "android.intent.action.VIEW",
            "android.intent.action.VIEW_DOWNLOADS",
            "android.intent.action.VOICE_COMMAND",
            "android.intent.action.WEB_SEARCH",
            "android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL",
            "android.media.action.IMAGE_CAPTURE",
            "android.media.action.IMAGE_CAPTURE_SECURE",
            "android.media.action.MEDIA_PLAY_FROM_SEARCH",
            "android.media.action.STILL_IMAGE_CAMERA",
            "android.media.action.STILL_IMAGE_CAMERA_SECURE",
            "android.media.action.TEXT_OPEN_FROM_SEARCH",
            "android.media.action.VIDEO_CAMERA",
            "android.media.action.VIDEO_CAPTURE",
            "android.media.action.VIDEO_PLAY_FROM_SEARCH",
            "android.media.tv.action.REQUEST_CHANNEL_BROWSABLE",
            "android.net.conn.CAPTIVE_PORTAL",
            "android.net.scoring.CHANGE_ACTIVE",
            "android.net.scoring.CUSTOM_ENABLE",
            "android.net.wifi.PICK_WIFI_NETWORK",
            "android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE",
            "android.nfc.action.NDEF_DISCOVERED",
            "android.nfc.action.TAG_DISCOVERED",
            "android.nfc.action.TECH_DISCOVERED",
            "android.nfc.cardemulation.action.ACTION_CHANGE_DEFAULT",
            "android.os.storage.action.MANAGE_STORAGE",
            "android.provider.MediaStore.RECORD_SOUND",
            "android.provider.Telephony.ACTION_CHANGE_DEFAULT",
            "android.provider.action.QUICK_CONTACT",
            "android.provider.action.VOICE_SEND_MESSAGE_TO_CONTACTS",
            "android.provider.calendar.action.HANDLE_CUSTOM_EVENT",
            "android.search.action.SEARCH_SETTINGS",
            "android.service.wallpaper.CROP_AND_SET_WALLPAPER",
            "android.settings.ACCESSIBILITY_SETTINGS",
            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS",
            "android.settings.ACTION_PRINT_SETTINGS",
            "android.settings.ADD_ACCOUNT_SETTINGS",
            "android.settings.AIRPLANE_MODE_SETTINGS",
            "android.settings.APN_SETTINGS",
            "android.settings.APPLICATION_DETAILS_SETTINGS",
            "android.settings.APPLICATION_DEVELOPMENT_SETTINGS",
            "android.settings.APPLICATION_SETTINGS",
            "android.settings.APP_NOTIFICATION_SETTINGS",
            "android.settings.BATTERY_SAVER_SETTINGS",
            "android.settings.BLUETOOTH_SETTINGS",
            "android.settings.CAPTIONING_SETTINGS",
            "android.settings.CAST_SETTINGS",
            "android.settings.CHANNEL_NOTIFICATION_SETTINGS",
            "android.settings.DATA_ROAMING_SETTINGS",
            "android.settings.DATE_SETTINGS",
            "android.settings.DEVICE_INFO_SETTINGS",
            "android.settings.DISPLAY_SETTINGS",
            "android.settings.DREAM_SETTINGS",
            "android.settings.HARD_KEYBOARD_SETTINGS",
            "android.settings.HOME_SETTINGS",
            "android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS",
            "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS",
            "android.settings.INPUT_METHOD_SETTINGS",
            "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS",
            "android.settings.INTERNAL_STORAGE_SETTINGS",
            "android.settings.LOCALE_SETTINGS",
            "android.settings.LOCATION_SOURCE_SETTINGS",
            "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS",
            "android.settings.MANAGE_APPLICATIONS_SETTINGS",
            "android.settings.MANAGE_DEFAULT_APPS_SETTINGS",
            "android.settings.MANAGE_UNKNOWN_APP_SOURCES",
            "android.settings.MEMORY_CARD_SETTINGS",
            "android.settings.NETWORK_OPERATOR_SETTINGS",
            "android.settings.NFCSHARING_SETTINGS",
            "android.settings.NFC_PAYMENT_SETTINGS",
            "android.settings.NFC_SETTINGS",
            "android.settings.NIGHT_DISPLAY_SETTINGS",
            "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS",
            "android.settings.PRIVACY_SETTINGS",
            "android.settings.QUICK_LAUNCH_SETTINGS",
            "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
            "android.settings.REQUEST_SET_AUTOFILL_SERVICE",
            "android.settings.SECURITY_SETTINGS",
            "android.settings.SETTINGS",
            "android.settings.SHOW_REGULATORY_INFO",
            "android.settings.SOUND_SETTINGS",
            "android.settings.SYNC_SETTINGS",
            "android.settings.USAGE_ACCESS_SETTINGS",
            "android.settings.USER_DICTIONARY_SETTINGS",
            "android.settings.VOICE_CONTROL_AIRPLANE_MODE",
            "android.settings.VOICE_CONTROL_BATTERY_SAVER_MODE",
            "android.settings.VOICE_CONTROL_DO_NOT_DISTURB_MODE",
            "android.settings.VOICE_INPUT_SETTINGS",
            "android.settings.VPN_SETTINGS",
            "android.settings.VR_LISTENER_SETTINGS",
            "android.settings.WEBVIEW_SETTINGS",
            "android.settings.WIFI_IP_SETTINGS",
            "android.settings.WIFI_SETTINGS",
            "android.settings.WIRELESS_SETTINGS",
            "android.settings.ZEN_MODE_PRIORITY_SETTINGS",
            "android.settings.action.MANAGE_OVERLAY_PERMISSION",
            "android.settings.action.MANAGE_WRITE_SETTINGS",
            "android.speech.tts.engine.CHECK_TTS_DATA",
            "android.speech.tts.engine.GET_SAMPLE_TEXT",
            "android.speech.tts.engine.INSTALL_TTS_DATA",
            "android.telephony.action.CONFIGURE_VOICEMAIL",
            "android.telephony.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS",
            "android.telephony.euicc.action.PROVISION_EMBEDDED_SUBSCRIPTION",
            "com.android.contacts.action.FILTER_CONTACTS",
    };

    public static String[] CATEGORIES;

    public static String[] EXTRAS = {
            "EXTRA_ALARM_COUNT",
            "EXTRA_BCC",
            "EXTRA_CC",
            "EXTRA_CHANGED_COMPONENT_NAME",
            "EXTRA_DATA_REMOVED",
            "EXTRA_DOCK_STATE",
            "EXTRA_DOCK_STATE_CAR",
            "EXTRA_DOCK_STATE_DESK",
            "EXTRA_DOCK_STATE_UNDOCKED",
            "EXTRA_DONT_KILL_APP",
            "EXTRA_EMAIL",
            "EXTRA_INITIAL_INTENTS",
            "EXTRA_INTENT",
            "EXTRA_KEY_EVENT",
            "EXTRA_PHONE_NUMBER",
            "EXTRA_REMOTE_INTENT_TOKEN",
            "EXTRA_REPLACING",
            "EXTRA_SHORTCUT_ICON",
            "EXTRA_SHORTCUT_ICON_RESOURCE",
            "EXTRA_SHORTCUT_INTENT",
            "EXTRA_STREAM",
            "EXTRA_SHORTCUT_NAME",
            "EXTRA_SUBJECT",
            "EXTRA_TEMPLATE",
            "EXTRA_TEXT",
            "EXTRA_TITLE",
            "EXTRA_UID",
    };

    // source: http://f-del.com/blog/list-of-android-mime-types-and-uris
    // original url is down (2017-09-28)
    // http://web.archive.org/web/20111219175703/http://f-del.com/blog/list-of-android-mime-types-and-uris

    public static String[] MIME_TYPES = {
            "application/vnd.android.package-archive",
            "media/*",
            "audio/*",
            "video/*",
            "application/ogg",
            "application/x-ogg",
            "application/atom+xml",
            "application/rss+xml",
            "vnd.android.cursor.item/*",
            "vnd.android.cursor.dir/*",
            "vnd.google.fitness.activity/*"

    };

    public  static String[] WEARABLE_MIME_TYPES = {
            "vnd.google.fitness.activity/biking",
            "vnd.google.fitness.activity/running",
            "vnd.google.fitness.activity/other",
            "vnd.google.fitness.data_type/com.google.heart_rate.bpm",
            "vnd.google.fitness.data_type/com.google.step_count.cumulative",
    };

    public static String[] URI_TYPES = {
            "content://",
            "file://",
            "folder://",
            "directory://",
            "geo:",
            "google.streetview:",
            "http://",
            "https://",
            "mailto:",
            "ssh:",
            "tel:",
            "voicemail:"
    };

    // list of components that are avoided during bulk testing
    // TODO: review this list
    public static String[] BLACKLIST = {
            "android.accounts.GrantCredentialsPermissionActivity",
            "com.android.settings.AccountSyncSettings",
            "com.android.settings.AccountSyncSettingsInAddAccount",
            "com.android.voicedialer.VoiceDialerActivity",
            "com.android.cardock.CarDockActivity",
            "com.android.carhome.CarHome",
            "com.android.providers.media.MediaScannerService",

            // google api
//            "com.google.android.gms.common.api.GoogleApiActivity",
//            "com.google.android.gms.appinvite.PreviewActivity",
//            "com.google.android.gms.auth.api.signin.internal.SignInHubActivity",
//
//            "android.support.wearable.activity.ConfirmationActivity",

            // squibble components
            "edu.purdue.android.fuzzer.squibble.common.IntentFuzzer",
            "edu.purdue.android.fuzzer.squibble.MainActivity",
            "edu.purdue.android.fuzzer.squibble.IntentFuzzerListenerService",
            "edu.purdue.android.fuzzer.squibble.IntentFuzzerService",
            "edu.purdue.android.fuzzer.squibble.WearClient",
    };


    public static DataType[] DATATYPES = {
            DataType.TYPE_HEART_RATE_BPM,
            DataType.TYPE_HEART_POINTS
    };


    /* ---------------------------------------------------------------------------
     * Target Application lists
     * ---------------------------------------------------------------------------
     */

    public static String TARGET_APP = "com.dungelin.heartrate";

    public static String TARGET_APPS[] = {
            "com.tencent.mm"
//            "com.google.android.apps.fitness",
//            "com.strava",
//            "com.citymapper.app.release",
//            "com.fitnesskeeper.runkeeper.pro",
//            "com.runtastic.android",
//            "se.perigee.android.seven",
//            "net.nurik.roman.muzei",
//            "com.dungelin.heartrate",
//            "com.northpark.drinkwater",
//            "ch.publisheria.bring"
    };

//    public static String TARGET_BUILTIN [] = {
//            "com.android.providers.telephony",
//            "com.android.externalstorage",
//            "com.android.mms.service",
//            "com.android.defcontainer",
//            "com.android.pacprocessor",
//            "com.android.carrierconfig",
//            "com.android.providers.settings",
//            "com.android.sharedstoragebackup",
//            "com.android.sdm.plugins.diagmon",
//            "com.android.dreams.basic",
//            "com.android.inputdevices",
//            "com.android.calllogbackup",
//            "com.android.proxyhandler",
//            "com.android.sdm.plugins.dcmo",
//            "com.android.providers.partnerbookmarks",
//            "com.android.bookmarkprovider",
//            "com.android.wallpaperbackup",
//            "com.android.providers.blockednumber",
//            "com.android.providers.userdictionary",
//            "com.android.location.fused",
//            "com.android.bluetoothmidiservice",
//            "com.android.statementservice",
//            "com.android.sdm.plugins.connmo",
//            "com.android.providers.media",
//            "com.android.wallpapercropper",
//            "com.android.htmlviewer",
//            "com.android.providers.downloads",
//            "com.android.providers.downloads.ui",
//            "com.android.hotwordenrollment",
//            "com.android.retaildemo",
//            "com.android.keychain",
//            "com.android.cts.ctsshim",
//            "com.android.shell",
//            "com.android.providers.contacts",
//            "com.android.captiveportallogin",
//            "com.android.mtp",
//            "com.android.backupconfirm"
//    };
//
//    public static String TOP_APPS_NON_BUILTIN [] = {
//            "com.facebook.orca",
//            "com.whatsapp",
//            "com.instagram.android",
//            "com.snapchat.android",
//            "com.google.android.apps.books",
//            "com.google.android.apps.magazines",
//            "com.sec.spp.push",
//            "com.google.android.street",
//            "com.skype.raider"
//    };
//
//
//    public static String PREFIX_HEALTH_APPS [] = {
//            "com.dungelin.heartrate",
//            "com.fitnesskeeper.runkeeper.pro",
//            "com.google.android.apps.fitness",
//            "com.jwork.wearable.heartratesync2",
//            "com.motorola.omni",
//            "com.runtastic.android",
//            "com.sonymobile.lifelog",
//            "com.strava",
//            "se.perigee.android.seven"
//    };
//
//    public static String PREFIX_ANDROID_WEAR_APPS [] = {
//            "ch.publisheria.bring",
//            "com.accuweather.android",
//            "com.aita",
//            "com.augmentra.viewranger.android",
//            "com.citymapper.app.release",
//            "com.clearchannel.iheartradio.controller",
//            "com.glidetalk.glideapp",
//            "com.google.android.wearable.app",
//            "com.hole19golf.hole19.beta",
//            "com.hotellook",
//            "com.joelapenna.foursquared",
//            "com.mobilefootie.wc2010",
//            "com.northpark.drinkwater",
//            "com.shazam.android",
//            "com.strava",
//            "com.weather.Weather",
//            "se.perigee.android.seven"
//    };
//
//    public static String PREFIX_ANDROID_WEAR_2_APPS [] = {
//            "ch.publisheria.bring",
//            "com.accuweather.android",
//            "com.fitnesskeeper.runkeeper.pro",
//            "com.glidetalk.glideapp",
//            "com.google.android.apps.fitness",
//            "com.joelapenna.foursquared",
//            "com.strava",
//            "com.weather.Weather",
//            "se.perigee.android.seven"
//    };

}
