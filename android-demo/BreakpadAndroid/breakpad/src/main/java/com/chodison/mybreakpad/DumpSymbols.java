package com.chodison.mybreakpad;

import android.util.Log;

/**
 * Created by chodison on 2017/8/24.
 */

public class DumpSymbols {
    private static String TAG = "DumpSymbols";

    static boolean loadBreakpadSuccess = false;

    static {
        try {
            System.loadLibrary("breakpad_symbols");
            loadBreakpadSuccess = true;
        } catch (UnsatisfiedLinkError ex) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libbreakpad_symbols.so,msg:"+ex.getMessage());
        } catch (Exception e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libbreakpad_symbols.so,msg:"+e.getMessage());
        } catch (Throwable e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libbreakpad_symbols.so,msg:"+e.getMessage());
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
