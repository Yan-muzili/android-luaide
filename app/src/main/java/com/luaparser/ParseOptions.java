package com.luaparser;

import java.util.*;

/**
 * 解析选项配置 - 完整对应所有配置选项
 */
public class ParseOptions {
    private boolean unicodeName = false;
    private Set<String> nonstandardSymbols = new HashSet<>();
    private Map<String, String> special = new HashMap<>();
    private String version = "Lua 5.4";
    private boolean strict = false;
    private boolean allowGoto = true;
    private boolean allowContinue = false;
    
    public ParseOptions() {
        // 默认配置
    }
    
    // Unicode名称支持
    public boolean isUnicodeName() { return unicodeName; }
    public void setUnicodeName(boolean unicodeName) { this.unicodeName = unicodeName; }
    
    // 非标准符号
    public Set<String> getNonstandardSymbols() { return nonstandardSymbols; }
    public void addNonstandardSymbol(String symbol) { nonstandardSymbols.add(symbol); }
    public void removeNonstandardSymbol(String symbol) { nonstandardSymbols.remove(symbol); }
    public boolean hasNonstandardSymbol(String symbol) { return nonstandardSymbols.contains(symbol); }
    
    // 特殊映射
    public Map<String, String> getSpecial() { return special; }
    public void addSpecial(String name, String mapping) { special.put(name, mapping); }
    public void removeSpecial(String name) { special.remove(name); }
    public String getSpecial(String name) { return special.get(name); }
    
    // 版本
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    // 严格模式
    public boolean isStrict() { return strict; }
    public void setStrict(boolean strict) { this.strict = strict; }
    
    // Goto支持
    public boolean isAllowGoto() { return allowGoto; }
    public void setAllowGoto(boolean allowGoto) { this.allowGoto = allowGoto; }
    
    // Continue支持
    public boolean isAllowContinue() { return allowContinue; }
    public void setAllowContinue(boolean allowContinue) { this.allowContinue = allowContinue; }
    
    // 预设配置
    public static ParseOptions defaultOptions() {
        return new ParseOptions();
    }
    
    public static ParseOptions lua51Options() {
        ParseOptions options = new ParseOptions();
        options.setVersion("Lua 5.1");
        options.setAllowGoto(false);
        return options;
    }
    
    public static ParseOptions lua52Options() {
        ParseOptions options = new ParseOptions();
        options.setVersion("Lua 5.2");
        return options;
    }
    
    public static ParseOptions lua53Options() {
        ParseOptions options = new ParseOptions();
        options.setVersion("Lua 5.3");
        return options;
    }
    
    public static ParseOptions lua54Options() {
        ParseOptions options = new ParseOptions();
        options.setVersion("Lua 5.4");
        return options;
    }
    
    public static ParseOptions luaJitOptions() {
        ParseOptions options = new ParseOptions();
        options.setVersion("LuaJIT");
        options.addNonstandardSymbol("//");
        options.addNonstandardSymbol("continue");
        options.addNonstandardSymbol("!=");
        options.addNonstandardSymbol("&&");
        options.addNonstandardSymbol("||");
        options.addNonstandardSymbol("+=");
        options.addNonstandardSymbol("-=");
        options.addNonstandardSymbol("*=");
        options.addNonstandardSymbol("/=");
        options.addNonstandardSymbol("%=");
        options.addNonstandardSymbol("^=");
        options.addNonstandardSymbol("//=");
        options.addNonstandardSymbol("&=");
        options.addNonstandardSymbol("|=");
        options.addNonstandardSymbol("<<=");
        options.addNonstandardSymbol(">>=");
        options.setAllowContinue(true);
        return options;
    }
    
    @Override
    public String toString() {
        return String.format("ParseOptions{version='%s', unicodeName=%s, nonstandardSymbols=%s}", 
                           version, unicodeName, nonstandardSymbols);
    }
    
    @Override
    public ParseOptions clone() {
        ParseOptions cloned = new ParseOptions();
        cloned.unicodeName = this.unicodeName;
        cloned.nonstandardSymbols = new HashSet<>(this.nonstandardSymbols);
        cloned.special = new HashMap<>(this.special);
        cloned.version = this.version;
        cloned.strict = this.strict;
        cloned.allowGoto = this.allowGoto;
        cloned.allowContinue = this.allowContinue;
        return cloned;
    }
}
