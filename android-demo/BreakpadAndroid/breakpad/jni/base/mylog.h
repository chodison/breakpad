#ifndef __MY_LOG_H__
#define __MY_LOG_H__
#include <jni.h>
#include <android/log.h>

#ifndef DEBUG
#define DEBUG 1
#endif

#ifdef DEBUG
	#define MODULE_NAME "cn.chodison.mybreakpad"
	#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, MODULE_NAME, __VA_ARGS__)
	#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
	#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MODULE_NAME, __VA_ARGS__)
	#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, MODULE_NAME, __VA_ARGS__)
	#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)
#else
	#define LOGV(...) {}
	#define LOGD(...) {}
	#define LOGI(...) {}
	#define LOGW(...) {}
	#define LOGE(...) {}
#endif
#endif /* __COMMON_H__ */
