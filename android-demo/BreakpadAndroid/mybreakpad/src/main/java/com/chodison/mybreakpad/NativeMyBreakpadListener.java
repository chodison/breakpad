package com.chodison.mybreakpad;

/**
 * Created by chodison on 2017/9/13.
 */

public class NativeMyBreakpadListener {
    /**
     * mybreakpad事件监听
     * <ul>
     *     <li>{@link NativeMybreakpad#EVENT_TYPE_INIT}</li>
     *     <li>{@link NativeMybreakpad#EVENT_TYPE_PROCESS}</li>
     * </ul>
     */
    public interface OnEventListener {
        /**
         * 初始化mybreakpad事件值
         * @param what 事件错误
         *             <ul>
         *             <li>{@link NativeMybreakpad#EVENT_INIT_SUCCESS}</li>
         *             <li>{@link NativeMybreakpad#EVENT_INIT_FAILED}</li>
         *             <li>{@link NativeMybreakpad#EVENT_INIT_DUMPDIR_NULL}</li>
         *             <li>{@link NativeMybreakpad#EVENT_INIT_CONTEXT_NULL}</li>
         *             <li>{@link NativeMybreakpad#EVENT_INIT_LOADSO_FAIL}</li>
         *             </ul>
         * @param arg1 事件补充
         */
        void onInitEvent(int what, int arg1);

        /**
         * 分析处理dump文件事件值
         * @param what 事件错误
         *             <ul>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_SUCCESS}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_FAILED}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_OBJFAIL_CLASS}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_OBJFAIL_METHOD_INIT}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_OBJFAIL_METHOD_NEWOBJ}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_OBJFAIL_METHOD_GETFEILD}</li>
         *             <li>{@link NativeMybreakpad#EVENT_PROCESS_DUMPFILE_NULL}</li>
         *             </ul>
         * @param arg1 事件补充
         */
        void onProcessEvent(int what, int arg1);
    }

    public static final int REPORT_LOG_LEVEL_DEBUG = 0;
    public static final int REPORT_LOG_LEVEL_INFO = 1;
    public static final int REPORT_LOG_LEVEL_WARN = 2;
    public static final int REPORT_LOG_LEVEL_ERROR = 3;
    /**
     * 日志上报回调接口定义
     *
     */
    public interface OnLogCallback {
        /**
         * 回调函数接口
         * @param level
         * 	see
         *  <ul>
         *  <li>{@link #REPORT_LOG_LEVEL_DEBUG}
         *  <li>{@link #REPORT_LOG_LEVEL_INFO}
         *  <li>{@link #REPORT_LOG_LEVEL_WARN}
         *  <li>{@link #REPORT_LOG_LEVEL_ERROR}
         *  </ul>
         * @param modelId 模块id
         * @param tag  日志标记
         * @param msg  日志信息
         */
        void onLogCb(int level, String modelId, String tag, String msg);
    }
}
