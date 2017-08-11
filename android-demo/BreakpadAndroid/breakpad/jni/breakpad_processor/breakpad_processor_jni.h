#ifndef __BREAKPAD_PROCESSOR_JNI_H__
#define __BREAKPAD_PROCESSOR_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpProcessor_nativeExec(JNIEnv *env, jobject obj, jobjectArray commands);

#ifdef __cplusplus
};
#endif

#endif // __BREAKPAD_PROCESSOR_JNI_H__