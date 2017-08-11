package com.chodison.mybreakpad;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by chodison on 2017/8/11.
 */

public class ExceptionHandler {
    private static String TAG = "ExceptionHandler";

    static boolean loadBreakpadSuccess = false;

    static {
        try {
            System.loadLibrary("breakpad_client");
            loadBreakpadSuccess = true;
        } catch (Exception e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load breakpad_client");
        }
    }

    public static boolean init(String dumpFileDir){
        if (TextUtils.isEmpty(dumpFileDir)) {
            Log.e(TAG, "dumpFileDir can not be empty");
            return false;
        }
        if (loadBreakpadSuccess) {
            return nativeInit(dumpFileDir) > 0 ;
        }
        return false;
    }

    private static native int nativeInit(String dumpFileDir);

    public static int testNativeCrash(){
        if (loadBreakpadSuccess){
            Log.d(TAG, "test native crash .......................");
            return nativeTestCrash();
        }
        return -1;
    }

    private static native int nativeTestCrash();
}
