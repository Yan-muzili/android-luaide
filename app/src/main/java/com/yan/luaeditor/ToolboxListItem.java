package com.yan.luaeditor;

public class ToolboxListItem {
    private String title;
    int id;

    public ToolboxListItem(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public int getId(){return id;}
}