package com.chodison.mybreakpad;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by chodison on 2017/8/22.
 *
 */

public class NativeMybreakpad {
    private static String TAG = "NativeMybreakpad";

    static boolean loadBreakpadSuccess = false;

    /**
     * 初始化返回类型
     */
    public enum BreakpadInitType {
        TYPE_INIT_SUCCESS, TYPE_INIT_FAILED, TYPE_INIT_LOADSO_FAIL, TYPE_INIT_DUMPDIR_NULL
    }

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

    /**
     * mybreakpad初始化,当初始化失败时，根据返回值确定原因，
     * 若为TYPE_INIT_LOADSO_FAIL则此mybreakpad模块不可用
     * @param dumpFileDir 崩溃发生时dump文件存储的目录
     * @return
     */
    public static BreakpadInitType init(String dumpFileDir){
        if (TextUtils.isEmpty(dumpFileDir)) {
            Log.e(TAG, "dumpFileDir can not be empty");
            return BreakpadInitType.TYPE_INIT_DUMPDIR_NULL;
        }
        if (loadBreakpadSuccess) {
            if(nativeInit(dumpFileDir) > 0)
                return BreakpadInitType.TYPE_INIT_SUCCESS;
            else
                return BreakpadInitType.TYPE_INIT_FAILED;
        }
        return BreakpadInitType.TYPE_INIT_LOADSO_FAIL;
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
     * 仅当 {@link #dumpFileProcess}处理成功调取才有效
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
     * 仅当 {@link #dumpFileProcess}处理成功调取才有效
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
