/*
 * Copyright (c) 2017 chodison <c_soft_dev@163.com>
 *
 * This file is part of libmybreakpad
 *
 */

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
    int  crashSoValid;
    char firstCrashSoName[MAX_SO_NAME_LEN];
    int  so_num;
    int  crash_so_num;
    /**
    常量	    解释
    SIGTERM	发送给程序的终止请求
    SIGSEGV	非法内存访问（段错误）
    SIGINT	外部中断，通常为用户所发动
    SIGILL	非法程序映像，例如非法指令
    SIGABRT	异常终止条件，例如 abort() 所起始的
    SIGFPE	错误的算术运算，如除以零
     */
    char signal_type[10];
};

static google_breakpad::ExceptionHandler* mExceptionHandler;
JavaVM *mJVM = NULL;
void *mWeak_thiz;

#define STATUS_IDLE  0
#define STATUS_INIT  1
#define STATUS_BEGIN  2
#define STATUS_BEGIN2  22
#define STATUS_END  3

#define EVENT_WHAT_INIT			100
#define EVENT_WHAT_PROCESS		200

#define EVENT_INIT_SUCCESS			1000
#define EVENT_INIT_FAILED			1001
#define EVENT_INIT_DUMPDIR_NULL		1002

#define EVENT_PROCESS_SUCCESS					2000
#define EVENT_PROCESS_FAILED					2001
#define EVENT_PROCESS_OBJFAIL_CLASS				2002
#define EVENT_PROCESS_OBJFAIL_METHOD_INIT		200301
#define EVENT_PROCESS_OBJFAIL_METHOD_NEWOBJ     200302
#define EVENT_PROCESS_OBJFAIL_METHOD_GETFEILD   200303
#define EVENT_PROCESS_DUMPFILE_NULL				2004

static volatile int needCheckStatus = STATUS_IDLE;
static volatile bool needGetAddr = false;
static volatile bool needCheckValid = false;
static volatile bool needNextFind = false;
static ProcessorSoInfo mSoInfo;
static pthread_mutex_t mutex;
FILE *pFile = NULL;

static bool isBaseLibrary(char* libstr)
{
    bool isBaseLib = false;
    if(!libstr) {
        goto fail;
    }
    if(strstr(libstr, "libc.so") ||
       strstr(libstr, "libc++.so")) {
        isBaseLib = true;
    }
    fail:
    return isBaseLib;
}

