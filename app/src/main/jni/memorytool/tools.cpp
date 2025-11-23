#include <dirent.h>
#include "tools.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <android/log.h>
#include <unordered_map>

#define LOG_TAG "System.out"
#define TYPE_DWORD 4
#define DEFAULT_BUFF_SIZE 1024

// 日志输出函数
void android_log(const char *s) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s", s);
}

void android_log(int i) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%d", i);
}

void android_log(double i) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%f", i);
}

void android_log(long i) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%ld", i);
}

// 创建内存映射节点
MemoryMapNode *createMemoryMapNode(unsigned long start, unsigned long end, const char *permissions,
                                   unsigned long offset, const char *device, int inode,
                                   const char *pathname) {
    MemoryMapNode *newNode = (MemoryMapNode *) malloc(sizeof(MemoryMapNode));
    if (newNode == nullptr) {
        perror("Error allocating memory");
        exit(1);
    }

    newNode->start = start;
    newNode->end = end;
    strncpy(newNode->permissions, permissions, sizeof(newNode->permissions));
    newNode->offset = offset;
    strncpy(newNode->device, device, sizeof(newNode->device));
    newNode->inode = inode;
    strncpy(newNode->pathname, pathname, sizeof(newNode->pathname));
    newNode->next = nullptr;
    return newNode;
}

// 释放内存映射节点链表
void freeMemoryMapNodes(MemoryMapNode *head) {
    MemoryMapNode *tmp;
    while (head != nullptr) {
        tmp = head;
        head = head->next;
        free(tmp);
    }
}

// 读取进程的内存映射信息
MemoryMapNode *readMaps(int pid) {
    FILE *file;
    char path[64];          //路径
    sprintf(path, "/proc/%d/maps", pid);
    char line[256];
    file = fopen(path, "r");

    if (file == nullptr) {
        perror("Error opening file");
        return nullptr;
    }

    MemoryMapNode *head = nullptr;  // 头节点
    MemoryMapNode *current = nullptr;

    while (fgets(line, sizeof(line), file)) {
        unsigned long start, end;
        char permissions[5];
        unsigned long offset;
        char device[12];
        int inode;
        char pathname[256];

        sscanf(line, "%lx-%lx %4s %lx %11s %d %255s",
               &start, &end, permissions, &offset, device, &inode, pathname);

        // 创建新节点
        MemoryMapNode *newNode = createMemoryMapNode(start, end, permissions, offset, device, inode,
                                                     pathname);

        if (head == nullptr) {
            head = newNode;
            current = newNode;
        } else {
            current->next = newNode;
            current = newNode;
        }
    }

    fclose(file);

    return head;
}

// 打开进程的内存文件
int openMem(int pid) {
    char path[64];
    sprintf(path, "/proc/%d/mem", pid);
    int fd = open(path, O_RDWR);
    if (fd == -1) {
        perror("Error opening /proc/pid/mem");
    }
    return fd;
}

// 获取进程的PID
int getPid(const char *pn) {
    DIR *dir = nullptr;
    struct dirent *ptr = nullptr;
    FILE *fp = nullptr;
    char filepath[256];     // 大小随意，能装下cmdline文件的路径即可
    char filetext[128];     // 大小随意，能装下要识别的命令行文本即可
    dir = opendir("/proc"); // 打开路径
    if (nullptr != dir) {
        while ((ptr = readdir(dir)) != nullptr) // 循环读取路径下的每一个文件/文件夹
        {
            // 如果读取到的是"."或者".."则跳过，读取到的不是文件夹名字也跳过
            if ((strcmp(ptr->d_name, ".") == 0) || (strcmp(ptr->d_name, "..") == 0))
                continue;
            if (ptr->d_type != DT_DIR)
                continue;
            sprintf(filepath, "/proc/%s/cmdline", ptr->d_name); // 生成要读取的文件的路径
            fp = fopen(filepath, "r");                          // 打开文件
            if (nullptr != fp) {
                fgets(filetext, sizeof(filetext), fp); // 读取文件
                if (strcmp(filetext, pn) == 0) {
                    // puts(filepath);
                    // printf("packagename:%s\n",filetext);
                    break;
                }
                fclose(fp);
            }
        }
    }
    if (ptr == nullptr) {
        // puts("Get pid fail");
        return 0;
    }
    closedir(dir); // 关闭路径
    return atoi(ptr->d_name);
}

