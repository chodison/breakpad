#include "breakpad_processor_jni.h"
#include "minidump_stackwalk.h"
#include "processor/logging.h"

#define MAX_LOGTEXT_LEN (2048)         /* 每行日志的最大长度*/
#define MAX_LOG_FILE_NAME_LEN (2048)    /* 日志文件名的最大长度*/

FILE *pFile = NULL;
static char szFileName[MAX_LOG_FILE_NAME_LEN] = {0};
static int so_num = 0;
static char** checkSoFileName;
static char* crashSoFileNameIndex;
static bool needCheck = false;

static void breakpad_log_callback(void *ptr, int level, const char *fmt, va_list vl)
{
    char line[MAX_LOGTEXT_LEN] = {0};
    memset(line, 0, MAX_LOGTEXT_LEN);

    if(vsnprintf(line, sizeof(line) - 1, fmt, vl) >= 0)
    {
    	if(strstr(line, "Loaded modules"))
    		needCheck = false;
    	if(needCheck && checkSoFileName && so_num > 0) {
    		int i = 0;
    		for(i = 0; i< so_num; i++) {
    			if(strstr(line, checkSoFileName[i])) {
    				crashSoFileNameIndex[i] = 1;
    			}
    		}
    	}

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

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_DumpProcessor_nativeExec(JNIEnv *env, jobject obj,
		jobjectArray commands, jstring printf_path_js, jobjectArray check_sos)
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
    //需要检查的so库
    so_num = env->GetArrayLength(check_sos);
    LOGI("check so num: %d", so_num);

    checkSoFileName = (char **)malloc(so_num*sizeof(char *));
    crashSoFileNameIndex = (char *)malloc(so_num*sizeof(char));
    memset(crashSoFileNameIndex, 0, so_num);
    for (i = 0; i < so_num; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(check_sos, i);
        checkSoFileName[i] = (char*) env->GetStringUTFChars(js, 0);
        LOGI("checkSoFileName[%d]: %s", i, checkSoFileName[i]);
    }
    //保存解析后的trace文件
    char *printf_path = (char*) env->GetStringUTFChars(printf_path_js, 0);
    memset(szFileName, 0, MAX_LOG_FILE_NAME_LEN);
    strcpy(szFileName, printf_path);
    LOGI("print save path: %s", printf_path);
    //设置trace打印回调
    breakpad_log_set_callback(breakpad_log_callback);

    needCheck = true;
    int ret = main_jni(argc, argv);

    for (i = 0; i < so_num; i++) {
    	if(crashSoFileNameIndex[i])
    		LOGE("crashSoFileName[%d]: %s", i, checkSoFileName[i]);
    }

    for (i = 0; i < so_num; i++) {
    	if(checkSoFileName[i])
    		free(checkSoFileName[i]);
    }
    if(checkSoFileName)
    	free(checkSoFileName);
    if(crashSoFileNameIndex)
    	free(crashSoFileNameIndex);

	if(pFile)
	{
		fflush(pFile);
		fclose(pFile);
		pFile = NULL;
	}

	return ret;
}
