package com.yan.luaeditor.tools.dep;

public interface IProgressUpdater {
    void updateProgress1(int percent);   // 当前文件字节进度
    void updateTotalProgress(String string,int finished, int total);
    void setTitle(String title);
    void setMessage(String msg);
    void dismiss();
}