// 获取DWORD类型的值
int getValueDWORD(unsigned long address, int pid) {
    int fp = openMem(pid);
    if (fp == -1) {
        return 0;
    }
    int buff = 0;
    pread64(fp, &buff, sizeof(int), address);
    close(fp);
    return buff;
}

// 获取BYTE类型的值
char getValueBYTE(unsigned long address, int pid) {
    int fp = openMem(pid);
    if (fp == -1) {
        return 0;
    }
    char buff = 0;
    pread64(fp, &buff, sizeof(char), address);
    close(fp);
    return buff;
}

// 获取QWORD类型的值
long long getValueQWORD(unsigned long address, int pid) {
    int fp = openMem(pid);
    if (fp == -1) {
        return 0;
    }
    long long buff = 0;
    pread64(fp, &buff, sizeof(long long), address);
    close(fp);
    return buff;
}

// 获取内存映射节点的数量
int getMapsCount(MemoryMapNode *memoryMapNode) {
    MemoryMapNode *tmp = memoryMapNode;
    int count = 0;
    while (tmp != nullptr) {
        count++;
        tmp = tmp->next;
    }
    return count;
}

// 创建一个新的DWORD结果映射
std::unordered_map<unsigned long, int> new_MAP_DWORD_RES() {
    std::unordered_map<unsigned long, int> map;
    return map;
}

// 读取并过滤DWORD值
void readAndFilteringDWORDValue(unsigned long start, int readCount, int value,
                                std::unordered_map<unsigned long, int> *map, int fp) {
    int valueCount = readCount / 4;
    if (valueCount <= 0) {
        return;
    }
    int *buff = new int[valueCount];
    memset(buff, 0, sizeof(int) * valueCount);
    pread64(fp, buff, valueCount * 4, start);
    for (int i = 0; i < valueCount; ++i) {
        if (buff[i] == value) {
            unsigned long addr = start + i * 4;
            (*map)[addr] = value;
        }
    }
    delete[] buff;
}

// 搜索DWORD类型的数值
void searchNumberDWORD(int pid, long value) {
    if (getResultsCount() != 0) {
        for (auto it = result_map.begin(); it != result_map.end(); ) {
            unsigned long addr = it->first;
            int val = getValueDWORD(addr, pid);
            if (val != value) {
                it = result_map.erase(it);  // 删除当前键值对，并将迭代器指向下一个位置
            } else {
                ++it;  // 继续遍历下一个键值对
            }
        }
        return;
    }
    MemoryMapNode *head = readMaps(pid);
    int fp = openMem(pid);
    if (fp == -1) {
        freeMemoryMapNodes(head);
        return;
    }
    MemoryMapNode *tmp = head;

    while (tmp != nullptr) {
        unsigned long start = tmp->start;
        unsigned long anEnd = tmp->end;
        unsigned long range = anEnd - start;
        unsigned long readNum = range / DEFAULT_BUFF_SIZE;

        for (int i = 0; i < readNum; ++i) {
            readAndFilteringDWORDValue(start + i * DEFAULT_BUFF_SIZE, DEFAULT_BUFF_SIZE, (int)value,
                                       &result_map, fp);
        }

        tmp = tmp->next;
    }
    close(fp);
    freeMemoryMapNodes(head);
}

// 搜索BYTE类型的数值
void searchNumberBYTE(int pid, char value) {
    if (getResultsCount() != 0) {
        for (auto it = result_map.begin(); it != result_map.end(); ) {
            unsigned long addr = it->first;
            char val = getValueBYTE(addr, pid);
            if (val != value) {
                it = result_map.erase(it);
            } else {
                ++it;
            }
        }
        return;
    }
    MemoryMapNode *head = readMaps(pid);
    int fp = openMem(pid);
    if (fp == -1) {
        freeMemoryMapNodes(head);
        return;
    }
    MemoryMapNode *tmp = head;
    while (tmp != nullptr) {
        unsigned long start = tmp->start;
        unsigned long anEnd = tmp->end;
        unsigned long range = anEnd - start;
        unsigned long readNum = range / DEFAULT_BUFF_SIZE;
        for (int i = 0; i < readNum; ++i) {
            char buff[DEFAULT_BUFF_SIZE];
            memset(buff, 0, sizeof(buff));
            pread64(fp, buff, sizeof(buff), start + i * DEFAULT_BUFF_SIZE);
            for (int j = 0; j < DEFAULT_BUFF_SIZE; ++j) {
                if (buff[j] == value) {
                    unsigned long addr = start + i * DEFAULT_BUFF_SIZE + j;
                    result_map[addr] = (int)value;
                }
            }
        }
        tmp = tmp->next;
    }
    close(fp);
    freeMemoryMapNodes(head);
}

