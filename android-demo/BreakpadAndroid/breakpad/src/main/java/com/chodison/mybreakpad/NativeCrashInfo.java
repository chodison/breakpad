package com.chodison.mybreakpad;

/**
 * Created by chodison on 2017/9/1.
 */

public class NativeCrashInfo {
    //崩溃的SO名字列表
    public String[] crashSoName;
    //崩溃的SO对应的堆栈地址
    public String[] crashSoAddr;
    //设置下来的SO在dump解析文件里能找到
    public int exist_app_so;
    //记录第一个崩溃的SO名字，当exist_app_so为0的话说明其他so崩溃了
    public String firstCrashSoName;
}
