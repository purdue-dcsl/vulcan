package edu.purdue.dagobah.fuzzer.intent;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static edu.purdue.dagobah.common.Constants.PROVIDER_AUTHORITY;

/**
 * URI (Universal Resource Identifier) Generator
 * @author Amiya Maji
 */

public class UriGen {

    private static final String TAG = "FUZZ/UriGen";

    String currentDir = "";
    Random rnd = null;
    Context context = null;

    ArrayList<String> mKnownProviders = null;

    public enum URIType {
        CONTENT, FILE, FOLDER, DIRECTORY, GEO, STREETVIEW, HTTP, HTTPS, MAILTO, SSH, TEL, VOICEMAIL
    };

    protected static Map<String, URIType> uriNamesToTypes = new HashMap<String, URIType>();
    static {
        uriNamesToTypes.put("content://", URIType.CONTENT);
        uriNamesToTypes.put("file://", URIType.FILE);
        uriNamesToTypes.put("folder://", URIType.FOLDER);
        uriNamesToTypes.put("directory://", URIType.DIRECTORY);
        uriNamesToTypes.put("geo:", URIType.GEO);
        uriNamesToTypes.put("google.streetview:", URIType.STREETVIEW);
        uriNamesToTypes.put("http://", URIType.HTTP);
        uriNamesToTypes.put("https://", URIType.HTTPS);
        uriNamesToTypes.put("mailto:", URIType.MAILTO);
        uriNamesToTypes.put("ssh:", URIType.SSH);
        uriNamesToTypes.put("tel:", URIType.TEL);
        uriNamesToTypes.put("voicemail:", URIType.VOICEMAIL);
    }

    public UriGen(String currDir, Random r, ArrayList<String> providersList) {
        currentDir = currDir;
        rnd = r;
        mKnownProviders = providersList;
    }

    public UriGen(String currDir, Random r, ArrayList<String> providersList, Context context) {
        currentDir = currDir;
        rnd = r;
        mKnownProviders = providersList;
        this.context = context;
    }


    // get a Uri based on scheme mType
    // If mType is content we need providerIndex to build the Uri, providerIndex is ignored for other mTypes
    public Uri getUri(String mtype, int providerIndex)
    {
        URIType t = uriNamesToTypes.get(mtype);
        switch(t)
        {
            case CONTENT:
                return buildContentUri(providerIndex);
            case FILE:
                return buildFileUri(currentDir, this.context);
            case FOLDER:
                return buildFolderUri(currentDir);
            case DIRECTORY:
                return buildDirectoryUri(currentDir);
            case GEO:
                return buildGeoUri();
            case STREETVIEW:
                return buildStreetViewUri();
            case HTTP:
                return buildHttpUri();
            case HTTPS:
                return buildHttpsUri();
            case MAILTO:
                return buildMailtoUri();
            case SSH:
                return buildSshUri();
            case TEL:
                return buildTelUri();
            case VOICEMAIL:
                return buildVoicemailUri();
            default:
                return null;
        }

    }
    protected Uri buildContentUri(int providerIndex)
    {	// at present we only support provider and provider+id uris
        // more sophisticated queries are not considered
        return genRandomContentUri(rnd.nextBoolean(), providerIndex);
    }

    // Change to comply with SDK 24
    protected Uri buildFileUri(String currentDir, Context context) {

        // String authority = context.getPackageName()
        //         + ".edu.purdue.android.fuzzer.squibble.common.provider";
        File file = new File(getPathName(currentDir)+getFileName());

        // Log.d(TAG, "authority [1]: " + authority);
        // Log.d(TAG, "authority [2]: " + BuildConfig.APPLICATION_ID
        //        + ".edu.purdue.android.fuzzer.squibble.common.provider");

        return FileProvider.getUriForFile(
                context,
                PROVIDER_AUTHORITY,
                file);
    }

    protected Uri buildFileUri(String currentDir)
    {
        return Uri.parse("file:/"+getPathName(currentDir)+getFileName());
    }

