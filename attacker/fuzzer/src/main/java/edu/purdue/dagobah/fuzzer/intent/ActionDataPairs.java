package edu.purdue.dagobah.fuzzer.intent;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static edu.purdue.dagobah.common.Constants.PROVIDER_AUTHORITY;

/**
 * @author Amiya Mayi
 * @author ebarsallo
 *
 * Actions defined on Android documentation.
 *
 * @see <a url="https://developer.android.com/reference/android/content/Intent.html"></a>
 */

public class ActionDataPairs {

    private String currentDir;

    Map<String, Uri> adp = new HashMap<String, Uri>();
    Context context = null;

    public ActionDataPairs(Context context) {

        // Set context
        this.context = context;
        // Set path where dummy files will be stored
        currentDir = this.context.getFilesDir().getPath();

        // Init data pairs
        adp.put("ACTION_AIRPLANE_MODE_CHANGED",null);
        adp.put("ACTION_ALL_APPS",null);
        adp.put("ACTION_ANSWER",null);
        adp.put("ACTION_APP_ERROR",null);
        adp.put("ACTION_ATTACH_DATA", getFileUri("photos/100001.jpg"));
        adp.put("ACTION_BATTERY_CHANGED",null);
        adp.put("ACTION_BATTERY_LOW",null);
        adp.put("ACTION_BATTERY_OKAY",null);
        adp.put("ACTION_BOOT_COMPLETED",null);
        adp.put("ACTION_BUG_REPORT",null);
        adp.put("ACTION_CALL", Uri.parse("content://contacts/people/1"));
        adp.put("ACTION_CALL_BUTTON",null);
        adp.put("ACTION_CAMERA_BUTTON",null);
        adp.put("ACTION_CHOOSER",null);
        adp.put("ACTION_CLOSE_SYSTEM_DIALOGS",null);
        adp.put("ACTION_CONFIGURATION_CHANGED",null);
        adp.put("ACTION_CREATE_SHORTCUT",null);
        adp.put("ACTION_DATE_CHANGED",null);
        adp.put("ACTION_DEFAULT", Uri.parse("content://contacts/people/1"));
        adp.put("ACTION_DELETE", getFileUri("photos/100001.jpg"));
        adp.put("ACTION_DEVICE_STORAGE_LOW",null);
        adp.put("ACTION_DEVICE_STORAGE_OK",null);
        adp.put("ACTION_DIAL", Uri.parse("tel:123-456-7890"));
        adp.put("ACTION_DOCK_EVENT",null);
        adp.put("ACTION_EDIT", Uri.parse("content://contacts/people/1"));
        adp.put("ACTION_EXTERNAL_APPLICATIONS_AVAILABLE",null);
        adp.put("ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE",null);
        adp.put("ACTION_FACTORY_TEST",null);
        adp.put("ACTION_GET_CONTENT",null); // this only takes mime-type
        adp.put("ACTION_GTALK_SERVICE_CONNECTED",null);
        adp.put("ACTION_GTALK_SERVICE_DISCONNECTED",null);
        adp.put("ACTION_HEADSET_PLUG",null);
        adp.put("ACTION_INPUT_METHOD_CHANGED",null);
        adp.put("ACTION_INSERT", Uri.parse("directory://data/data/com.android.IntentFuzzer/"));
        adp.put("ACTION_INSERT_OR_EDIT",null); // uses mime-type and extras
        adp.put("ACTION_INSTALL_PACKAGE", getFileUri("apps/dummyapp.apk"));
        adp.put("ACTION_LOCALE_CHANGED",null);
        adp.put("ACTION_MAIN",null);
        adp.put("ACTION_MANAGE_NETWORK_USAGE",null);
        adp.put("ACTION_MANAGE_PACKAGE_STORAGE",null);
        adp.put("ACTION_MEDIA_BAD_REMOVAL", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_BUTTON",null);
        adp.put("ACTION_MEDIA_CHECKING", Uri.parse("folder://media/sdcard/songs/"));
        adp.put("ACTION_MEDIA_EJECT", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_MOUNTED", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_NOFS", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_REMOVED", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_SCANNER_FINISHED", Uri.parse("folder://media/sdcard/songs/"));
        adp.put("ACTION_MEDIA_SCANNER_SCAN_FILE", getFileUri("extras/song5.mp3"));
        adp.put("ACTION_MEDIA_SCANNER_STARTED", Uri.parse("folder://media/sdcard/songs/"));
        adp.put("ACTION_MEDIA_SHARED", Uri.parse("folder://media/sdcard/shared/"));
        adp.put("ACTION_MEDIA_UNMOUNTABLE", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MEDIA_UNMOUNTED", Uri.parse("folder://media/sdcard/"));
        adp.put("ACTION_MY_PACKAGE_REPLACED",null);
        adp.put("ACTION_NEW_OUTGOING_CALL",null);
        adp.put("ACTION_PACKAGE_ADDED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_CHANGED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_DATA_CLEARED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_FIRST_LAUNCH", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_FULLY_REMOVED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_INSTALL", Uri.parse("https://developer.google.com/mirror1/tree1/branch1/somepackage.apk"));
        adp.put("ACTION_PACKAGE_NEEDS_VERIFICATION", Uri.parse("https://developer.google.com/mirror1/tree1/branch1/somepackage.apk"));
        adp.put("ACTION_PACKAGE_REMOVED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_REPLACED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PACKAGE_RESTARTED", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_PASTE", Uri.parse("directory://data/data/com.android.IntentFuzzer/"));
        adp.put("ACTION_PICK", Uri.parse("directory://data/data/com.android.IntentFuzzer/"));
        adp.put("ACTION_PICK_ACTIVITY",null);
        adp.put("ACTION_POWER_CONNECTED",null);
        adp.put("ACTION_POWER_DISCONNECTED",null);
        adp.put("ACTION_POWER_USAGE_SUMMARY",null);
        adp.put("ACTION_PROVIDER_CHANGED", Uri.parse("content://android.mail.GmailProvider/unread/"));
        adp.put("ACTION_REBOOT",null);
        adp.put("ACTION_RUN", getFileUri("run/exec"));
        adp.put("ACTION_SCREEN_OFF",null);
        adp.put("ACTION_SCREEN_ON",null);
        adp.put("ACTION_SEARCH",null);	//takes search keys from extras
        adp.put("ACTION_SEARCH_LONG_PRESS",null);
        adp.put("ACTION_SEND",null); //uses mime-type and extras
        adp.put("ACTION_SENDTO", Uri.parse("content://contacts/people/1"));
        adp.put("ACTION_SEND_MULTIPLE",null); //uses mime-type
        adp.put("ACTION_SET_WALLPAPER",null);
        adp.put("ACTION_SHUTDOWN",null);
        adp.put("ACTION_SYNC",null);
        adp.put("ACTION_SYSTEM_TUTORIAL",null);
        adp.put("ACTION_TIMEZONE_CHANGED",null);
        adp.put("ACTION_TIME_CHANGED",null);
        adp.put("ACTION_TIME_TICK",null);
        adp.put("ACTION_UID_REMOVED",null);
        adp.put("ACTION_UMS_CONNECTED",null);
        adp.put("ACTION_UMS_DISCONNECTED",null);
        adp.put("ACTION_UNINSTALL_PACKAGE", Uri.parse("content://com.android.example.somepackage"));
        adp.put("ACTION_USER_PRESENT",null);
        adp.put("ACTION_VIEW", getFileUri("gallery/image1.jpg"));
        adp.put("ACTION_VOICE_COMMAND",null);
        adp.put("ACTION_WALLPAPER_CHANGED",null);
        adp.put("ACTION_WEB_SEARCH", null);	//uses extra field
        // weareabl specifc
        adp.put("vnd.google.android.gms.fitness.COMPUTE_DATA", null);
        adp.put("com.google.android.gms.actions.RESERVE_TAXI_RESERVATION", null);
        adp.put("android.intent.action.SET_ALARM", null);
        adp.put("android.intent.action.SET_TIMER", null);
        adp.put("vnd.google.fitness.TRACK", null);
        adp.put("vnd.google.fitness.VIEW", null);
        adp.put("com.google.android.wearable.action.STOPWATCH", null);
        adp.put("vnd.google.fitness.VIEW_GOAL", null);
    }

    public Uri get(String actionString) {
        return adp.get(actionString);
    }

    private Uri getFileUri(String file) {
        return FileProvider.getUriForFile(
                context,
                PROVIDER_AUTHORITY,
                new File(currentDir + "/" + file));
    }

}
