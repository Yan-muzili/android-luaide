package com.yan.luaeditor.tools.memorytool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.topjohnwu.superuser.ipc.RootService;
import com.topjohnwu.superuser.nio.FileSystemManager;
import com.yan.luaeditor.ui.ActivitySet;
import com.yan.luaide.ILuaideMemoryTool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryTool {
    private static final String TAG = "MemoryTool";
    private static MemoryTool instance;
    private ILuaideMemoryTool ipcService;
    private final AtomicBoolean isBound = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();




    // 绑定服务
    public void bindService(Context context) {
        Intent intent = new Intent(context, AIDLService.class);
        RootService.bind(intent, new AIDLConnection(false));
    }

    // 解绑服务
    public void unbindService() {
        if (isBound.get()) {
            RootService.unbind(aidlConn);
            isBound.set(false);
            ipcService = null;
            remoteFS = null;
            executorService.shutdown();
            Log.d(TAG, "服务已解绑，线程池已关闭");
        }
    }

    // 检查服务是否绑定
    public boolean isServiceBound() {
        return isBound.get() && ipcService != null;
    }

    // 获取进程 PID
    public int getProcessPid(String packageName) {
        try {
            if (ipcService!=null)
                return ipcService.getPid(packageName);
            else
                return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "获取 PID 失败", e);
            return -1;
        }
    }

    // 搜索内存（DWORD类型）
    public void searchMemory(int pid, int targetValue, OnSearchListener listener) {
        executeTask(() -> {
            try {
                ipcService.searchNumberDWORD(pid, targetValue);
                int count = ipcService.getResultsCount();
                listener.onSuccess(count);
            } catch (RemoteException e) {
                Log.e(TAG, "内存搜索失败", e);
                listener.onFailure(e.getMessage());
            }
        });
    }

    // 修改内存值
    public void modifyMemory(int pid, long address, int value, OnModifyListener listener) {
        executeTask(() -> {
            if (!isServiceBound()) {
                listener.onFailure("服务未绑定");
                return;
            }
            try {
                ipcService.setValue(pid, address, value);
                listener.onSuccess();
            } catch (RemoteException e) {
                Log.e(TAG, "内存修改失败", e);
                listener.onFailure(e.getMessage());
            }
        });
    }

    // 清除搜索结果
    public void clearSearchResults(OnClearListener listener) {
        executeTask(() -> {
            if (!isServiceBound()) {
                listener.onFailure("服务未绑定");
                return;
            }
            try {
                ipcService.clearResults();
                listener.onSuccess();
            } catch (RemoteException e) {
                Log.e(TAG, "清除结果失败", e);
                listener.onFailure(e.getMessage());
            }
        });
    }

    // 获取文件系统管理器（可选）
    public FileSystemManager getRemoteFS() {
        return remoteFS;
    }

    // 执行异步任务
    private void executeTask(Runnable task) {
        executorService.submit(task);
    }

    // 服务连接回调
    private AIDLConnection aidlConn;
    private AIDLConnection daemonConn;
    private FileSystemManager remoteFS;

    class AIDLConnection implements ServiceConnection {

        private final boolean isDaemon;

        AIDLConnection(boolean b) {
            isDaemon = b;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            if (isDaemon) {
                daemonConn = this;
            } else {
                aidlConn = this;
            }

            ipcService = ILuaideMemoryTool.Stub.asInterface(service);
            try {
                int pid = ipcService.getPid("com.yan.luaide");
                System.out.println("AIDL PID : " + pid);

            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            if (isDaemon) {
                daemonConn = null;
            } else {
                aidlConn = null;
                remoteFS = null;
            }
        }
    }

    // 搜索结果回调接口
    public interface OnSearchListener {
        void onSuccess(int resultCount);

        void onFailure(String message);
    }

    // 修改结果回调接口
    public interface OnModifyListener {
        void onSuccess();

        void onFailure(String message);
    }

    // 清除结果回调接口
    public interface OnClearListener {
        void onSuccess();

        void onFailure(String message);
    }
}