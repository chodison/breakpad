#include "breakpad_processor_jni.h"
#include "minidump_stackwalk.h"
#include "base/mylog.h"


JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpProcessor_nativeExec(JNIEnv *env, jobject obj, jobjectArray commands)
{
    int argc = env->GetArrayLength(commands);
    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(commands, i);
        argv[i] = (char*) env->GetStringUTFChars(js, 0);
    }
    return main_jni(argc, argv);
}