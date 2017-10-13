/*
 * Copyright (c) 2017 chodison <c_soft_dev@163.com>
 *
 * This file is part of libmybreakpad
 *
 */
#ifndef __BREAKPAD_PROCESSOR_JNI_H__
#define __BREAKPAD_PROCESSOR_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpProcessor_nativeExec(JNIEnv *env, jobject obj,
		jobjectArray commands, jstring printf_path_js, jobjectArray check_sos);

#ifdef __cplusplus
};
#endif

#endif // __BREAKPAD_PROCESSOR_JNI_H__
