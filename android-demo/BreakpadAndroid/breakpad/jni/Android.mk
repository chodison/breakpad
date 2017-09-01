# copyright (c) 2013-2017 ChodisonChen <c_soft_dev@163.com>
#

LOCAL_PATH := $(call my-dir)

MY_APP_JNI_ROOT := $(realpath $(LOCAL_PATH))

ifeq ($(IS_ENABLE_STATIC_LIB),true)
#libmybreakpad
include $(CLEAR_VARS)
LOCAL_MODULE := mybreakpad
LOCAL_SRC_FILES :=  \
					minidump_processor.cc \
					mybreakpad_jni.cc
					
LOCAL_C_INCLUDES        := $(MY_APP_JNI_ROOT)/src/common/android/include \
                           $(MY_APP_JNI_ROOT)/src \
                           $(MY_APP_JNI_ROOT)
LOCAL_LDLIBS := -llog -latomic

LOCAL_CFLAGS :=
#LOCAL_CFLAGS += -DLOGDEBUG

LOCAL_SHARED_LIBRARIES := 
LOCAL_STATIC_LIBRARIES := breakpad_processor breakpad_client
include $(BUILD_SHARED_LIBRARY)

include $(MY_APP_JNI_ROOT)/breakpad_client/Android.mk
include $(MY_APP_JNI_ROOT)/breakpad_processor/Android.mk
include $(MY_APP_JNI_ROOT)/breakpad_symbols/Android.mk
else
include $(call all-subdir-makefiles)
endif
