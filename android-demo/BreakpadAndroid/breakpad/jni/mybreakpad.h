/*
 * MyBreakpad.h
 *
 * Created by chodison on 2017/8/22.
 */

#ifndef _MYBREAKPAD_H_
#define _MYBREAKPAD_H_

#include <string>
#include <vector>

using std::string;

#define MAX_LOG_FILE_NAME_LEN (2048)    /* 日志文件名的最大长度*/
#define MAX_SO_NAME_LEN 512
#define MAX_SO_NUM 100

struct ProcessorSoInfo{
	char szFileName[MAX_LOG_FILE_NAME_LEN];
	char checkSoName[MAX_SO_NUM][MAX_SO_NAME_LEN];
	char crashSoIndex[MAX_SO_NUM];
	char crashSoName[MAX_SO_NUM][MAX_SO_NAME_LEN];
	char crashSoAddr[MAX_SO_NUM][MAX_SO_NAME_LEN];
	int  so_num;
	int crash_so_num;
};

class MyBreakpad{
public:
	static MyBreakpad *getInstance();
	static void destroyInstance();

	bool processDumpFile(const char* dump_path);
	ProcessorSoInfo getProcessorSoInfo();
	void setProcessorSoInfo(ProcessorSoInfo *setSoInfo);
private:
	static MyBreakpad *mInstance;
};


#endif /* _MYBREAKPAD_H_ */
