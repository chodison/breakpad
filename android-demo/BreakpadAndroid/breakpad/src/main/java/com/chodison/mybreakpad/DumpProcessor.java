package com.chodison.mybreakpad;

import android.util.Log;

/**
 * Created by chodison on 2017/8/11.
 */

public class DumpProcessor {
    private static String TAG = "DumpProcessor";
    private static String[] app_so = {"libbreakpad_client.so", "libbreakpad_processor.so"};

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
    public static boolean exec(String[] commands, String printf_path) {
        if (commands.length < 2) {
            Log.e(TAG, "commands is error!");
            return false;
        }
        if(loadBreakpadSuccess) {
            return nativeExec(commands, printf_path, app_so) == 0;
        }
        return false;
    }

    private native static int nativeExec(String[] commands, String printf_path, String[] check_sos);
}
