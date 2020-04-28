package edu.purdue.ryloth.services.comm;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzUtils;

public class CommMan {

    public static final int SIZE_0 = 0;
    public static final int SIZE_1 = 512;
    public static final int SIZE_2 = 1024;
    public static final int SIZE_3 = 4096;
    public static final int SIZE_4 = 8192;
    public static final int SIZE_5 = 16384;
    public static final int SIZE_6 = 32768;
    public static final int SIZE_7 = 65536;

    private static byte COMM_512[];
    private static byte COMM_1K[];
    private static byte COMM_5K[];

    private static final int SIZE = Constants.POC_PARAM_COMM_SIZE;


    static {

        COMM_512 = FuzzUtils.getRandomData(512, true);
        COMM_1K  = FuzzUtils.getRandomData(1024, true);
        COMM_5K  = FuzzUtils.getRandomData(1024*5, true);

    }

    private static int getSize() {
        int size = SIZE;

        switch (size) {
            case 0:
                return SIZE_0;
            case 1:
                return SIZE_1;
            case 2:
                return SIZE_2;
            case 3:
                return SIZE_3;
            case 4:
                return SIZE_4;
            case 5:
                return SIZE_5;
            case 6:
                return SIZE_6;
            case 7:
                return SIZE_7;
            default:
                return SIZE_0;
        }
    }

    public static byte[] getChunk() {
        return FuzzUtils.getRandomData(getSize(), true);
    }

}
