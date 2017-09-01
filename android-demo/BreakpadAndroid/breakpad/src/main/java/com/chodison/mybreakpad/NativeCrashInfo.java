package com.chodison.mybreakpad;

/**
 * Created by chodison on 2017/9/1.
 */

public class NativeCrashInfo {
    //崩溃的SO名字列表
    public String[] crashSoName;
    //崩溃的SO对应的堆栈地址
    public String[] crashSoAddr;
    //记录第一个崩溃的SO名字，当上面两个参数没有的话说明其他so崩溃了
    public String firstCrashSoName;
}
