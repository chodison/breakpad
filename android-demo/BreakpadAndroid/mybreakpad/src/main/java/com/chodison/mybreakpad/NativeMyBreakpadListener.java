package com.chodison.mybreakpad;

/**
 * Created by chodison on 2017/9/13.
 */

public class NativeMyBreakpadListener {
    /**
     * mybreakpad事件监听
     */
    public interface OnEventListener {
        /**
         * 初始化mybreakpad事件值
         * @param what 事件错误
         * @param arg1 事件补充
         */
        void onInitEvent(int what, int arg1);

        /**
         * 分析处理dump文件事件值
         * @param what 事件错误
         * @param arg1 事件补充
         */
        void onProcessEvent(int what, int arg1);
    }
}
