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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.chodison.mybreakpad.helper.ABuildHelper;
import com.chodison.mybreakpad.progma.BreakpadConfig;
import com.chodison.mybreakpad.progma.DebugLog;
import com.chodison.mybreakpad.NativeMyBreakpadListener.OnEventListener;
import com.chodison.mybreakpad.NativeMyBreakpadListener.OnLogCallback;

import java.lang.ref.WeakReference;

/**
 * Created by chodison on 2017/8/22.
 *
 */

public class NativeMybreakpad {
    private static String TAG = "chodison java";

    private static NativeMybreakpad mInstance;

    private EventHandler mEventHandler;
    private WeakReference<NativeMybreakpad> mWeakNativeMyBreakpad;
    private OnEventListener mOnEventListener;

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


    public static final int EVENT_TYPE_INIT = 100;
    public static final int EVENT_TYPE_PROCESS = 200;

    public static final int EVENT_INIT_SUCCESS = 1000;
    public static final int EVENT_INIT_FAILED = 1001;
    public static final int EVENT_INIT_DUMPDIR_NULL = 1002;
    public static final int EVENT_INIT_CONTEXT_NULL = 1003;
    public static final int EVENT_INIT_LOADSO_FAIL = 1004;

    public static final int EVENT_PROCESS_SUCCESS = 2000;
    public static final int EVENT_PROCESS_FAILED = 2001;
    public static final int EVENT_PROCESS_OBJFAIL_CLASS = 2002;
    public static final int EVENT_PROCESS_OBJFAIL_METHOD_INIT = 200301;
    public static final int EVENT_PROCESS_OBJFAIL_METHOD_NEWOBJ = 200302;
    public static final int EVENT_PROCESS_OBJFAIL_METHOD_GETFEILD = 200303;
    public static final int EVENT_PROCESS_DUMPFILE_NULL = 2004;

    private static volatile boolean mIsNativeInitialized = false;
    private void initNativeOnce() {
        synchronized (NativeMybreakpad.class) {
            if (!mIsNativeInitialized) {
                Looper looper;
                if ((looper = Looper.myLooper()) != null) {
                    mEventHandler = new EventHandler(this, looper);
                } else if ((looper = Looper.getMainLooper()) != null) {
                    mEventHandler = new EventHandler(this, looper);
                } else {
                    mEventHandler = null;
                }


                mWeakNativeMyBreakpad = new WeakReference<NativeMybreakpad>(this);
                nativeSetup(mWeakNativeMyBreakpad);

                mIsNativeInitialized = true;
            }
        }
    }

    private native final void nativeSetup(Object NativeMybreakpad_this);

    private static volatile boolean mIsLibLoaded = false;
    private static void loadLibrariesOnce(Context context) {
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

    private static class EventHandler extends Handler {
        private WeakReference<NativeMybreakpad> mWeakMyBreakpad;

        public EventHandler(NativeMybreakpad mbr, Looper looper) {
            super(looper);
            mWeakMyBreakpad = new WeakReference<NativeMybreakpad>(mbr);
        }

        @Override
        public void handleMessage(Message msg) {
            NativeMybreakpad br = mWeakMyBreakpad.get();
            if (br == null) {
                DebugLog.w(TAG,
                        "NativeMybreakpad went away with unhandled events");
                return;
            }

            switch (msg.what) {
                case EVENT_TYPE_INIT:
                    DebugLog.d(TAG, "init event msg,arg1:"+msg.arg1);
                    br.notifyOnInitEvent(msg.arg1, msg.arg2);
                    return;

                case EVENT_TYPE_PROCESS:
                    DebugLog.d(TAG, "process event msg,arg1:"+msg.arg1);
                    br.notifyOnProcessEvent(msg.arg1, msg.arg2);
                    return;

                default:
                    DebugLog.w(TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    //native层调用，崩溃事件上报
    private static void onNativeCrashEvent(Object weakThiz, int what,
                                            int arg1, int arg2, Object obj) {
        if (weakThiz == null)
            return;

        @SuppressWarnings("rawtypes")
        NativeMybreakpad mbr = (NativeMybreakpad ) ((WeakReference) weakThiz).get();
        if (mbr == null) {
            return;
        }

        if (mbr.mEventHandler != null) {
            Message m = mbr.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mbr.mEventHandler.sendMessage(m);
        }
    }

    private NativeMybreakpad() {
    }

    private static OnLogCallback mOnLogCallback;

    /**
     * 设置日志回调
     * @param callback
     */
    public static void setOnLogCallback(OnLogCallback callback) {
        mOnLogCallback = callback;
    }

    /**
     * 获取日志回调接口
     * @return
     */
    public static OnLogCallback getOnLogCallback() {
        return mOnLogCallback;
    }

    /**
     * 获取mybreakpad版本信息
     * @return
     */
    public static String getMyBreakpadVersion() {
        return BreakpadConfig.BREAKPAD_LIB_VERSION;
    }

    public static NativeMybreakpad getInstance() {
        if(mInstance == null) {
            synchronized (NativeMybreakpad.class) {
                if(mInstance == null) {
                    mInstance = new NativeMybreakpad();
                }
            }
        }
        return mInstance;
    }

    public void notifyOnInitEvent(int what, int arg1) {
        if(mOnEventListener != null) {
            mOnEventListener.onInitEvent(what, arg1);
        }
    }

    public void notifyOnProcessEvent(int what, int arg1) {
        if(mOnEventListener != null) {
            mOnEventListener.onProcessEvent(what, arg1);
        }
    }

    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void setDebug(boolean enable) {
        BreakpadConfig.setDebug(enable);
    }
    /**
     * 获取初始化的情况
     * @return
     */
    public String getInitedMsg() {
        return mLoadSoFailMsg;
    }
    /**
     * mybreakpad初始化,当初始化失败时，根据返回值确定原因，
     * 若为TYPE_INIT_LOADSO_FAIL则此mybreakpad模块不可用
     * @param context 获取SO路径
     * @param dumpFileDir 崩溃发生时dump文件存储的目录
     * @return
     */
    public int init(Context context, String dumpFileDir) {
        if(context == null) {
            DebugLog.e(TAG, "context is null");
            if(mOnEventListener != null) {
                mOnEventListener.onInitEvent(EVENT_INIT_CONTEXT_NULL, 0);
            }
            return TYPE_INIT_CONTEXT_NULL;
        }

        loadLibrariesOnce(context);
        //初始化SO失败
        if(!mIsLibLoaded) {
            if(mOnEventListener != null) {
                mOnEventListener.onInitEvent(EVENT_INIT_LOADSO_FAIL, 0);
            }
            return TYPE_INIT_LOADSO_FAIL;
        }
        initNativeOnce();

        if (TextUtils.isEmpty(dumpFileDir)) {
            DebugLog.e(TAG, "dumpFileDir can not be empty");
            if(mOnEventListener != null) {
                mOnEventListener.onInitEvent(EVENT_INIT_DUMPDIR_NULL, 0);
            }
            return TYPE_INIT_DUMPDIR_NULL;
        }
        if (mIsLibLoaded) {
            if(nativeInit(dumpFileDir) > 0)
                return TYPE_INIT_SUCCESS;
            else
                return TYPE_INIT_FAILED;
        }
        if(mOnEventListener != null) {
            mOnEventListener.onInitEvent(EVENT_INIT_LOADSO_FAIL, 0);
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
    public NativeCrashInfo dumpFileProcessinfo(String dump_file, String[] check_sos) {
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
    public NativeCrashInfo dumpFileProcessinfo(String dump_file, String processed_path, String[] check_sos) {
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
