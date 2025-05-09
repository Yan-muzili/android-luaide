#include <stdio.h>
#include <lua.h>
#include <lauxlib.h>
#include <jni.h>
#include "luajava.h"
#include <android/log.h>
#include <android/looper.h>
#include <unistd.h>
#include <pthread.h>

int callbackFunc(int fd, int events, void *data) {
    return 0;
};

struct LuaData {
    lua_State *L;
};

static void *run(void *arg) {

}

static int loop(lua_State *L) {
    pthread_t thread;
    int pip_fd[2];
    pipe(pip_fd);
    ALooper *looper = ALooper_forThread();
    ALooper_addFd(looper, pip_fd[0], 0, ALOOPER_EVENT_INPUT, callbackFunc, L);
    pthread_create(&thread, NULL, run, L);
    return 1;
}

int luaopen_looper(lua_State *L) {
    luaL_Reg l[] = {
            {"loop", loop},
            {NULL, NULL},
    };

    luaL_newlib(L, l);
    return 1;
}
