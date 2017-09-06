#include <pthread.h>
#include "mybreakpad_jni.h"

#include "src/client/linux/handler/exception_handler.h"
#include "src/client/linux/handler/minidump_descriptor.h"
#include "minidump_processor.h"
#include "processor/logging.h"

#define NATIVE_CLASS_NAME "com/chodison/mybreakpad/NativeMybreakpad"
#define MAX_LOGTEXT_LEN (2048)          /* 每行日志的最大长度*/
#define MAX_LOG_FILE_NAME_LEN (2048)    /* 日志文件名的最大长度*/
#define MAX_SO_NAME_LEN 512
#define MAX_SO_NUM 100

struct ProcessorSoInfo{
    char szFileName[MAX_LOG_FILE_NAME_LEN];
    char checkSoName[MAX_SO_NUM][MAX_SO_NAME_LEN];
    char crashSoIndex[MAX_SO_NUM];
    char crashSoName[MAX_SO_NUM][MAX_SO_NAME_LEN];
    char crashSoAddr[MAX_SO_NUM][MAX_SO_NAME_LEN];
    char firstCrashSoName[MAX_SO_NAME_LEN];
    int  so_num;
    int crash_so_num;
};

static google_breakpad::ExceptionHandler* mExceptionHandler;
JavaVM *mJVM = NULL;

static volatile bool needCheck = false;
static volatile bool needGetAddr = false;
static ProcessorSoInfo mSoInfo;
static pthread_mutex_t mutex;
FILE *pFile = NULL;

static void breakpad_log_callback(void *ptr, int level, const char *fmt, va_list vl)
{
    char line[MAX_LOGTEXT_LEN] = {0};
    memset(line, 0, MAX_LOGTEXT_LEN);

    if(vsnprintf(line, sizeof(line) - 1, fmt, vl) >= 0)
    {
        if(strstr(line, "Loaded modules"))
            needCheck = false;
        //检查每一行
        if(needCheck && mSoInfo.so_num > 0) {
            int i = 0;
            //当出现崩溃的so后紧接着下一行将是堆栈地址，没有就跳过
            if(needGetAddr) {
                needGetAddr = false;
                if(strstr(line, "0x")) {
                    LOGI("find crash addr: %s", line);
                    strcpy(mSoInfo.crashSoAddr[mSoInfo.crash_so_num - 1], strstr(line, "0x"));
                }
            }
            if(strlen(mSoInfo.firstCrashSoName) < 1 && strstr(line, ".so")) {
                LOGI("first crash so name: %s", line);
                strcpy(mSoInfo.firstCrashSoName, line);
            }
            for(i = 0; i< mSoInfo.so_num; i++) {
                if(strstr(line, mSoInfo.checkSoName[i])) {
                    LOGI("find crash [%d]: %s", i, mSoInfo.checkSoName[i]);
                    mSoInfo.crashSoIndex[i] = 1;
                    strcpy(mSoInfo.crashSoName[mSoInfo.crash_so_num], mSoInfo.checkSoName[i]);
                    mSoInfo.crash_so_num ++;
                    needGetAddr = true;
                    break;//每一行只会出现一个so
                }
            }
        }

        if(pFile == NULL)
            pFile = fopen(mSoInfo.szFileName, "ab+");
        if(pFile)
            fwrite(line, 1, strlen(line), pFile);
    }

#ifdef LOGDEBUG
    VLOG(ANDROID_LOG_INFO, MYLOG_MODULE_NAME, fmt, vl);
#endif
}

bool processDumpFile(const char* dump_path) {
    std::vector<string> symbol_paths;
    bool ret = false;
    needCheck = true;
    needGetAddr = false;
    ret = MinidumpProcessExport(dump_path, symbol_paths, false, true);
    if(pFile)
    {
        fflush(pFile);
        fclose(pFile);
        pFile = NULL;
    }
    return  ret;
}

void onNativeCrash(int success) {
    JNIEnv *env = 0;
    int result = mJVM->GetEnv((void **) &env, JNI_VERSION_1_4);
    if (result != JNI_OK) {
        LOGE("mJVM->GetEnv null");
        return;
    }
    jclass crashClass = env->FindClass(NATIVE_CLASS_NAME);
    if (crashClass == NULL) {
        LOGE("FindClass %s null",NATIVE_CLASS_NAME);
        return;
    }
    jmethodID crashReportMethod = env->GetStaticMethodID(crashClass,
            "onNativeCrash", "(I)V");
    if (crashReportMethod == NULL) {
        LOGE("GetMethod onNativeCrash null");
        return;
    }
    env->CallStaticVoidMethod(crashClass, crashReportMethod, success);
    LOGI("onNativeCrash ===> succeeded %d", success);
}

