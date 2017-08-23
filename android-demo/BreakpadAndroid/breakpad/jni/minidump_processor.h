#ifndef __MINIDUMP_PROCESSOR_H__
#define __MINIDUMP_PROCESSOR_H__

#include <string>
#include <vector>

using std::string;

bool MinidumpProcessExport(const string &minidump_file,
                          const std::vector<string> &symbol_paths,
                          bool machine_readable,
                          bool output_stack_contents);


#endif // __MINIDUMP_PROCESSOR_H__