    protected Uri buildFolderUri(String currentDir)
    {
        return Uri.parse("folder:/"+getPathName(currentDir));
    }
    protected Uri buildDirectoryUri(String currentDir)
    {
        return Uri.parse("directory:/"+getPathName(currentDir));
    }
    protected Uri buildGeoUri()
    {   //latitude,longitude
        return Uri.parse("geo:"+getLatAndLong());
    }
    protected Uri buildStreetViewUri()
    {
        // we are using static cbp and mz parameters, these are optional
        return Uri.parse("google.streetview:cbll="+getLatAndLong()+"&cbp=13,250.2,0,0,0");
    }
    protected Uri buildHttpUri()
    {
        return Uri.parse("http://"+getDomainName()+getQueryString());
    }
    protected Uri buildHttpsUri()
    {
        return Uri.parse("https://"+getDomainName()+getQueryString());
    }
    protected Uri buildMailtoUri()
    {
        return Uri.parse("mailto:username@"+getDomainName());
    }
    protected Uri buildSshUri()
    {
        return Uri.parse("ssh:username:password@"+getDomainName());
    }
    protected Uri buildTelUri()
    {
        return Uri.parse("tel:"+getPhoneNumber());
    }
    protected Uri buildVoicemailUri()
    {
        return Uri.parse("voicemail:"+getPhoneNumber());
    }

    protected String getPathName(String currentDir) { // this function can be randomized to return arbitrary pathname
        return currentDir+"/";
    }
    protected String getFileName() { // this function can be randomized to return random filenames
        return "somefile.txt";
    }
    protected String getDomainName() { //this function can be randomized to return random domain names
        return "doomsday.heaven.gov";
    }
    protected String getQueryString() {
        return "";
    }

    protected String getPhoneNumber() {
        String num = "";
        int x = rnd.nextInt(5);
        switch(x)
        {
            case 0: //10 dig
                for(int n=0; n<10; n++)
                    num += rnd.nextInt(10);
                return num;
            case 1: //+intlcode 10 dig
                num += "+"+rnd.nextInt(99);
                for(int n=0; n<10; n++)
                    num += rnd.nextInt(10);
                return num;
            case 2: // xxx xxx xxxx
                for(int n=0; n<3; n++)
                    num += rnd.nextInt(10);
                num += " ";
                for(int n=0; n<3; n++)
                    num += rnd.nextInt(10);
                num += " ";
                for(int n=0; n<4; n++)
                    num += rnd.nextInt(10);
                return num;
            case 3: // (xxx)xxxxxxx
                num += "(";
                for(int n=0; n<3; n++)
                    num += rnd.nextInt(10);
                num += ")";
                for(int n=0; n<7; n++)
                    num += rnd.nextInt(10);
                return num;
            case 4: // xxx-xxx-xxxx
                for(int n=0; n<3; n++)
                    num += rnd.nextInt(10);
                num += "-";
                for(int n=0; n<3; n++)
                    num += rnd.nextInt(10);
                num += "-";
                for(int n=0; n<4; n++)
                    num += rnd.nextInt(10);
                return num;
            default:
                return "tel:1234567890";
        }
    }

    protected String getLatAndLong() {
        //format "lat,long"
        String res = genSign();
        res += (rnd.nextDouble()*90); //lat should be 90, intentionally made larger
        res += ","+genSign();
        res += (rnd.nextDouble()*180); //long should be 180, intentionally made larger
        return res;
    }

    protected String genSign() {
        if(rnd.nextBoolean())
            return "";
        else
            return "-";
    }

    protected Uri genRandomContentUri(boolean withID, int providerIndex) {
        if(withID)
            return Uri.parse("content://"+mKnownProviders.get(providerIndex)+"/"+rnd.nextInt(50));
        else
            return Uri.parse("content://"+mKnownProviders.get(providerIndex));
    }

}
