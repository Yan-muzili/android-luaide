#include "tools.h"
#include "MemoryTools.h"
//#include "MemoryTools.h"
#include <sys/stat.h>


extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getPid(JNIEnv *env, jobject thiz, jstring package_name) {
	// TODO: implement getPid()
	const char *packagename = env->GetStringUTFChars(package_name, nullptr);
	int pid = getPid(packagename);

	return (jint) pid;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getValue(JNIEnv *env, jobject thiz, jint pid, jlong address) {
	// TODO: implement getValue()
	return getValueDWORD(address, pid);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_searchNumberDWORD(JNIEnv *env, jobject thiz, jint pid, jint value) {
	// TODO: implement searchNumberDWORD()
	searchNumberDWORD(pid, value);
	return getResultsCount();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_clearResults(JNIEnv *env, jobject thiz) {
	// TODO: implement clearResults()
	return clear_result();
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getResultsCount(JNIEnv *env, jobject thiz) {
	return getResultsCount();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_setValue(JNIEnv *env, jobject thiz, jint pid, jlong address,
										 jint value) {
	// TODO: implement setValue()
	return setValue(pid, address, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getSearchResultBYTECount(JNIEnv *env, jobject thiz) {
    return getSearchResultBYTECount();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_clear_result_BYTE(JNIEnv *env, jobject thiz) {
    clear_result_BYTE();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getSearchResultQWORDCount(JNIEnv *env, jobject thiz) {
    return getSearchResultQWORDCount();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_clear_result_QWORD(JNIEnv *env, jobject thiz) {
    clear_result_QWORD();
}
extern "C"
JNIEXPORT jchar JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getValueBYTE(JNIEnv *env, jobject thiz, jint pid, jlong address) {
    return (jchar)getValueBYTE((unsigned long)address, pid);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getValueQWORD(JNIEnv *env, jobject thiz, jint pid, jlong address) {
    return (jlong)getValueQWORD((unsigned long)address, pid);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_setValueBYTE(JNIEnv *env, jobject thiz, jint pid, jlong address, jchar value) {
    setValueBYTE(pid, (unsigned long)address, (char)value);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_setValueQWORD(JNIEnv *env, jobject thiz, jint pid, jlong address, jlong value) {
    setValueQWORD(pid, (unsigned long)address, (long long)value);
}
// 启动内存监控的 JNI 方法声明
extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_startMemoryMonitoring(JNIEnv *env, jobject thiz, jstring package_name) {
    const char *packagename = env->GetStringUTFChars(package_name, nullptr);
    if (monitor_running) {
        env->ReleaseStringUTFChars(package_name, packagename);
        return -1; // 监控已经在运行
    }
    monitor_running = 1;
    if (pthread_create(&monitor_thread, NULL, MemoryMonitorThread, (void *)packagename) != 0) {
        monitor_running = 0;
        env->ReleaseStringUTFChars(package_name, packagename);
        return -2; // 线程创建失败
    }
    env->ReleaseStringUTFChars(package_name, packagename);
    return 0; // 启动成功
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_stopMemoryMonitoring(JNIEnv *env, jobject thiz) {
    if (!monitor_running) {
        return -1; // 监控未运行
    }
    monitor_running = 0;
    pthread_join(monitor_thread, NULL);
    // 清空监控结果
    pthread_mutex_lock(&results_mutex);
    if (monitor_results != NULL) {
        PMAPS tmp = monitor_results;
        while (tmp != NULL) {
            PMAPS next = tmp->next;
            free(tmp);
            tmp = next;
        }
        monitor_results = NULL;
    }
    pthread_mutex_unlock(&results_mutex);
    return 0; // 停止成功
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_getMemoryMonitoringResults(JNIEnv *env, jobject thiz) {
    // 加锁保护监控结果
    pthread_mutex_lock(&results_mutex);
    PMAPS tmp = monitor_results;
    int count = 0;
    // 计算监控结果的数量
    while (tmp != NULL) {
        count++;
        tmp = tmp->next;
    }

    jobjectArray result = env->NewObjectArray(count, env->FindClass("java/lang/String"), nullptr);
    tmp = monitor_results;
    int index = 0;
    while (tmp != NULL) {
        char buffer[100];
        sprintf(buffer, "%lx-%lx", tmp->addr, tmp->taddr);
        jstring str = env->NewStringUTF(buffer);
        env->SetObjectArrayElement(result, index++, str);
        env->DeleteLocalRef(str);
        tmp = tmp->next;
    }
    // 解锁
    pthread_mutex_unlock(&results_mutex);
    return result;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_searchMemory(JNIEnv* env, jobject thiz, jstring pkg, jlong value, jint valueSize, jlong startAddress, jlong endAddress) {
    // 搜索内存
    PSearchResult results = searchMemoryForValue(reinterpret_cast<char *>(pkg), (unsigned long)value, (size_t)valueSize, (unsigned long)startAddress, (unsigned long)endAddress);
    if (!results) {
        return NULL;
    }

    // 创建 Java 的 HashMap
    jclass hashMapClass = env->FindClass("java/util/HashMap");
    jmethodID hashMapInit = env->GetMethodID(hashMapClass, "<init>", "()V");
    jobject hashMap = env->NewObject(hashMapClass, hashMapInit);

    // 获取 HashMap 的 put 方法
    jmethodID putMethod = env->GetMethodID(hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    // 类和方法 ID 用于创建 Long 对象
    jclass longClass = env->FindClass("java/lang/Long");
    jmethodID longInit = env->GetMethodID(longClass, "<init>", "(J)V");

    // 将搜索结果添加到 HashMap
    PSearchResult current = results;
    while (current) {
        // 创建地址和值的 Long 对象
        jobject addressObj = env->NewObject(longClass, longInit, (jlong)current->address);
        jobject valueObj = env->NewObject(longClass, longInit, (jlong)current->value);

        // 添加到 HashMap
        env->CallObjectMethod(hashMap, putMethod, addressObj, valueObj);

        // 释放局部引用
        env->DeleteLocalRef(addressObj);
        env->DeleteLocalRef(valueObj);

        current = current->next;
    }

    // 释放搜索结果内存
    while (results) {
        PSearchResult temp = results;
        results = results->next;
        free(temp);
    }

    return hashMap;
}
/*extern "C"
JNIEXPORT void JNICALL
Java_com_yan_luaeditor_tools_memorytool_AIDLService_gotoAddress(JNIEnv *env, jobject thiz, jint pid, jlong address) {
    gotoAddress(pid, (unsigned long)address);
}*/


