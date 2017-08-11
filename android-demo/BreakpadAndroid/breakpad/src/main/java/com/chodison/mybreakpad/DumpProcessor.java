package com.chodison.mybreakpad;

import android.util.Log;

/**
 * Created by chodison on 2017/8/11.
 */

public class DumpProcessor {
    private static String TAG = "DumpProcessor";

    static boolean loadBreakpadSuccess = false;

    static {
        try {
            System.loadLibrary("breakpad_processor");
            loadBreakpadSuccess = true;
        } catch (Exception e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load breakpad_processor");
        }
    }

    /**
     *
     *
     * @param commands
     *
     * param1:dump file
     * param2:processed file
     */
    public static boolean exec(String[] commands) {
        if (commands.length < 2) {
            Log.e(TAG, "commands is error!");
            return false;
        }
        if(loadBreakpadSuccess) {
            return nativeExec(commands) == 0;
        }
        return false;
    }

    private native static int nativeExec(String[] commands);
}
