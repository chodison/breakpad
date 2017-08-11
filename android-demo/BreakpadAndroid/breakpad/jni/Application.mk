APP_ABI := armeabi armeabi-v7a
APP_STL := gnustl_static
APP_CPPFLAGS += -std=gnu++11 -fexceptions

ifeq ($(APP_DEBUG), true)
APP_CFLAGS += -g
endif