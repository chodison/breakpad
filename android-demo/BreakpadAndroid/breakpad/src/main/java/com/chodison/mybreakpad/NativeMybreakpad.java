package com.chodison.mybreakpad;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by chodison on 2017/8/22.
 */

public class NativeMybreakpad {
    private static String TAG = "NativeMybreakpad";

    static boolean loadBreakpadSuccess = false;

    static {
        try {
            System.loadLibrary("mybreakpad");
            loadBreakpadSuccess = true;
        } catch (UnsatisfiedLinkError ex) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+ex.getMessage());
        } catch (Exception e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+e.getMessage());
        } catch (Throwable e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+e.getMessage());
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

    /**
     * 处理dump文件
     * @param dump_file
     * @param processed_path
     * @return true 成功处理
     */
    public static boolean dumpFileProcess(String dump_file, String processed_path, String[] check_sos) {
        synchronized (NativeMybreakpad.class) {
            if (loadBreakpadSuccess) {
                return nativeDumpProcess(dump_file, processed_path, check_sos) == 0;
            } else {
                Log.e(TAG, "init is failed!");
            }
            return false;
        }
    }

    private native static int nativeDumpProcess(String dump_file, String processed_path, String[] check_sos);

    /**
     * 获取崩溃的SO名称
     * @return
     */
    public static String[] getCrashSoName() {
        if (loadBreakpadSuccess) {
            return  nativeGetCrashSoName();
        }
        return null;
    }
    private static native String[] nativeGetCrashSoName();

    /**
     * 获取解析后崩溃的so堆栈地址
     * @return
     */
    public static String[] getCrashSoAddr() {
        if (loadBreakpadSuccess) {
            return  nativeGetCrashSoAddr();
        }
        return null;
    }
    private static native String[] nativeGetCrashSoAddr();

    public static int testNativeCrash(){
        if (loadBreakpadSuccess){
            Log.d(TAG, "test native crash .......................");
            return nativeTestCrash();
        }
        return -1;
    }

    private static native int nativeTestCrash();
}
