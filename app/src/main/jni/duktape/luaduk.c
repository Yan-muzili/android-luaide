#include <lua.h>
#include <lauxlib.h>

#include <time.h>

#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <dirent.h>
#include <sys/stat.h>
#include "duktape.h"


static void duk_meta(lua_State *L);
static int push_duk_context(lua_State * L, duk_context* p);


static duk_ret_t native_print(duk_context *ctx) {
	duk_push_string(ctx, " ");
	duk_insert(ctx, 0);
	duk_join(ctx, duk_get_top(ctx) - 1);
    const char *s = duk_safe_to_string(ctx, -1);
	printf("%s\n", s);
	lua_State *L = duk_get_lua_state(ctx);
	lua_getglobal(L,"print");
	lua_pushstring(L,s);
	lua_call(L,1,0);
	return 0;
}

//strcmp memcmp
static int eval(lua_State *L) {
	duk_context *ctx = duk_create_heap_default();

	duk_push_lua_state(ctx, L);

	duk_push_c_function(ctx, native_print, DUK_VARARGS);
	duk_put_global_string(ctx, "print");

	duk_eval_string(ctx, luaL_checkstring(L,1));

	duk_pop(ctx);  /* pop eval result */

	duk_destroy_heap(ctx);
	return 0;
}


int push_duk_context(lua_State * L, duk_context* p)
{
    duk_context **userData;
	userData = (duk_context **) lua_newuserdata(L, sizeof(p));
	*userData = p;
	duk_meta(L);
	return 1;
}

void duk_meta(lua_State *L) {
	if (luaL_newmetatable(L, "dir")) {
		luaL_Reg l[] = {
			//\{ "close", duk_close },
			{ NULL, NULL },
		};
		luaL_newlib(L,l);
		lua_setfield(L, -2, "__index");
	}
	lua_setmetatable(L, -2);
}

int
luaopen_duk(lua_State *L) {
	luaL_checkversion(L);
	
	luaL_Reg l[] = {
		{ "eval", eval },
		{ NULL,  NULL },
	};

	luaL_newlib(L,l);
	
	return 1;
}

