#include <string>
#include <vector>

#include "mybreakpad.h"
#include "minidump_processor.h"
#include "processor/logging.h"
#include "base/mylog.h"

#define MAX_LOGTEXT_LEN (2048)          /* 每行日志的最大长度*/

static bool needCheck = false;
static bool needGetAddr = false;
static ProcessorSoInfo soInfo;
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
    	if(needCheck && soInfo.so_num > 0) {
    		int i = 0;
    		//当出现崩溃的so后紧接着下一行将是堆栈地址，没有就跳过
    		if(needGetAddr) {
    			needGetAddr = false;
				if(strstr(line, "0x")) {
					LOGI("find crash addr: %s", line);
					strcpy(soInfo.crashSoAddr[soInfo.crash_so_num - 1], line);
				}
			}
    		for(i = 0; i< soInfo.so_num; i++) {
    			if(strstr(line, soInfo.checkSoName[i])) {
    				LOGI("find crash [%d]: %s", i, soInfo.checkSoName[i]);
    				soInfo.crashSoIndex[i] = 1;
    				strcpy(soInfo.crashSoName[soInfo.crash_so_num], soInfo.checkSoName[i]);
    				soInfo.crash_so_num ++;
    				needGetAddr = true;
    				break;//每一行只会出现一个so
    			}
    		}
    	}

		if(pFile)
		{
			fflush(pFile);
			fclose(pFile);
		}
		pFile = fopen(soInfo.szFileName, "ab+");
		if(pFile)
			fwrite(line, 1, strlen(line), pFile);
    }

    VLOG(ANDROID_LOG_INFO, MYLOG_MODULE_NAME, fmt, vl);
}

MyBreakpad *MyBreakpad::mInstance = new MyBreakpad();

MyBreakpad *MyBreakpad::getInstance(){
    //设置trace打印回调
    breakpad_log_set_callback(breakpad_log_callback);
	return mInstance;
}

void MyBreakpad::destroyInstance(){
	delete mInstance;
	mInstance=NULL;
}

bool MyBreakpad::processDumpFile(const char* dump_path) {
	std::vector<string> symbol_paths;
	bool ret = false;
	needCheck = true;
	ret = MinidumpProcessExport(dump_path, symbol_paths, false, true);
	if(pFile)
	{
		fflush(pFile);
		fclose(pFile);
	}
	return  ret;
}

ProcessorSoInfo MyBreakpad::getProcessorSoInfo() {
	int i = 0;
	for(i = 0; i< soInfo.so_num; i++) {
		LOGI("get check crash [%d]: %s,%d", i, soInfo.checkSoName[i], soInfo.crashSoIndex[i]);
	}
	return soInfo;
}

void MyBreakpad::setProcessorSoInfo(ProcessorSoInfo *setSoInfo) {
	if(setSoInfo) {
		memcpy(&soInfo, setSoInfo, sizeof(ProcessorSoInfo));
		int i = 0;
		for(i = 0; i< soInfo.so_num; i++) {
			LOGI("set check crash [%d]: %s,%d", i, soInfo.checkSoName[i], soInfo.crashSoIndex[i]);
		}
	}
}