static void breakpad_log_callback(void *ptr, int level, const char *fmt, va_list vl)
{
    char line[MAX_LOGTEXT_LEN] = {0};
    memset(line, 0, MAX_LOGTEXT_LEN);

    if(vsnprintf(line, sizeof(line) - 1, fmt, vl) >= 0)
    {
        if(strstr(line, "Crash reason") && strstr(line, "SIG")) {
            LOGI("find Crash reason,line: %s", line);
            strcpy(mSoInfo.signal_type, strstr(line, "SIG"));
        }
        //if(strstr(line, "Loaded modules"))
        //只需找第一个Thread即可
        if(needCheckStatus == STATUS_BEGIN && (strstr(line, "Thread") || strstr(line, "Loaded modules")))
            needCheckStatus = STATUS_END;
        //有的dump文件处理后Thread的后面没有crashed
        if(needCheckStatus == STATUS_INIT && strstr(line, "Thread") /*&& strstr(line, "crashed")*/)
            needCheckStatus = STATUS_BEGIN;

        //当查找到dalvik-main space (deleted)可以终止了
        if(needCheckStatus == STATUS_BEGIN && needNextFind &&
                strstr(line, "dalvik-main space") != NULL &&
                /*找到最后一个SO不是基础库为止*/
                (mSoInfo.crash_so_num >= 1 && !isBaseLibrary(mSoInfo.crashSoName[mSoInfo.crash_so_num-1]))) {
            needCheckStatus = STATUS_END;
        }

        //Found by：given as instruction pointer in context块里面不包含so
        //FIXME:需要重新确定筛选规则
        if(needCheckStatus == STATUS_BEGIN && strlen(mSoInfo.firstCrashSoName) < 1 &&
           strstr(line, "given as instruction pointer in context")) {
            LOGE("do not find so name for first Block");
#if 0 //暂时关闭
            strcpy(mSoInfo.firstCrashSoName, "FNOSONAME");
            needCheckStatus == STATUS_END;
#endif
        }

        //检查每一行
        if(needCheckStatus == STATUS_BEGIN) {
        	if(strlen(mSoInfo.firstCrashSoName) < 1 && strstr(line, ".so")) {
				LOGI("first crash so name: %s", line);
				strcpy(mSoInfo.firstCrashSoName, line);
			}
        	//有需要查找的so并且查找到崩溃的so堆栈信息数小于最大的so的个数
        	if(mSoInfo.so_num > 0 && mSoInfo.crash_so_num < MAX_SO_NUM) {
				int i = 0;

				//找到有效崩溃信息 "Found by: given as instruction pointer in context"
                if(needCheckValid && !needGetAddr && strstr(line, "Found by")) {
                    LOGI("valid text: %s", line);
                    if(strstr(line, "given as instruction pointer in context")) {
                        LOGI("find valid text: %s", line);
                        mSoInfo.crashSoValid = 1;
                        needNextFind = true;
                    } else {
                        //确保是连续的
                        mSoInfo.crashSoValid = 0;
                    }
                    needCheckValid = false;
                }

				//当出现崩溃的so后紧接着下一行将是堆栈地址，没有就跳过
				if(needGetAddr) {
					needGetAddr = false;
					if(strstr(line, "0x")) {
						LOGI("find crash addr: %s", line);
						strcpy(mSoInfo.crashSoAddr[mSoInfo.crash_so_num - 1], strstr(line, "0x"));
					} else {
						LOGE("can not find crash addr: %s", line);
						strcpy(mSoInfo.crashSoAddr[mSoInfo.crash_so_num - 1], "NA");
					}
				}

                if(strstr(line, ".so")) {
                    for(i = 0; i< mSoInfo.so_num; i++) {
                        if(strstr(line, mSoInfo.checkSoName[i])) {
                            LOGI("find crash [%d]: %s", i, mSoInfo.checkSoName[i]);
                            //排除后面找到的基础库libc.so
                            if (needNextFind && isBaseLibrary(mSoInfo.checkSoName[i]) &&
                                !isBaseLibrary(mSoInfo.crashSoName[mSoInfo.crash_so_num-1])) {
                                needNextFind = false;
                                needCheckStatus = STATUS_END;
                                break;
                            } else {
                                if(mSoInfo.crash_so_num > 0)
                                    LOGI("current last crash soname: %s", mSoInfo.crashSoName[mSoInfo.crash_so_num - 1]);
                                if(mSoInfo.crash_so_num > 0 && isBaseLibrary(mSoInfo.checkSoName[i]) &&
                                   !isBaseLibrary(mSoInfo.crashSoName[mSoInfo.crash_so_num-1])) {
                                    needCheckStatus = STATUS_END;
                                } else {
                                    mSoInfo.crashSoIndex[i] = 1;
                                    strcpy(mSoInfo.crashSoName[mSoInfo.crash_so_num],
                                           mSoInfo.checkSoName[i]);
                                    mSoInfo.crash_so_num++;
                                    needGetAddr = true;
                                    needCheckValid = true;
                                }

                                break;//每一行只会出现一个so
                            }
                        }

                        //不连续了，不需要再处已经理后续的so
                        if(needNextFind && i == mSoInfo.so_num - 1) {
                            if(mSoInfo.crash_so_num >= 1 && isBaseLibrary(mSoInfo.crashSoName[mSoInfo.crash_so_num-1])) {
                                strcpy(mSoInfo.crashSoName[mSoInfo.crash_so_num],
                                       strstr(line, "lib"));
                                mSoInfo.crash_so_num++;
                                needGetAddr = true;
                                needCheckValid = true;
                                needCheckStatus = STATUS_BEGIN2;
                                LOGI("change status to STATUS_BEGIN2");
                            } else {
                                needNextFind = false;
                                needCheckStatus = STATUS_END;
                            }
                        }
                    }
				}
        	}
        } else if(needNextFind && needCheckStatus == STATUS_BEGIN2) {
            if(needGetAddr) {
                needGetAddr = false;
                if(strstr(line, "0x")) {
                    LOGI("find crash addr: %s", line);
                    strcpy(mSoInfo.crashSoAddr[mSoInfo.crash_so_num - 1], strstr(line, "0x"));
                } else {
                    LOGE("can not find crash addr: %s", line);
                    strcpy(mSoInfo.crashSoAddr[mSoInfo.crash_so_num - 1], "NA");
                }
                needNextFind = false;
                needCheckStatus = STATUS_END;
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
    needCheckStatus = STATUS_INIT;
    needGetAddr = false;
    needCheckValid = false;
    needNextFind = false;
    ret = MinidumpProcessExport(dump_path, symbol_paths, false, true);
    if(pFile)
    {
        fflush(pFile);
        fclose(pFile);
        pFile = NULL;
    }
    return  ret;
}

void onNativeEventReport(int what, int arg1, int arg2, jobject obj) {
    JNIEnv *env = 0;
    bool isAttach = false;

    if (mWeak_thiz == NULL) {
        LOGE("mWeak_thiz is null");
        return;
    }
    jobject weak_thiz = (jobject) mWeak_thiz;

    int result = mJVM->GetEnv((void **) &env, JNI_VERSION_1_4);
    if (result != JNI_OK) {
        LOGE("mJVM->GetEnv failed");
        if(mJVM->AttachCurrentThread(&env, NULL)) {
            isAttach = false;
			LOGE("%s:%d: AttachCurrentThread fail!", __FUNCTION__, __LINE__);
			return;
		} else {
			LOGI("Attached success.");
			isAttach = true;
		}
    }

    LOGI("onNativeCrash ===> succeeded %d-%d-%d", what, arg1, arg2);

    jclass crashClass = env->FindClass(NATIVE_CLASS_NAME);
	if (crashClass == NULL) {
		LOGE("FindClass %s null",NATIVE_CLASS_NAME);
		if (isAttach) {
			if(mJVM->DetachCurrentThread()) {
				LOGE("%s:%d: DetachCurrentThread fail!\n", __FUNCTION__, __LINE__);
				return;
			}
		}
		return;
	}

	jmethodID crashReportMethod = env->GetStaticMethodID(crashClass,
			"onNativeCrashEvent", "(Ljava/lang/Object;IIILjava/lang/Object;)V");
	if (env->ExceptionCheck()) {
		env->ExceptionDescribe();
		env->ExceptionClear();
	}
	if (crashReportMethod == NULL) {
		LOGE("GetMethod onNativeCrash null");
		if (isAttach) {
			if(mJVM->DetachCurrentThread()) {
				LOGE("%s:%d: DetachCurrentThread fail!\n", __FUNCTION__, __LINE__);
				return;
			}
		}
		return;
	}
	env->CallStaticVoidMethod(crashClass, crashReportMethod, mWeak_thiz, what, arg1, arg2, obj);

    if (env->ExceptionCheck()) {
	   env->ExceptionDescribe();
	   env->ExceptionClear();
   }

	if (isAttach) {
		if(mJVM->DetachCurrentThread()) {
			LOGE("%s:%d: DetachCurrentThread fail!\n", __FUNCTION__, __LINE__);
			return;
		}
	}
}

void onNativeEventReport_arg1(int what, int arg1) {
	onNativeEventReport(what, arg1, 0, NULL);
}

bool DumpCallback(const google_breakpad::MinidumpDescriptor& descriptor, void* context, bool succeeded)
{
    LOGI("DumpCallback ===> succeeded %d", succeeded);
    return succeeded;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void* /*reserved*/) {
    LOGI("JNI_OnLoad");
    JNIEnv* env = NULL;
    mJVM = vm;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);

    pthread_mutex_init(&mutex, NULL);

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnUnload");
    pthread_mutex_destroy(&mutex);
}

JNIEXPORT void JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeSetup(JNIEnv *env, jobject obj, jobject weak_this)
{
	mWeak_thiz = env->NewGlobalRef(weak_this);
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir)
{
	if(dumpfile_dir == NULL) {
		onNativeEventReport_arg1(EVENT_WHAT_INIT, EVENT_INIT_DUMPDIR_NULL);
	}

    const char *path = env->GetStringUTFChars(dumpfile_dir, NULL);

    google_breakpad::MinidumpDescriptor descriptor(path);
//    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
    mExceptionHandler = new google_breakpad::ExceptionHandler(descriptor, NULL, DumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(dumpfile_dir, path);

    //设置trace打印回调
    breakpad_log_set_callback(breakpad_log_callback);

    onNativeEventReport_arg1(EVENT_WHAT_INIT, EVENT_INIT_SUCCESS);

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
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_OBJFAIL_CLASS);
        return NULL;
    }
    jmethodID crashInfoID = env->GetMethodID(crashInfo, "<init>", "()V");
    if(crashInfoID == NULL) {
        LOGE("Process ===>get NativeCrashInfo MethodID failed");
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_OBJFAIL_METHOD_INIT);
        return NULL;
    }
    jobject crashInfoObj = env->NewObject(crashInfo, crashInfoID);
    if(crashInfoObj == NULL) {
        LOGE("Process ===>NewObject NativeCrashInfo failed");
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_OBJFAIL_METHOD_NEWOBJ);
        return NULL;
    }
    jfieldID crashSoName_fid = env->GetFieldID(crashInfo, "crashSoName", "[Ljava/lang/String;");
    jfieldID crashSoAddr_fid = env->GetFieldID(crashInfo, "crashSoAddr", "[Ljava/lang/String;");
    jfieldID firstSoName_fid = env->GetFieldID(crashInfo, "firstCrashSoName", "Ljava/lang/String;");
    jfieldID existAppSo_fid = env->GetFieldID(crashInfo, "exist_app_so", "I");
    jfieldID siganltype_fid = env->GetFieldID(crashInfo, "signal_type", "Ljava/lang/String;");
    if(crashSoName_fid == NULL || crashSoAddr_fid == NULL
    || firstSoName_fid == NULL || existAppSo_fid == NULL) {
        LOGE("Process ===> NativeCrashInfo GetFieldID failed");
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_OBJFAIL_METHOD_GETFEILD);
        return NULL;
    }

    //初始化SO信息
    memset(&mSoInfo, 0, sizeof(ProcessorSoInfo));
    strcpy(mSoInfo.signal_type, "UNKNOWN");

    //需要处理的dump文件
    if(dump_file_jst == NULL) {
        LOGE("Process ===> dump file dir is null");
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_DUMPFILE_NULL);
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
    jstring firstSoName_jst;
    jstring sinaltype_jst;
    //处理成功
    if(ret) {
        //信号量类型
        sinaltype_jst = env->NewStringUTF(mSoInfo.signal_type);
        env->SetObjectField(crashInfoObj, siganltype_fid, sinaltype_jst);
        LOGE("Process ===>signal_type: %s", mSoInfo.signal_type);
    	//第一个崩溃so未找到
    	if(strlen(mSoInfo.firstCrashSoName) < 1) {
    		strcpy(mSoInfo.firstCrashSoName, "NA");
    	}
    	//打印崩溃的so
        for (i = 0; i < so_num; i++) {
            if(mSoInfo.crashSoIndex[i]) {
                LOGE("Process ===>crashSoFileName[%d]: %s", i, mSoInfo.checkSoName[i]);
            }
        }
        //构造crashInfoObj并赋值
        jclass stringClass = env->FindClass("java/lang/String");
        //FIXME:given as instruction pointer in context 板块没有so信息，但是有基础库信息得保留这种情况
        if(mSoInfo.crash_so_num > 0 && (mSoInfo.crashSoValid || !isBaseLibrary(mSoInfo.crashSoName[mSoInfo.crash_so_num-1]))) {
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
            firstSoName_jst = env->NewStringUTF(mSoInfo.firstCrashSoName);
            env->SetObjectField(crashInfoObj, firstSoName_fid, firstSoName_jst);
        }
        onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_SUCCESS);
    } else {
        env->SetIntField(crashInfoObj, existAppSo_fid, 0);
        strcpy(mSoInfo.firstCrashSoName, "NAF");
        firstSoName_jst = env->NewStringUTF(mSoInfo.firstCrashSoName);
        env->SetObjectField(crashInfoObj, firstSoName_fid, firstSoName_jst);
    	onNativeEventReport_arg1(EVENT_WHAT_PROCESS, EVENT_PROCESS_FAILED);
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
