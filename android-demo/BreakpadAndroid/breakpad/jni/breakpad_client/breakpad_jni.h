#ifndef BREAKPAD_JNI_H
#define BREAKPAD_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeTestCrash(JNIEnv *env, jobject obj);

#ifdef __cplusplus
};
#endif

#endif // BREAKPAD_JNI_H