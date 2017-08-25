#ifndef __MYBREAKPAD_JNI_H__
#define __MYBREAKPAD_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeDumpProcess(JNIEnv *env, jobject obj,
		jstring dump_file_jst, jstring processed_path_jst, jobjectArray check_sos);

JNIEXPORT jobjectArray JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeGetCrashSoName(JNIEnv *env, jobject obj);
JNIEXPORT jobjectArray JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeGetCrashSoAddr(JNIEnv *env, jobject obj);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeTestCrash(JNIEnv *env, jobject obj);

#ifdef __cplusplus
};
#endif

#endif // __MYBREAKPAD_JNI_H__
