// ILuaideMemoryTool.aidl
package com.yan.luaide;
import com.yan.luaide.MemoryMapNode;
import com.yan.luaide.KeyValuePair;
// Declare any non-default types here with import statements
interface ILuaideMemoryTool {
    int getPid(String packageName);
    //    FileInputStream readMaps(int pid);
        byte[] readMaps(int pid);
        IBinder getFileSystemService();
        int getValue(int pid, long address);
        int searchNumberDWORD(int pid, int value);
        void clearResults();
        int getResultsCount();
        void setValue(int pid, long address, int value);

        char getValueBYTE(int pid, long address);
        long getValueQWORD(int pid, long address);
        void searchNumberBYTE(int pid, char value);
        void setValueBYTE(int pid, long address, char value);

        void searchNumberQWORD(int pid, long value);
        void setValueQWORD(int pid, long address, long value);

        int getSearchResultBYTECount();
        void clear_result_BYTE();

        int getSearchResultQWORDCount();
        void clear_result_QWORD();

        int getGotoAddressCount();
        void clearGotoAddressResults();
        List<MemoryMapNode> getMap();
        String[] getMemoryMonitoringResults();
        int startMemoryMonitoring(String pkg);
        List<KeyValuePair> searchMemory(String pkg, long value, int valueSize, long startAddress, long endAddress);
}