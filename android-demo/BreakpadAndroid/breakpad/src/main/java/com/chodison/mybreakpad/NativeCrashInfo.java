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

/**
 * Created by chodison on 2017/9/1.
 */

public class NativeCrashInfo {
    //崩溃的SO名字列表
    public String[] crashSoName;
    //崩溃的SO对应的堆栈地址
    public String[] crashSoAddr;
    //设置下来的SO在dump解析文件里能找到, 1:代表能找到
    public int exist_app_so;
    //记录第一个崩溃的SO名字，当exist_app_so为0的话说明其他so崩溃了
    public String firstCrashSoName;
    /**
     常量	    解释
     SIGTERM	发送给程序的终止请求
     SIGSEGV	非法内存访问（段错误）
     SIGINT	外部中断，通常为用户所发动
     SIGILL	非法程序映像，例如非法指令
     SIGABRT	异常终止条件，例如 abort() 所起始的
     SIGFPE	错误的算术运算，如除以零
     */
    public String signal_type;
}