// 搜索QWORD类型的数值
void searchNumberQWORD(int pid, long long value) {
    if (getResultsCount() != 0) {
        for (auto it = result_map.begin(); it != result_map.end(); ) {
            unsigned long addr = it->first;
            long long val = getValueQWORD(addr, pid);
            if (val != value) {
                it = result_map.erase(it);
            } else {
                ++it;
            }
        }
        return;
    }
    MemoryMapNode *head = readMaps(pid);
    int fp = openMem(pid);
    if (fp == -1) {
        freeMemoryMapNodes(head);
        return;
    }
    MemoryMapNode *tmp = head;
    while (tmp != nullptr) {
        unsigned long start = tmp->start;
        unsigned long anEnd = tmp->end;
        unsigned long range = anEnd - start;
        unsigned long readNum = range / DEFAULT_BUFF_SIZE;
        for (int i = 0; i < readNum; ++i) {
            long long *buff = new long long[DEFAULT_BUFF_SIZE / sizeof(long long)];
            memset(buff, 0, sizeof(long long) * (DEFAULT_BUFF_SIZE / sizeof(long long)));
            pread64(fp, buff, sizeof(long long) * (DEFAULT_BUFF_SIZE / sizeof(long long)), start + i * DEFAULT_BUFF_SIZE);
            for (int j = 0; j < DEFAULT_BUFF_SIZE / sizeof(long long); ++j) {
                if (buff[j] == value) {
                    unsigned long addr = start + i * DEFAULT_BUFF_SIZE + j * sizeof(long long);
                    result_map[addr] = (int)value;
                }
            }
            delete[] buff;
        }
        tmp = tmp->next;
    }
    close(fp);
    freeMemoryMapNodes(head);
}

// 获取DWORD搜索结果的数量
int getSearchResultDWORDCount(std::unordered_map<unsigned long, int> *map) {
    int count = 0;
    for (const auto &pair: (*map)) {
        count++;
    }
    return count;
}

// 获取所有搜索结果的数量
int getResultsCount() {
    return getSearchResultDWORDCount(&result_map);
}

// 用于存储BYTE类型搜索结果的unordered_map
std::unordered_map<unsigned long, char> result_map_BYTE;

// 用于存储QWORD类型搜索结果的unordered_map
std::unordered_map<unsigned long, long long> result_map_QWORD;

// 获取BYTE搜索结果的数量
int getSearchResultBYTECount() {
    return result_map_BYTE.size();
}

// 清除BYTE搜索结果
void clear_result_BYTE() {
    result_map_BYTE.clear();
}

// 获取QWORD搜索结果的数量
int getSearchResultQWORDCount() {
    return result_map_QWORD.size();
}

// 清除QWORD搜索结果
void clear_result_QWORD() {
    result_map_QWORD.clear();
}

// 清除所有搜索结果
void clear_result() {
    result_map = new_MAP_DWORD_RES();
}

// 设置DWORD类型的值
void setValue(int pid, unsigned long address, int value) {
    int fp = openMem(pid);
    if (fp != -1) {
        pwrite64(fp, &value, sizeof(int), address);
        close(fp);
    }
}

// 设置BYTE类型的值
void setValueBYTE(int pid, unsigned long address, char value) {
    int fp = openMem(pid);
    if (fp != -1) {
        pwrite64(fp, &value, sizeof(char), address);
        close(fp);
    }
}

// 设置QWORD类型的值
void setValueQWORD(int pid, unsigned long address, long long value) {
    int fp = openMem(pid);
    if (fp != -1) {
        pwrite64(fp, &value, sizeof(long long), address);
        close(fp);
    }
}