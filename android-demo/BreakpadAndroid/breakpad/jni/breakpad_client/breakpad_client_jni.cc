/*
 * Copyright (c) 2017 chodison <c_soft_dev@163.com>
 *
 * This file is part of libbreakpad_client
 *
 */
#include "breakpad_client_jni.h"
#include "src/client/linux/handler/exception_handler.h"
#include "src/client/linux/handler/minidump_descriptor.h"
#include "base/mylog.h"

static google_breakpad::ExceptionHandler *exception_handler;

bool DumpCallback(const google_breakpad::MinidumpDescriptor& descriptor, void* context, bool succeeded)
{
    LOGI("DumpCallback ===> succeeded %d", succeeded);
    return succeeded;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir)
{
    const char *path = env->GetStringUTFChars(dumpfile_dir, NULL);

    google_breakpad::MinidumpDescriptor descriptor(path);
    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(dumpfile_dir, path);

    LOGI("nativeInit ===> breakpad initialized succeeded, dump file will be saved at %s", path);
    return 1;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_ExceptionHandler_nativeTestCrash(JNIEnv *env, jobject obj)
{
	char *ptr = NULL;
	LOGE("native crash capture begin");
	*ptr = 1;
	LOGE("native crash capture end");
	return 0;
}
