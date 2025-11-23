LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS := -Wno-pointer-to-int-cast -Wno-int-to-pointer-cast
LOCAL_CFLAGS += -std=c++23


LOCAL_MODULE     := memoryTool
LOCAL_SRC_FILES  := memoryTest.cpp tools.cpp
LOCAL_LDLIBS    += -lz
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog -ldl

include $(BUILD_SHARED_LIBRARY)