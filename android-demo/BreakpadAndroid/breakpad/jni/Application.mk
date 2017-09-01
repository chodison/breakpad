APP_OPTIM := release
APP_PLATFORM := android-14
APP_ABI := armeabi
NDK_TOOLCHAIN_VERSION=4.9
APP_STL := gnustl_static
APP_CPPFLAGS += -std=gnu++11 -fexceptions

ifeq ($(APP_DEBUG), true)
APP_CFLAGS += -g
endif
#
IS_ENABLE_STATIC_LIB := true