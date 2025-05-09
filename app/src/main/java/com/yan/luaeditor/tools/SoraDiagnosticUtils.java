package com.yan.luaeditor.tools;

import java.util.List;

import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.lang.diagnostic.Quickfix;

public class SoraDiagnosticUtils {
    /**
     * 添加诊断信息到编辑器
     * @param line 行号
     * @param column 列位置
     * @param message 提示信息
     * @param quickfixes 解决方案列表
     */
    public static void addDiagnostic(DiagnosticsContainer diagnostics, int line, int column,String title, String message, List<Quickfix> quickfixes) {
        DiagnosticRegion region = new DiagnosticRegion(line, column, DiagnosticRegion.SEVERITY_ERROR, DiagnosticRegion.SEVERITY_ERROR, new DiagnosticDetail(title, message, null,null));
        diagnostics.addDiagnostic(region);
    }
}
