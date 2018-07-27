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
package com.chodison.mybreakpad.progma;


public class BreakpadConfig {
	public static final String BREAKPAD_LIB_VERSION = "V1.0.12";
	/**
	 * Print no output.
	 */
	public static final int BREAKPAD_LOG_REPORT_NONE = 0;
	/**
	 * Print brief output.
	 */
	public static final int BREAKPAD_LOG_REPORT_BRIEF = 1;
	/**
	 * Print detail output.
	 */
	public static final int BREAKPAD_LOG_REPORT_DETAIL = 2;
	
	/*
	 * Android log priority values, in ascending priority order.
	 */
//	typedef enum android_LogPriority {
//	    ANDROID_LOG_UNKNOWN = 0,
//	    ANDROID_LOG_DEFAULT =1,    /* only for SetMinPriority() */
//	    ANDROID_LOG_VERBOSE =2,
//	    ANDROID_LOG_DEBUG =3,
//	    ANDROID_LOG_INFO =4,
//	    ANDROID_LOG_WARN =5,
//	    ANDROID_LOG_ERROR =6,
//	    ANDROID_LOG_FATAL =7,
//	    ANDROID_LOG_SILENT =8,     /* only for SetMinPriority(); must be last */
//	} android_LogPriority;
	/**
	 * Print no output.
	 */
	public static final int BREAKPAD_LOG_LEVEL_SILENT = 8;

	/**
	 * Something went wrong and recovery is not possible.
	 * For example, no header was found for a format which depends
	 * on headers or an illegal combination of parameters is used.
	 */
	public static final int BREAKPAD_LOG_LEVEL_FATAL = 7;

	/**
	 * Something went wrong and cannot losslessly be recovered.
	 * However, not all future data is affected.
	 */
	public static final int BREAKPAD_LOG_LEVEL_ERROR = 6;

	/**
	 * Something somehow does not look correct. This may or may not
	 * lead to problems. An example would be the use of '-vstrict -2'.
	 */
	public static final int BREAKPAD_LOG_LEVEL_WARN = 5;

	/**
	 * Standard information.
	 */
	public static final int BREAKPAD_LOG_LEVEL_INFO = 4;

	/**
	 * Standard information.
	 */
	public static final int BREAKPAD_LOG_LEVEL_DEBUG = 3;
	
	/**
	 * Detailed information.
	 */
	public static final int BREAKPAD_LOG_LEVEL_VERBOSE = 2;

	/**
	 * Detailed information.Stuff which is only useful for libav* developers.
	 */
	public static final int BREAKPAD_LOG_LEVEL_DEFAULT = 1;
	public static final int BREAKPAD_LOG_LEVEL_UNKNOWN = 0;
	
	public static int use_report_level= BREAKPAD_LOG_REPORT_NONE;
	public static int use_log_level= BREAKPAD_LOG_LEVEL_DEBUG;


	public static void setDebug(boolean enable) {
		if(enable) {
			use_report_level = BREAKPAD_LOG_REPORT_BRIEF;
		} else {
			use_report_level = BREAKPAD_LOG_REPORT_NONE;
		}
		use_log_level = BREAKPAD_LOG_LEVEL_DEBUG;
	}
	/**
	 * 
	 * @param report_level
		 *            <ul>
		 *            <li>{@link #BREAKPAD_LOG_REPORT_NONE}
		 *            <li>{@link #BREAKPAD_LOG_REPORT_BRIEF}
		 *            <li>{@link #BREAKPAD_LOG_REPORT_DETAIL}
		 *            </ul>
	 */
	public static void setDebugInfo(int report_level) {
		use_report_level = report_level;
	}
	/**
	 * 
	 * @param log_level 值越小等级越高
		 *            <ul>
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_SILENT}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_FATAL}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_ERROR}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_WARN}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_INFO}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_DEBUG}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_VERBOSE}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_DEFAULT}
		 *            <li>{@link #BREAKPAD_LOG_LEVEL_UNKNOWN}
		 *            </ul>
	 */
	public static void setDebugLogLevel(int log_level) {
		use_log_level = log_level;
	}
}
