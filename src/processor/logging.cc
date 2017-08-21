// Copyright (c) 2007, Google Inc.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// logging.cc: Breakpad logging
//
// See logging.h for documentation.
//
// Author: Mark Mentovai

#include <assert.h>
#include <errno.h>
#include <string.h>
#include <time.h>

#include <string>

#include "common/stdio_wrapper.h"
#include "common/using_std_string.h"
#include "processor/logging.h"
#include "processor/pathname_stripper.h"

static void (*breakpad_log_callback)(void*, int, const char*, va_list) = NULL;

static void breakpad_vlog(void* model, int level, const char *fmt, va_list vl)
{
    void (*log_callback)(void*, int, const char*, va_list) = breakpad_log_callback;
    if (log_callback)
        log_callback(model, level, fmt, vl);
}

void breakpad_log(void* model, int level, const char *fmt, ...)
{
    va_list vl;
    va_start(vl, fmt);
    breakpad_vlog(model, level, fmt, vl);
    va_end(vl);
}

void breakpad_log2(const char *fmt, ...)
{
    va_list vl;
    va_start(vl, fmt);
    breakpad_vlog((void*)"", 0, fmt, vl);
    va_end(vl);
}

void breakpad_log_set_callback(void (*callback)(void*, int, const char*, va_list))
{
	breakpad_log_callback = callback;
}

namespace google_breakpad {

LogStream::LogStream(std::ostream &stream, Severity severity,
                     const char *file, int line)
    : stream_(stream) {
  time_t clock;
  time(&clock);
  struct tm tm_struct;
#ifdef _WIN32
  localtime_s(&tm_struct, &clock);
#else
  localtime_r(&clock, &tm_struct);
#endif
  strftime(time_string, sizeof(time_string), "%Y-%m-%d %H:%M:%S", &tm_struct);

  const char *severity_string = "UNKNOWN_SEVERITY";
  switch (severity) {
    case SEVERITY_INFO:
      severity_string = "INFO";
      alog_level = ANDROID_LOG_INFO;
      break;
    case SEVERITY_ERROR:
      severity_string = "ERROR";
      alog_level = ANDROID_LOG_ERROR;
      break;
  }

  stream_ << time_string << ": " << PathnameStripper::File(file) << ":" <<
             line << ": " << severity_string << ": ";
  file_ = (char*)PathnameStripper::File(file).c_str();
  line_ = line;
}

//template<typename T>
//std::ostream& LogStream::operator<<(const T &t) {
//	  std::ostringstream ost;
//	  ost<< time_string << ": " << file_ << ":" <<
//	             line_ << " "<<t;
//	  std::string logstr = ost.str();
//	  report_log(alog_level, logstr.c_str());
//  return stream_ << t;
//
//}

LogStream::~LogStream() {
  stream_ << std::endl;
}

string HexString(uint32_t number) {
  char buffer[11];
  snprintf(buffer, sizeof(buffer), "0x%x", number);
  return string(buffer);
}

string HexString(uint64_t number) {
  char buffer[19];
  snprintf(buffer, sizeof(buffer), "0x%" PRIx64, number);
  return string(buffer);
}

string HexString(int number) {
  char buffer[19];
  snprintf(buffer, sizeof(buffer), "0x%x", number);
  return string(buffer);
}

int ErrnoString(string *error_string) {
  assert(error_string);

  // strerror isn't necessarily thread-safe.  strerror_r would be preferrable,
  // but GNU libc uses a nonstandard strerror_r by default, which returns a
  // char* (rather than an int success indicator) and doesn't necessarily
  // use the supplied buffer.
  error_string->assign(strerror(errno));
  return errno;
}

}  // namespace google_breakpad
