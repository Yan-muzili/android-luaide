package com.yan.luaeditor.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yan.luaeditor.Editor;
import com.yan.luaeditor.FileNode;
import com.yan.luaeditor.adapter.FileTreeAdapter;
import com.yan.luaeditor.tools.YanDialog;
import com.yan.luaide.R;
import com.yan.luaide.databinding.FileTreeFragmentBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTreeFragment extends Fragment {
  private RecyclerView recyclerView; // 更名为 recyclerView 以符合使用
  private FileTreeFragmentBinding binding;
  private FileTreeAdapter adapter;
  private List<FileNode> fileNodes;
  private HorizontalScrollView scrollView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FileTreeFragmentBinding.inflate(inflater, container, false);
    View view = binding.getRoot(); // 使用 binding.getRoot() 获取根视图
    recyclerView = view.findViewById(R.id.treeview);
    scrollView=view.findViewById(R.id.scroll);
    return view;
  }

  public void setTrees(String path){
    fileNodes = new ArrayList<>();
    File rootDirectory = new File(path); // 替换为你的根目录
    fileNodes.add(new FileNode(rootDirectory.getAbsolutePath(), true,true));

    try {
      adapter = new FileTreeAdapter(getActivity(), fileNodes, (Editor) getActivity());
      if (adapter!= null) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
      } else {
        YanDialog.show(getContext(), "", "没有适配器");
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      //YanDialog.show(getContext(), "", "创建适配器时出现异常: " + e.getMessage());
    }

  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Editor editor=(Editor) getActivity();
if (editor.mdir!=null)
    setTrees(new File(editor.mdir).getParent());
  }

  /*File currentDirectory = new File("/sdcard/");

  public void setList() {
    final List<String> fileNames = new ArrayList<>();
    final List<String> filePaths = new ArrayList<>();
    listFiles(currentDirectory, fileNames, filePaths);
    // 将文件名和文件路径转换为File对象列表
    List<File> files = new ArrayList<>();
    for (String path : filePaths) {
      files.add(new File(path));
    }

    // 自定义比较器，确保文件夹在文件之前
    Comparator<File> customComparator =
            (file1, file2) -> {
              boolean isDir1 = file1.isDirectory();
              boolean isDir2 = file2.isDirectory();

              // 如果两个都是文件夹或都是文件，按名称排序
              if (isDir1 == isDir2) {
                return file1.getName().compareToIgnoreCase(file2.getName());
              }

              // 如果一个是文件夹，另一个是文件，则文件夹排在前面
              return isDir1 ? -1 : 1;
            };

    // 对File对象列表进行排序
    Collections.sort(files, customComparator);

    // 将排序后的File对象列表转回String列表
    List<String> sortedFileNames = new ArrayList<>();
    List<String> sortedFilePaths = new ArrayList<>();
    sortedFileNames.add("...");
    sortedFilePaths.add("...");
    for (File file : files) {
      sortedFileNames.add(file.getName());
      sortedFilePaths.add(file.getPath());
    }
    FileListAdapter adapter =
            new FileListAdapter(getActivity(), sortedFileNames, sortedFilePaths);
    adapter.setOnItemClickListener(
            new FileListAdapter.OnItemClickListener() {
              @Override
              public void onItemClick(String topText, int position) {
                if (position == 0) {
                  if (!currentDirectory.getPath().equals("/sdcard")) {
                    if (!currentDirectory.equals(Environment.getExternalStorageDirectory())) {
                      currentDirectory = currentDirectory.getParentFile();
                      setList();
                    }
                  }
                } else {
                  String selectedFileName = sortedFileNames.get(position);
                  String selectedFilePath = sortedFilePaths.get(position);
                  File selectedFile = new File(selectedFilePath);
                  if (selectedFile.isDirectory()) {
                    // 如果是文件夹，则进入文件夹
                    currentDirectory = selectedFile;
                    setList(); // 递归显示文件选择对话框
                  } else {
                    // 如果是文件，则处理文件的逻辑，例如打开文件等
                    ((Editor)getActivity()).addFileToUI(selectedFilePath);
                  }
                }
              }
            });
    //listview.setAdapter(adapter);
  }
  private void listFiles(File directory, List<String> fileNames, List<String> filePaths) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        fileNames.add(file.getName());
        filePaths.add(file.getPath());
      }
    }
  }*/
}