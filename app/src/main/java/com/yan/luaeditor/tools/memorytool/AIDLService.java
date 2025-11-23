package com.yan.luaeditor.tools.memorytool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.ipc.RootService;
import com.topjohnwu.superuser.nio.FileSystemManager;
import com.yan.luaide.ILuaideMemoryTool;
import com.yan.luaide.KeyValuePair;
import com.yan.luaide.MemoryMapNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Demonstrate RootService using AIDL (daemon mode)
public class AIDLService extends RootService {

    static {
        // Only load the library when this class is loaded in a root process.
        // The classloader will load this class (and call this static block) in the non-root
        // process because we accessed it when constructing the Intent to send.
        // Add this check so we don't unnecessarily load native code that'll never be used.
        if (Process.myUid() == 0)
            System.loadLibrary("memoryTool");
    }

    // Demonstrate we can also run native code via JNI with RootServices
    native int getPid(String packageName);
    native int getValue(int pid, long address);
    native int searchNumberDWORD(int pid, int value);
    native void clearResults();
    native int getResultsCount();
    native void setValue(int pid, long address, int value);
    native Object[] getMemoryMonitoringResults();
    native int startMemoryMonitoring(String pkg);
    native Object searchMemory(String pkg,long value,int type,long start,long end);
    class TestIPC extends ILuaideMemoryTool.Stub {
        @Override
        public List<MemoryMapNode> getMap() throws RemoteException {
            return Collections.emptyList();
        }

        @Override
        public String[] getMemoryMonitoringResults() throws RemoteException {
            return (String[]) AIDLService.this.getMemoryMonitoringResults();
        }

        @Override
        public int startMemoryMonitoring(String pkg) throws RemoteException {
            return AIDLService.this.startMemoryMonitoring(pkg);
        }

        @Override
        public List<KeyValuePair> searchMemory(String pkg, long value, int valueSize, long startAddress, long endAddress) throws RemoteException {
            return null;
        }

        @Override
        public int getPid(String packageName) {
            return AIDLService.this.getPid(packageName);
        }
        @Override
        @SuppressLint("DefaultLocale")
        public byte[] readMaps(int pid) {
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(String.format("/proc/%d/maps", pid));
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    // 将读取的数据转换为字符串并输出
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                fileInputStream.close();
                byte[] bytes = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                return bytes;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public IBinder getFileSystemService() throws RemoteException {
            return null;
        }

        @Override
        public int getValue(int pid, long address) throws RemoteException {
            return AIDLService.this.getValue(pid,address);
        }

        @Override
        public int searchNumberDWORD(int pid, int value) throws RemoteException {
            return AIDLService.this.searchNumberDWORD(pid,value);
        }

        @Override
        public void clearResults() throws RemoteException {
            AIDLService.this.clearResults();
        }

        @Override
        public int getResultsCount() throws RemoteException {
            return AIDLService.this.getResultsCount();
        }

        @Override
        public void setValue(int pid, long address, int value) throws RemoteException {
            AIDLService.this.setValue(pid,address,value);
        }

        @Override
        public char getValueBYTE(int pid, long address) throws RemoteException {
            return 0;
        }

        @Override
        public long getValueQWORD(int pid, long address) throws RemoteException {
            return 0;
        }

        @Override
        public void searchNumberBYTE(int pid, char value) throws RemoteException {

        }

        @Override
        public void setValueBYTE(int pid, long address, char value) throws RemoteException {

        }

        @Override
        public void searchNumberQWORD(int pid, long value) throws RemoteException {

        }

        @Override
        public void setValueQWORD(int pid, long address, long value) throws RemoteException {

        }

        @Override
        public int getSearchResultBYTECount() throws RemoteException {
            return 0;
        }

        @Override
        public void clear_result_BYTE() throws RemoteException {

        }

        @Override
        public int getSearchResultQWORDCount() throws RemoteException {
            return 0;
        }

        @Override
        public void clear_result_QWORD() throws RemoteException {

        }

        @Override
        public int getGotoAddressCount() throws RemoteException {
            return 0;
        }

        @Override
        public void clearGotoAddressResults() throws RemoteException {

        }


    }

    private final String uuid = UUID.randomUUID().toString();

    @Override
    public void onCreate() {

    }

    @Override
    public void onRebind(@NonNull Intent intent) {
        // This callback will be called when we are reusing a previously started root process

    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        System.out.println(getPid("com.yan.luaide"));
        return new TestIPC();
    }

    @Override
    public boolean onUnbind(@NonNull Intent intent) {

        // Return true here so onRebind will be called
        return true;
    }

    @Override
    public void onDestroy() {

    }
}