//
// Created by 28608 on 2023/12/9.
//
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <unordered_map>

#define LOG_TAG "System.out"
#ifndef STUDY_MEMORYTEST_H
#define STUDY_MEMORYTEST_H


#define TYPE_DWORD 4

#define DEFAULT_BUFF_SIZE 1024


void android_log(const char *s);

void android_log(int i);

void android_log(double i);

void android_log(long i);

struct MemoryMapNode {
	unsigned long start;
	unsigned long end;
	char permissions[5];
	unsigned long offset;
	char device[12];
	int inode;
	char pathname[256];
	struct MemoryMapNode *next;
};

static
std::unordered_map<unsigned long, int> result_map;


int getPid(const char *pn);


int getSearchResultDWORDCount(std::unordered_map<unsigned long, int> *map);

void searchNumberDWORD(int pid, long value);
void searchNumberBYTE(int pid, char value);
void searchNumberQWORD(int pid, long long value);


MemoryMapNode *readMaps(int pid);

int getMapsCount(MemoryMapNode *memoryMapNode);


int getValueDWORD(unsigned long address, int pid);
char getValueBYTE(unsigned long address, int pid);
long long getValueQWORD(unsigned long address, int pid);


int openMem(int pid);
int getSearchResultBYTECount();
void clear_result_BYTE();
int getSearchResultQWORDCount();
void clear_result_QWORD();

int getResultsCount();
void clear_result();

void setValue(int pid, unsigned long address, int value);
void setValueBYTE(int pid, unsigned long address, char value);
void setValueQWORD(int pid, unsigned long address, long long value);

std::unordered_map<unsigned long, int> new_MAP_DWORD_RES();

#endif //STUDY_MEMORYTEST_H


