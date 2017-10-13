/*
 * Copyright (c) 2017 chodison <c_soft_dev@163.com>
 *
 * This file is part of libmybreakpad
 *
 */
#ifndef __MYBREAKPAD_JNI_H__
#define __MYBREAKPAD_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeSetup(JNIEnv *env, jobject obj, jobject weak_this);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir);

JNIEXPORT jobject JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeDumpProcess(JNIEnv *env, jobject obj,
		jstring dump_file_jst, jstring processed_path_jst, jobjectArray check_sos);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeTestCrash(JNIEnv *env, jobject obj);

#ifdef __cplusplus
};
#endif

#endif // __MYBREAKPAD_JNI_H__
