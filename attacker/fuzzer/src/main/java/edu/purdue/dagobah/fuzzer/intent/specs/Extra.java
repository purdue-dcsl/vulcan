package edu.purdue.dagobah.fuzzer.intent.specs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzUtils;

/**
 * Class to represent an <code>extra</code> field on a Intent.
 */
public class Extra {

    DataType type;
    String name;
    String constant_value;
    boolean optional;

    /* ---------------------------------------------------------------------------
     * Constructors
     * --------------------------------------------------------------------------- */

    public Extra (String type, String name, String constant_value, boolean optional) {
        this.type = DataType.fromString(type);
        this.name = name;
        this.constant_value = constant_value;
        this.optional = optional;
    }

    public Extra (String type, String name, boolean optional) {
        this.type = DataType.fromString(type);
        this.name = name;
        this.optional = optional;
    }

    public Extra (DataType type, String name, boolean optional) {
        this.type = type;
        this.name = name;
        this.optional = optional;
    }

    public Extra (DataType type, String name) {
        this.type = type;
        this.name = name;
        this.optional = false;
    }

    /* ---------------------------------------------------------------------------
     * Accessors
     * --------------------------------------------------------------------------- */

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getConstantValue() {
        return constant_value;
    }

    public void setConstantValue(String constant_value) {
        this.constant_value = constant_value;
    }

    /* ---------------------------------------------------------------------------
     * Helper Methods
     * --------------------------------------------------------------------------- */

    public boolean hasDefautValue() {
        return ( type == DataType.Tboolean ||
                type == DataType.Tint ||
                type == DataType.Tlong ||
                type == DataType.Tdouble ||
                type == DataType.Tchar
        );
    }

    public Intent setRandomValue(Intent intent) {

        switch (type) {
            // types with default value
            case Tint:
                return intent.putExtra(constant_value, Constants.rnd.nextInt());
            case Tlong:
                return intent.putExtra(constant_value, Constants.rnd.nextLong());
            case Tfloat:
                return intent.putExtra(constant_value, Constants.rnd.nextFloat());
            case Tdouble:
                return intent.putExtra(constant_value, Constants.rnd.nextDouble());
            case Tboolean:
                return intent.putExtra(constant_value, Constants.rnd.nextBoolean());
            case Tchar:
                return intent.putExtra(constant_value, (char)Constants.rnd.nextInt(256));

            // types without default value
            case TCharSequence:
                CharSequence ch = new String(FuzzUtils.
                        getRandomData(128, false));
                return intent.putExtra(constant_value, ch);

            case TCharSequenceArrayList: {
                int len = Constants.rnd.nextInt(100);
                ArrayList<CharSequence> chArrayList = new ArrayList<>();
                for (int i=0; i<len; i++)
                    chArrayList.add(new String(FuzzUtils.getRandomData(128, false)));
                return intent.putExtra(constant_value, chArrayList);
            }

            case TURI: {
                Uri uri = FuzzUtils.getRandomUri("dumb");
                return intent.putExtra(constant_value, uri);
            }

            case TURIArrayList: {
                int len = Constants.rnd.nextInt(100);
                ArrayList<Uri> uriArrayList = new ArrayList<>();
                for (int i=0; i<len; i++)
                    uriArrayList.add(FuzzUtils.getRandomUri("dumb"));
                return intent.putExtra(constant_value, uriArrayList);
            }

            case TintArray: {
                int len = Constants.rnd.nextInt(100);
                int[] inta = new int[len];
                for (int i=0; i<inta.length; i++)
                    inta[i] = Constants.rnd.nextInt();
                return intent.putExtra(constant_value, inta);
            }

            case TStringArray: {
                int len = Constants.rnd.nextInt(100);
                String[] strArray = new String [len];
                ArrayList<String> strArrayList = new ArrayList<>();
                for (int i=0; i<len; i++)
                    strArrayList.add(new String(FuzzUtils.getRandomData(128, false)));
                return intent.putExtra(constant_value, strArrayList.toArray(strArray));
            }

            case TStringArrayList: {
                int len = Constants.rnd.nextInt(100);
                ArrayList<String> strArrayList = new ArrayList<>();
                for (int i=0; i<len; i++)
                    strArrayList.add(new String(FuzzUtils.getRandomData(128, false)));
                return intent.putExtra(constant_value, strArrayList);
            }

            case TStringArrayArrayList: {
                int leni = Constants.rnd.nextInt(100);
                int lenj = Constants.rnd.nextInt(100);
                String[] strArray = new String [lenj];
                ArrayList<String[]> strArrayList = new ArrayList<>();
                for (int i=0; i<leni; i++) {
                    for (int j=0; j<lenj; j++)
                        strArray[j] = new String(FuzzUtils.getRandomData(128, false));
                    strArrayList.add(strArray);
                }
                return intent.putExtra(constant_value, strArrayList);
            }

            case TString:
                default: {
                    String str = new String(FuzzUtils.
                            getRandomData(128, false));
                    return intent.putExtra(constant_value, str);
                }
        }

    }

    public Object getXtremeValue() {

        switch (type) {
            case Tint:
//                Integer.MAX_VALUE;
//                Integer.MIN_VALUE;
                break;
            case Tlong:
                break;
            case Tfloat:
                break;
            case Tdouble:
                break;
            case TString:
                break;
            case Tchar:
                break;
            case TCharSequence:
                break;
            case Tboolean:
                break;
        }

        return null;
    }
        
    @Override
    public String toString() {
        return String.format("Extra {name=%s constant=%s type=%s optional=%b}",
                this.name, this.constant_value, this.type, this.optional);
    }
}
