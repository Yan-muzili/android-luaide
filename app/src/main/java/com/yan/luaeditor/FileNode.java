package com.yan.luaeditor;

import com.yan.luaeditor.adapter.FileTreeAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileNode {
  private String name;
  private boolean isFolder;
  private boolean isExpanded;
  private List<FileNode> children;
  private FileNode parent;

  public FileNode(String name, boolean isFolder, boolean isExpanded) {
    this.name = name;
    this.isFolder = isFolder;
    this.isExpanded=isExpanded;
    this.children = new ArrayList<>();
  }

  private FileTreeAdapter childAdapter;

  public FileTreeAdapter getChildAdapter() {
    return childAdapter;
  }

  public void setChildAdapter(FileTreeAdapter childAdapter) {
    this.childAdapter = childAdapter;
  }

  public String getName() {
    return name;
  }

  public boolean isFolder() {
    return isFolder;
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setExpanded(boolean expanded) {
    isExpanded = expanded;
  }

  public List<FileNode> getChildren() {
    return children;
  }

  public void addChild(FileNode child) {
    child.parent = this;
    children.add(child);
  }

  public void addAllChildren(List<FileNode> childrenToAdd) {
    for (FileNode child : childrenToAdd) {
      child.parent = this;
      this.children.add(child);
    }
  }

  public void removeChild(FileNode child) {
    if (children.contains(child)) {
      child.parent = null;
      children.remove(child);
    }
  }

  public void removeAllChildren() {
    for (FileNode child : children) {
      child.parent = null;
      child.removeAllChildren();
    }
    children.clear();
  }

  public FileNode getParent() {
    return parent;
  }

  public boolean isRoot() {
    return parent == null;
  }

  public String getPath() {
    if (isRoot()) {
      return name;
    } else {
      List<String> pathParts = new ArrayList<>();
      FileNode currentNode = this;
      while (currentNode != null) {
        pathParts.add(0, currentNode.name);
        currentNode = currentNode.getParent();
      }
      return String.join("/", pathParts);
    }
  }
}
