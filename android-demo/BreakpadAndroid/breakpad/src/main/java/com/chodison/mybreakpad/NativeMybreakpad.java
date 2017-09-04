package com.chodison.mybreakpad;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by chodison on 2017/8/22.
 *
 */

public class NativeMybreakpad {
    private static String TAG = "chodison";

    static boolean loadBreakpadSuccess = false;

    /**
     * 初始化返回类型
     */
    private final static int TYPE_INIT_SUCCESS = 0;
    private final static int TYPE_INIT_FAILED = 1;
    private final static int TYPE_INIT_LOADSO_FAIL = 2;
    private final static int TYPE_INIT_DUMPDIR_NULL = 3;

    private static String mLoadSoFailMsg = "";

    public static NativeCrashInfo mNativeCrashInfo;

    static {
        try {
            System.loadLibrary("mybreakpad");
            loadBreakpadSuccess = true;
            mLoadSoFailMsg = "loadLibrary libmybreakpad.so OK";
        } catch (UnsatisfiedLinkError ex) {
            loadBreakpadSuccess = false;
            mLoadSoFailMsg = ex.getMessage();
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+mLoadSoFailMsg);
        } catch (Exception ex) {
            loadBreakpadSuccess = false;
            mLoadSoFailMsg = ex.getMessage();
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+mLoadSoFailMsg);
        } catch (Throwable ex) {
            loadBreakpadSuccess = false;
            mLoadSoFailMsg = ex.getMessage();
            Log.e(TAG, "fail to load libmybreakpad.so,msg:"+mLoadSoFailMsg);
        }
    }

    //native层调用，当发生崩溃时。
    private static void onNativeCrash(int succeeded) {
        Log.e(TAG, "onNativeCrash callback:"+succeeded);
    }

    /**
     * 获取初始化的情况
     * @return
     */
    public static String getInitedMsg() {
        return mLoadSoFailMsg;
    }
    /**
     * mybreakpad初始化,当初始化失败时，根据返回值确定原因，
     * 若为TYPE_INIT_LOADSO_FAIL则此mybreakpad模块不可用
     * @param dumpFileDir 崩溃发生时dump文件存储的目录
     * @return
     */
    public static int init(String dumpFileDir) {
        if (TextUtils.isEmpty(dumpFileDir)) {
            Log.e(TAG, "dumpFileDir can not be empty");
            return TYPE_INIT_DUMPDIR_NULL;
        }
        if (loadBreakpadSuccess) {
            if(nativeInit(dumpFileDir) > 0)
                return TYPE_INIT_SUCCESS;
            else
                return TYPE_INIT_FAILED;
        }
        return TYPE_INIT_LOADSO_FAIL;
    }

    private static native int nativeInit(String dumpFileDir);
    /**
     * 分析dump文件
     * @param dump_file dump文件路径
     * @param check_sos 分析时需要匹配的SO
     * @return 成功时返回NativeCrashInfo，否则为空
     */
    public static NativeCrashInfo dumpFileProcessinfo(String dump_file, String[] check_sos) {
        if (loadBreakpadSuccess) {
            mNativeCrashInfo = nativeDumpProcess(dump_file, null, check_sos);
            return mNativeCrashInfo;
        } else {
            Log.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
        }
        return null;
    }
    /**
     * 分析dump文件
     * @param dump_file dump文件路径
     * @param processed_path 分析后的dump文件存储路径文件
     * @param check_sos 分析时需要匹配的SO
     * @return 成功时返回NativeCrashInfo，否则为空
     */
    public static NativeCrashInfo dumpFileProcessinfo(String dump_file, String processed_path, String[] check_sos) {
        if (loadBreakpadSuccess) {
            mNativeCrashInfo = nativeDumpProcess(dump_file, processed_path, check_sos);
            return mNativeCrashInfo;
        } else {
            Log.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
        }
        return null;
    }

    private native static NativeCrashInfo nativeDumpProcess(String dump_file, String processed_path, String[] check_sos);


    public static int testNativeCrash(){
        if (loadBreakpadSuccess){
            Log.d(TAG, "test native crash .......................");
            return nativeTestCrash();
        } else {
            Log.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
        }
        return -1;
    }

    private static native int nativeTestCrash();
}
