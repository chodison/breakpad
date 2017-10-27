# Copyright (c) 2012, Google Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
#     * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following disclaimer
# in the documentation and/or other materials provided with the
# distribution.
#     * Neither the name of Google Inc. nor the names of its
# contributors may be used to endorse or promote products derived from
# this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# ndk-build module definition for the Google Breakpad client library
#
# To use this file, do the following:
#
#   1/ Include this file from your own Android.mk, either directly
#      or with through the NDK's import-module function.
#
#   2/ Use the client static library in your project with:
#
#      LOCAL_STATIC_LIBRARIES += breakpad_client
#
#   3/ In your source code, include "src/client/linux/exception_handler.h"
#      and use the Linux instructions to use it.
#
# This module works with either the STLport or GNU libstdc++, but you need
# to select one in your Application.mk
#

# The top Google Breakpad directory.
# We assume this Android.mk to be under 'android/google_breakpad'

LOCAL_PATH := $(call my-dir)

# Defube the client library module, as a simple static library that
# exports the right include path / linker flags to its users.

include $(CLEAR_VARS)

LOCAL_MODULE := breakpad_symbols_static

LOCAL_CPP_EXTENSION := .cc

# Breakpad uses inline ARM assembly that requires the library
# to be built in ARM mode. Otherwise, the build will fail with
# cryptic assembler messages like:
#   Compile++ thumb  : google_breakpad_client <= crash_generation_client.cc
#   /tmp/cc8aMSoD.s: Assembler messages:
#   /tmp/cc8aMSoD.s:132: Error: invalid immediate: 288 is out of range
#   /tmp/cc8aMSoD.s:244: Error: invalid immediate: 296 is out of range
LOCAL_ARM_MODE := arm

# List of client source files, directly taken from Makefile.am
LOCAL_SRC_FILES 	+=\
	$(MY_APP_JNI_ROOT)/src/common/dwarf_cfi_to_module.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf_cu_to_module.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf_line_to_module.cc \
	$(MY_APP_JNI_ROOT)/src/common/language.cc \
	$(MY_APP_JNI_ROOT)/src/common/module.cc \
	$(MY_APP_JNI_ROOT)/src/common/stabs_reader.cc \
	$(MY_APP_JNI_ROOT)/src/common/stabs_to_module.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf/bytereader.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf/dwarf2diehandler.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf/dwarf2reader.cc \
	$(MY_APP_JNI_ROOT)/src/common/dwarf/elf_reader.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/crc32.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/dump_symbols.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/elf_symbols_to_module.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/elfutils.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/file_id.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/linux_libc_support.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/memory_mapped_file.cc \
	$(MY_APP_JNI_ROOT)/src/common/path_helper.cc \
	$(MY_APP_JNI_ROOT)/src/common/linux/safe_readlink.cc 

LOCAL_C_INCLUDES        := $(MY_APP_JNI_ROOT)/src/common/android/include \
                           $(MY_APP_JNI_ROOT)/src \
                           $(MY_APP_JNI_ROOT)

#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)
#LOCAL_EXPORT_LDLIBS     := -llog
LOCAL_LDLIBS             := -llog -latomic

#-fexceptions -frtti -fno-rtti
LOCAL_CPPFLAGS += -frtti

LOCAL_SHARED_LIBRARIES :=
LOCAL_STATIC_LIBRARIES :=
include $(BUILD_STATIC_LIBRARY)

##编译动态库
include $(CLEAR_VARS)
LOCAL_MODULE := breakpad_symbols

LOCAL_CPP_EXTENSION := .cc

LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES := dump_syms.cc
LOCAL_SRC_FILES += breakpad_symbols_jni.cc 

LOCAL_C_INCLUDES        := $(MY_APP_JNI_ROOT)/src/common/android/include \
                           $(MY_APP_JNI_ROOT)/src \
                           $(MY_APP_JNI_ROOT)
LOCAL_LDLIBS             := -llog -latomic

#-fexceptions -frtti -fno-rtti
LOCAL_CPPFLAGS += -frtti

LOCAL_SHARED_LIBRARIES :=
LOCAL_STATIC_LIBRARIES := breakpad_symbols_static
include $(BUILD_SHARED_LIBRARY)


#minidump_stackwalk exe file
include $(CLEAR_VARS)
LOCAL_MODULE := dump_syms
LOCAL_SRC_FILES := dump_syms_exe.cc 
LOCAL_C_INCLUDES        := $(MY_APP_JNI_ROOT)/src/common/android/include \
                           $(MY_APP_JNI_ROOT)/src \
                           $(MY_APP_JNI_ROOT)
LOCAL_LDLIBS             := -llog -latomic

LOCAL_SHARED_LIBRARIES := 
LOCAL_STATIC_LIBRARIES := breakpad_symbols_static
include $(BUILD_EXECUTABLE)

# Done.
