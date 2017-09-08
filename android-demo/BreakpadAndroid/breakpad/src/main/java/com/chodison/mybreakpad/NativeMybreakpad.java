/*
 * Copyright (C) 2017 chodison <c_soft_dev@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chodison.mybreakpad;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chodison.mybreakpad.helper.ABuildHelper;
import com.chodison.mybreakpad.progma.BreakpadConfig;
import com.chodison.mybreakpad.progma.DebugLog;

/**
 * Created by chodison on 2017/8/22.
 *
 */

public class NativeMybreakpad {
    private static String TAG = "chodison java";

    /**
     * 初始化返回类型
     */
    public final static int TYPE_INIT_SUCCESS = 0;
    public final static int TYPE_INIT_FAILED = 1;
    public final static int TYPE_INIT_LOADSO_FAIL = 2;
    public final static int TYPE_INIT_DUMPDIR_NULL = 3;
    public final static int TYPE_INIT_CONTEXT_NULL = 4;

    private static String mLoadSoFailMsg = "";

    public static NativeCrashInfo mNativeCrashInfo;


    private static volatile boolean mIsNativeInitialized = false;
    private static void initNativeOnce() {
        synchronized (NativeMybreakpad.class) {
            if (!mIsNativeInitialized) {
                //
                mIsNativeInitialized = true;
            }
        }
    }

    private static volatile boolean mIsLibLoaded = false;
    public static void loadLibrariesOnce(Context context) throws UnsatisfiedLinkError, SecurityException, NullPointerException {
        synchronized (NativeMybreakpad.class) {
            if (!mIsLibLoaded) {
                try {
                    if(ABuildHelper.supportX86()) {
                        DebugLog.i(TAG,
                                "loadLibrary mybreakpad_x86");
                        System.loadLibrary("mybreakpad_x86");
                    } else {
                        DebugLog.i(TAG,
                                "loadLibrary mybreakpad");
                        System.loadLibrary("mybreakpad");
                    }
                    mIsLibLoaded = true;
                } catch (UnsatisfiedLinkError ex) {
                    if(ex != null) {
                        DebugLog.e(TAG,
                                "loadLibrary UnsatisfiedLinkError "+ex.getMessage());
                    }
                    retryLoadLibrary(context);
                } catch (Exception e) {
                    if(e != null) {
                        DebugLog.e(TAG,
                                "loadLibrary Exception "+e.getMessage());
                    }
                    retryLoadLibrary(context);
                } catch (Throwable e) {
                    if(e != null) {
                        DebugLog.e(TAG,
                                "loadLibrary Throwable "+e.getMessage());
                    }
                    retryLoadLibrary(context);
                }
            }
        }
    }

    private static void retryLoadLibrary(Context context) {
        String libPath = context.getFilesDir().getParentFile().getAbsolutePath();
        try {
            if(ABuildHelper.supportX86()) {
                DebugLog.i(TAG,
                        "retryLoadLibrary libmybreakpad_x86.so");
                System.load(libPath+"/lib"+"/libmybreakpad_x86.so");
            } else {
                DebugLog.i(TAG,
                        "retryLoadLibrary libmybreakpad.so");
                System.load(libPath+"/lib"+"/libmybreakpad.so");
            }
            mIsLibLoaded = true;
        } catch (UnsatisfiedLinkError ex) {
            if(ex != null) {
                DebugLog.e(TAG,
                        "retryLoadLibrary UnsatisfiedLinkError "+ex.getMessage());
                mLoadSoFailMsg = ex.getMessage();
            }
        } catch (Exception ex) {
            if(ex != null) {
                DebugLog.e(TAG,
                        "retryLoadLibrary Exception "+ex.getMessage());
                mLoadSoFailMsg = ex.getMessage();
            }
        } catch (Throwable ex) {
            if(ex != null) {
                DebugLog.e(TAG,
                        "retryLoadLibrary Throwable "+ex.getMessage());
                mLoadSoFailMsg = ex.getMessage();
            }
        }
    }

    //native层调用，当发生崩溃时。
    private static void onNativeCrash(int succeeded) {
        DebugLog.e(TAG, "onNativeCrash callback:"+succeeded);
    }

    public void setDebug(boolean enable) {
        BreakpadConfig.setDebug(enable);
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
     * @param context 获取SO路径
     * @param dumpFileDir 崩溃发生时dump文件存储的目录
     * @return
     */
    public static int init(Context context, String dumpFileDir) {
        if(context == null) {
            return TYPE_INIT_CONTEXT_NULL;
        }

        loadLibrariesOnce(context);
        initNativeOnce();

        if (TextUtils.isEmpty(dumpFileDir)) {
            DebugLog.e(TAG, "dumpFileDir can not be empty");
            return TYPE_INIT_DUMPDIR_NULL;
        }
        if (mIsLibLoaded) {
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
        if (mIsLibLoaded) {
            mNativeCrashInfo = nativeDumpProcess(dump_file, null, check_sos);
            return mNativeCrashInfo;
        } else {
            DebugLog.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
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
        if (mIsLibLoaded) {
            mNativeCrashInfo = nativeDumpProcess(dump_file, processed_path, check_sos);
            return mNativeCrashInfo;
        } else {
            DebugLog.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
        }
        return null;
    }

    private native static NativeCrashInfo nativeDumpProcess(String dump_file, String processed_path, String[] check_sos);


    public static int testNativeCrash(){
        if (mIsLibLoaded){
            DebugLog.d(TAG, "test native crash .......................");
            return nativeTestCrash();
        } else {
            DebugLog.e(TAG, "inited was failed,msg:"+mLoadSoFailMsg+",PLZ fixed init error and init again");
        }
        return -1;
    }

    private static native int nativeTestCrash();
}
