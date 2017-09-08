/*
 * Copyright (C) 2015 chodison <c_soft_dev@163.com>
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

package com.chodison.mybreakpad.progma;

import java.util.Locale;
import android.util.Log;


public class DebugLog {
	private static final int REPORT_LOG_LEVEL_DEBUG = 0;
	private static final int REPORT_LOG_LEVEL_INFO = 1;
	private static final int REPORT_LOG_LEVEL_WARN = 2;
	private static final int REPORT_LOG_LEVEL_ERROR = 3;
	
	private static void reportLog(int level, String tag, String msg) {

	}
	
    // debug function.
    public static void e(String tag, String msg) {
        reportLog(REPORT_LOG_LEVEL_ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (BreakpadConfig.use_report_level > 0) {
        	Log.e(tag, msg, tr);
        }
    }

    public static void efmt(String tag, String fmt, Object... args) {
    	String msg = String.format(Locale.US, fmt, args);
        reportLog(REPORT_LOG_LEVEL_ERROR, tag, msg);
    }

    public static void i(String tag, String msg) {
        reportLog(REPORT_LOG_LEVEL_INFO, tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (BreakpadConfig.use_report_level > 0) {
            Log.i(tag, msg, tr);
        }
    }

    public static void ifmt(String tag, String fmt, Object... args) {
    	String msg = String.format(Locale.US, fmt, args);
        reportLog(REPORT_LOG_LEVEL_ERROR, tag, msg);
    }

    public static void w(String tag, String msg) {
        reportLog(REPORT_LOG_LEVEL_WARN, tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (BreakpadConfig.use_report_level > 0) {
            Log.w(tag, msg, tr);
        }
    }

    public static void wfmt(String tag, String fmt, Object... args) {
    	String msg = String.format(Locale.US, fmt, args);
        reportLog(REPORT_LOG_LEVEL_WARN, tag, msg);
    }

    public static void d(String tag, String msg) {
        reportLog(REPORT_LOG_LEVEL_DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (BreakpadConfig.use_report_level > 0) {
            Log.d(tag, msg, tr);
        }
    }

    public static void dfmt(String tag, String fmt, Object... args) {
    	String msg = String.format(Locale.US, fmt, args);
        reportLog(REPORT_LOG_LEVEL_DEBUG, tag, msg);
    }

    public static void v(String tag, String msg) {
        reportLog(REPORT_LOG_LEVEL_DEBUG, tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (BreakpadConfig.use_report_level > 0) {
            Log.v(tag, msg, tr);
        }
    }

    public static void vfmt(String tag, String fmt, Object... args) {
    	String msg = String.format(Locale.US, fmt, args);
        reportLog(REPORT_LOG_LEVEL_DEBUG, tag, msg);
    }

    public static void printStackTrace(Throwable e) {
        if (BreakpadConfig.use_report_level > 0) {
            e.printStackTrace();
        }
    }

    public static void printCause(Throwable e) {
        if (BreakpadConfig.use_report_level > 0) {
            Throwable cause = e.getCause();
            if (cause != null)
                e = cause;

            printStackTrace(e);
        }
    }
}
