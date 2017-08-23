#include "mybreakpad_jni.h"

#include "src/client/linux/handler/exception_handler.h"
#include "src/client/linux/handler/minidump_descriptor.h"
#include "mybreakpad.h"
#include "base/mylog.h"

bool DumpCallback(const google_breakpad::MinidumpDescriptor& descriptor, void* context, bool succeeded)
{
    LOGI("DumpCallback ===> succeeded %d", succeeded);
    return succeeded;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeInit(JNIEnv *env, jobject obj, jstring dumpfile_dir)
{
    const char *path = env->GetStringUTFChars(dumpfile_dir, NULL);

    google_breakpad::MinidumpDescriptor descriptor(path);
    static google_breakpad::ExceptionHandler eh(descriptor, NULL, DumpCallback, NULL, true, -1);
    env->ReleaseStringUTFChars(dumpfile_dir, path);

    LOGI("nativeInit ===> breakpad initialized succeeded, dump file will be saved at %s", path);
    return 1;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeDumpProcess(JNIEnv *env, jobject obj,
		jstring dump_file_jst, jstring processed_path_jst, jobjectArray check_sos)
{
    int i;
    //需要检查的so库
    int so_num = env->GetArrayLength(check_sos);
    LOGI("Process ===>check so num: %d", so_num);

    ProcessorSoInfo mSoInfo;
    memset(&mSoInfo, 0, sizeof(ProcessorSoInfo));
    mSoInfo.so_num = so_num;

    char *checkSoFileName;
    for (i = 0; i < so_num; i++) {
        jstring js = (jstring) env->GetObjectArrayElement(check_sos, i);
        checkSoFileName = (char*) env->GetStringUTFChars(js, 0);
        LOGI("Process ===>checkSoFileName[%d]: %s", i, checkSoFileName);
        strcpy(mSoInfo.checkSoName[i], checkSoFileName);
    }
    //需要处理的dump文件
    char *dump_file = (char*) env->GetStringUTFChars(dump_file_jst, 0);
    LOGI("Process ===> dump file dir: %s", dump_file);
    //解析后的trace文件
    char *processed_path = (char*) env->GetStringUTFChars(processed_path_jst, 0);
    strcpy(mSoInfo.szFileName, processed_path);
    LOGI("Process ===>processed file save path: %s", processed_path);

    //设置ProcessorSoInfo
    MyBreakpad::getInstance()->setProcessorSoInfo(&mSoInfo);

    int ret = MyBreakpad::getInstance()->processDumpFile(dump_file) ? 0 : 1;

    ProcessorSoInfo mSoInfo_ = MyBreakpad::getInstance()->getProcessorSoInfo();

    for (i = 0; i < so_num; i++) {
    	if(mSoInfo_.crashSoIndex[i]) {
    		LOGE("Process ===>crashSoFileName[%d]: %s", i, mSoInfo_.checkSoName[i]);
    	}
    }

    for (i = 0; i < mSoInfo_.crash_so_num; i++) {
    	LOGE("Process ===>2 crashSoFileName[%d]: %s", i, mSoInfo_.crashSoName[i]);
    }

	return ret;
}

JNIEXPORT jobjectArray JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeGetCrashSoName(JNIEnv *env, jobject obj)
{
	ProcessorSoInfo mSoInfo_ = MyBreakpad::getInstance()->getProcessorSoInfo();
	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray row = env->NewObjectArray(mSoInfo_.crash_so_num, stringClass, 0);
	jsize i;
	for (i = 0; i < mSoInfo_.crash_so_num; i ++)
	{
		env->SetObjectArrayElement(row, i, env->NewStringUTF(mSoInfo_.crashSoName[i]));
	}
	return row;
}

JNIEXPORT jint JNICALL Java_com_chodison_mybreakpad_NativeMybreakpad_nativeTestCrash(JNIEnv *env, jobject obj)
{
	char *ptr = NULL;
	LOGE("native crash capture begin");
	*ptr = 1;
	LOGE("native crash capture end");
	return 0;
}
