#ifndef __MY_LOG_H__
#define __MY_LOG_H__
#include <jni.h>
#include <android/log.h>

#ifndef DEBUG
#define DEBUG 1
#endif

#define MYLOG_MODULE_NAME "cn.chodison.mybreakpad"

#ifdef DEBUG
	#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, MYLOG_MODULE_NAME, __VA_ARGS__)
	#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MYLOG_MODULE_NAME, __VA_ARGS__)
	#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MYLOG_MODULE_NAME, __VA_ARGS__)
	#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, MYLOG_MODULE_NAME, __VA_ARGS__)
	#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MYLOG_MODULE_NAME, __VA_ARGS__)
#else
	#define LOGV(...) {}
	#define LOGD(...) {}
	#define LOGI(...) {}
	#define LOGW(...) {}
	#define LOGE(...) {}
#endif

#define VLOG(level, TAG, ...)    ((void)__android_log_vprint(level, TAG, __VA_ARGS__))
#define ALOG(level, TAG, ...)    ((void)__android_log_print(level, TAG, __VA_ARGS__))

static int report_log(int alog_level, const char* alog_text) {
	ALOG(alog_level, MYLOG_MODULE_NAME, "%s", alog_text);
	return 0;
}

#endif /* __COMMON_H__ */
