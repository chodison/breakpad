#include "breakpad_symbols_jni.h"
#include "processor/logging.h"
#include "base/mylog.h"

int main_symbols_jni(int argc, char **argv);

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpSymbols_nativeExec(JNIEnv *env, jobject obj, jobjectArray commands)
{
	//命令行
    int argc = env->GetArrayLength(commands);
    LOGI("Exec ===> argc %d", argc);

    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(commands, i);
        argv[i] = (char*) env->GetStringUTFChars(js, 0);
        LOGI("Exec ===> argv[%d] %s", i, argv[i]);
    }

    int ret = main_symbols_jni(argc, argv);

	return ret;
}
