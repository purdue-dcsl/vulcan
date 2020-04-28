package edu.purdue.dagobah.common;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.purdue.dagobah.ResourcesInfo;

import static android.content.Context.ACTIVITY_SERVICE;

public class ResourcesManager {

    private final static String TAG = "ry/RM";

    private final static String PREFIX_SYS_CPU = "/sys/devices/system/cpu/";
    private final static String SUFFIX_SYS_CPU_SCALING = "/cpufreq/scaling_cur_freq";
    private final static String SUFFIX_SYS_CPU_MAX = "/cpufreq/cpuinfo_max_freq";

    private static int num = 0;

    Context mContext;
    ActivityManager mAM;

    public ResourcesManager(Context ctxt) {
        this.mContext = ctxt;
        this.mAM = (ActivityManager)mContext.getApplicationContext().
                getSystemService(ACTIVITY_SERVICE);
    }


    /**
     *
     * @return
     */
    // TODO: Get Available Memory
    // TODO: Get Available CPU
    public ResourcesInfo getAvailableResources () {
        MemoryInfo mem = new MemoryInfo();
        final Runtime runtime = Runtime.getRuntime();

        mAM.getMemoryInfo(mem);
        double availMem = mem.availMem / 0x100000L;
        double xxx = mem.totalMem / 0x100000L;

        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long maxMem = runtime.maxMemory();

        /** cpu */
        int nTotal = new File(PREFIX_SYS_CPU).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return Pattern.matches("cpu[0-9]+", file.getName());
            }
        }).length;

        ArrayList<Integer> scaling = new ArrayList<>();
        for (int i=0; i<nTotal; i++) {
            String val = getContent(PREFIX_SYS_CPU + "cpu"+ i + SUFFIX_SYS_CPU_SCALING);
            scaling.add(Integer.valueOf(val));
        }

        ArrayList<Integer> max = new ArrayList<>();
        for (int i=0; i<nTotal; i++) {
            String val = getContent(PREFIX_SYS_CPU + "cpu"+ i + SUFFIX_SYS_CPU_MAX);
            max.add(Integer.valueOf(val));
        }

        int tmp = 0;
        for (int i=0; i<nTotal; i++) {
            if ( max.get(i) == 0)
                tmp += 100;
            else
                tmp += scaling.get(i) * 100 / max.get(i);
        }
        int cpu = Math.min (tmp / nTotal, 100);


        FuzzUtils.log(String.format("Mem Max: {%5.3f} CPU {%d} | " +
                        "{%d} Total: {%d} Free: {%d} Used: {%d} Available {%d} " +
                        "Total (AM) {%f} Available (AM): {%f}" +
                        " * %d %d",
                (xxx - availMem) / xxx * 100, cpu,
                maxMem, totalMem, freeMem, totalMem-freeMem, maxMem-totalMem+freeMem,
                xxx, availMem,
                mem.totalMem, mem.availMem));

        return null;
    }

    private String getContent (String path) {
        try (BufferedReader buf =
                new BufferedReader(new FileReader(path))) {
            return buf.readLine();
        } catch (FileNotFoundException ex) {
            return "0";
        } catch (IOException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
            return "0";
        }


    }


    ResourcesInfo getResources() {
        return null;
    }



}