bool DumpCallback(const google_breakpad::MinidumpDescriptor& descriptor, void* context, bool succeeded)
{
	onNativeCrash(succeeded ? 1:0);
    LOGI("DumpCallback ===> succeeded %d", succeeded);
    return succeeded;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void* /*reserved*/) {
    LOGI("JNI_OnLoad");
    mJVM = vm;
    pthread_mutex_init(&mutex, NULL);
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnUnload");
    pthread_mutex_destroy(&mutex);
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir)
{
    const char *path = env->GetStringUTFChars(dumpfile_dir, NULL);

    google_breakpad::MinidumpDescriptor descriptor(path);
//    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
    mExceptionHandler = new google_breakpad::ExceptionHandler(descriptor, NULL, DumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(dumpfile_dir, path);

    //设置trace打印回调
    breakpad_log_set_callback(breakpad_log_callback);

    LOGI("nativeInit ===> breakpad initialized succeeded, dump file will be saved at %s", path);
    return 1;
}

JNIEXPORT jobject JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeDumpProcess(JNIEnv *env, jobject obj,
              jstring dump_file_jst, jstring processed_path_jst, jobjectArray check_sos) {

    pthread_mutex_lock(&mutex);
    int i;

    //初始化需要返回的jobject
    jclass crashInfo = env->FindClass("com/chodison/mybreakpad/NativeCrashInfo");
    if(crashInfo == NULL) {
        LOGE("Process ===>get NativeCrashInfo failed");
        return NULL;
    }
    jmethodID crashInfoID = env->GetMethodID(crashInfo, "<init>", "()V");
    if(crashInfoID == NULL) {
        LOGE("Process ===>get NativeCrashInfo MethodID failed");
        return NULL;
    }
    jobject crashInfoObj = env->NewObject(crashInfo, crashInfoID);
    if(crashInfoObj == NULL) {
        LOGE("Process ===>NewObject NativeCrashInfo failed");
        return NULL;
    }
    jfieldID crashSoName_fid = env->GetFieldID(crashInfo, "crashSoName", "[Ljava/lang/String;");
    jfieldID crashSoAddr_fid = env->GetFieldID(crashInfo, "crashSoAddr", "[Ljava/lang/String;");
    jfieldID firstSoName_fid = env->GetFieldID(crashInfo, "firstCrashSoName", "Ljava/lang/String;");
    jfieldID existAppSo_fid = env->GetFieldID(crashInfo, "exist_app_so", "I");
    if(crashSoName_fid == NULL || crashSoAddr_fid == NULL
    || firstSoName_fid == NULL || existAppSo_fid == NULL) {
        LOGE("Process ===> NativeCrashInfo GetFieldID failed");
        return NULL;
    }

    //初始化SO信息
    memset(&mSoInfo, 0, sizeof(ProcessorSoInfo));

    //需要处理的dump文件
    if(dump_file_jst == NULL) {
        LOGE("Process ===> dump file dir is null");
        return NULL;
    }
    char *dump_file = (char *) env->GetStringUTFChars(dump_file_jst, 0);
    LOGI("Process ===> dump file dir: %s", dump_file);

    //解析后的trace文件
    if (processed_path_jst != NULL) {
        char *processed_path = (char *) env->GetStringUTFChars(processed_path_jst, 0);
        strcpy(mSoInfo.szFileName, processed_path);
        LOGI("Process ===>processed file save path: %s", processed_path);
    }

    //需要检查的so库
    int so_num = env->GetArrayLength(check_sos);
    LOGI("Process ===>check so num: %d", so_num);

    mSoInfo.so_num = so_num;

    char *checkSoFileName;
    for (i = 0; i < so_num; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(check_sos, i);
        checkSoFileName = (char *) env->GetStringUTFChars(js, 0);
        LOGI("Process ===>checkSoFileName[%d]: %s", i, checkSoFileName);
        strcpy(mSoInfo.checkSoName[i], checkSoFileName);
    }

    //处理dump文件
    bool ret = processDumpFile(dump_file);
    //处理成功
    if(ret) {
        for (i = 0; i < so_num; i++) {
            if(mSoInfo.crashSoIndex[i]) {
                LOGE("Process ===>crashSoFileName[%d]: %s", i, mSoInfo.checkSoName[i]);
            }
        }

        jclass stringClass = env->FindClass("java/lang/String");
        if(mSoInfo.crash_so_num > 0) {
            jobjectArray soname = env->NewObjectArray(mSoInfo.crash_so_num, stringClass, 0);
            jobjectArray soaddr = env->NewObjectArray(mSoInfo.crash_so_num, stringClass, 0);
            for (i = 0; i < mSoInfo.crash_so_num; i++) {
                LOGE("Process ===>2 crashSoFileName[%d]: %s%s", i, mSoInfo.crashSoName[i],
                     mSoInfo.crashSoAddr[i]);
                env->SetObjectArrayElement(soname, i, env->NewStringUTF(mSoInfo.crashSoName[i]));
                env->SetObjectArrayElement(soaddr, i, env->NewStringUTF(mSoInfo.crashSoAddr[i]));
            }
            env->SetObjectField(crashInfoObj, crashSoName_fid, soname);
            env->SetObjectField(crashInfoObj, crashSoAddr_fid, soaddr);

            env->SetIntField(crashInfoObj, existAppSo_fid, 1);
        } else {
            env->SetIntField(crashInfoObj, existAppSo_fid, 0);
        }

        if(strlen(mSoInfo.firstCrashSoName) > 0) {
            jstring firstSoName_jst = env->NewStringUTF(mSoInfo.firstCrashSoName);
            env->SetObjectField(crashInfoObj, firstSoName_fid, firstSoName_jst);
        }
    }
    pthread_mutex_unlock(&mutex);

    return crashInfoObj;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeTestCrash(JNIEnv *env, jobject obj)
{
	char *ptr = NULL;
	LOGE("native crash capture begin");
	*ptr = 1;
	LOGE("native crash capture end");
	return 0;
}
