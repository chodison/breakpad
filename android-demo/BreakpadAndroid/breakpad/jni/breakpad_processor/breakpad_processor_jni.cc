#include "breakpad_processor_jni.h"
#include "minidump_stackwalk.h"
#include "processor/logging.h"

#define MAX_LOGTEXT_LEN (2048)         /* 每行日志的最大长度*/
#define MAX_LOG_FILE_NAME_LEN (2048)    /* 日志文件名的最大长度*/

FILE *pFile = NULL;
static char szFileName[MAX_LOG_FILE_NAME_LEN] = {0};


static void breakpad_log_callback(void *ptr, int level, const char *fmt, va_list vl)
{
    char line[MAX_LOGTEXT_LEN] = {0};
    memset(line, 0, MAX_LOGTEXT_LEN);

    if(vsnprintf(line, sizeof(line) - 1, fmt, vl) >= 0)
    {
		if(pFile)
		{
			fflush(pFile);
			fclose(pFile);
		}
		pFile = fopen(szFileName, "ab+");
		if(pFile)
		fwrite(line, 1, strlen(line), pFile);
    }

    VLOG(ANDROID_LOG_INFO, MODULE_NAME, fmt, vl);
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpProcessor_nativeExec(JNIEnv *env, jobject obj, jobjectArray commands)
{
    int argc = env->GetArrayLength(commands);
    LOGI("Exec ===> argc %d", argc);
    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(commands, i);
        argv[i] = (char*) env->GetStringUTFChars(js, 0);
        LOGI("Exec ===> argv:%d, %s", i, argv[i]);
    }

    //第一个参数为解释后的trace文件，第二个参数为dump文件
    memset(szFileName, 0, MAX_LOG_FILE_NAME_LEN);
    strcpy(szFileName, argv[0]);
    //设置trace打印回调
    breakpad_log_set_callback(breakpad_log_callback);

    int ret = main_jni(argc, argv);

	if(pFile)
	{
		fflush(pFile);
		fclose(pFile);
		pFile = NULL;
	}

	return ret;
}
