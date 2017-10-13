/*
 * Copyright (c) 2017 chodison <c_soft_dev@163.com>
 *
 * This file is part of libbreakpad_client
 *
 */
#ifndef __BREAKPAD_CLIENT_JNI_H__
#define __BREAKPAD_CLIENT_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeTestCrash(JNIEnv *env, jobject obj);

#ifdef __cplusplus
};
#endif

#endif // __BREAKPAD_CLIENT_JNI_H__