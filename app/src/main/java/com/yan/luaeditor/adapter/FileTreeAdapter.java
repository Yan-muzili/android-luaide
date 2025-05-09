package com.yan.luaeditor.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yan.luaeditor.Editor;
import com.yan.luaeditor.FileNode;
import com.yan.luaeditor.tools.YanDialog;
import com.yan.luaeditor.ui.ImageActivity;
import com.yan.luaide.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.ViewHolder> {
    private Context context;
    private List<FileNode> fileNodes;
    private OnFileClickListener listener;
    private Editor editor;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.listener = listener;
    }

    public FileTreeAdapter(Context context, List<FileNode> fileNodes, Editor editor) {
        this.context = context;
        this.fileNodes = fileNodes;
        this.editor = editor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_tree_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final FileNode fileNode = fileNodes.get(position);
        String[] na = fileNode.getName().split("/");
        if (na.length == 0) {
            holder.fileName.setText(fileNode.getName());
        } else {
            holder.fileName.setText(na[na.length - 1]);
        }

        if (fileNode.isFolder()) {
            holder.statusIcon.setImageResource(fileNode.isExpanded() ? R.drawable.down : R.drawable.right);
            holder.childRecyclerView.setVisibility(fileNode.isExpanded() ? View.VISIBLE : View.GONE);

            // 当文件夹初始展开且子节点为空时，加载子节点
            if (fileNode.isExpanded() && fileNode.getChildren().isEmpty()) {
                new Thread(() -> {
                    loadChildren(fileNode);
                    mainHandler.post(() -> {
                        notifyItemChanged(holder.getAdapterPosition());
                    });
                }).start();
            }

            FileTreeAdapter childAdapter = fileNode.getChildAdapter();
            if (childAdapter == null) {
                childAdapter = new FileTreeAdapter(context, fileNode.getChildren(), editor);
                fileNode.setChildAdapter(childAdapter);
            }
            holder.childRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.childRecyclerView.setAdapter(childAdapter);
        } else {
            holder.folderIcon.setImageResource(getFileTypeByExtension(holder.fileName.getText().toString()));
            holder.childRecyclerView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (fileNode.isFolder()) {
                fileNode.setExpanded(!fileNode.isExpanded());
                if (!fileNode.isExpanded()) {
                    // 当父文件夹收起时，将所有子文件夹也收起
                    collapseAllChildren(fileNode);
                }
                mainHandler.post(() -> {
                    notifyItemChanged(holder.getAdapterPosition());
                    holder.statusIcon.setImageResource(fileNode.isExpanded() ? R.drawable.down : R.drawable.right);
                });
            } else {
                if (getFileTypeByExtension(fileNode.getPath()) != R.drawable.ic_image) {
                    editor.addFileToUI(fileNode.getPath());
                } else {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("imagePath", fileNode.getPath());
                    context.startActivity(intent);
                }
            }
        });
    }

    private void loadChildren(FileNode fileNode) {
        try {
            File directory = new File(fileNode.getPath());
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null && files.length > 0) {
                    List<File> fileList = new ArrayList<>(Arrays.asList(files));
                    sortFiles(fileList);
                    for (File file : fileList) {
                        fileNode.addChild(new FileNode(file.getName(), file.isDirectory(), false));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            YanDialog.show(context, "FileTreeAdapter", "loadChildren 出现异常: " + e.getMessage());
        }
    }

    private void collapseAllChildren(FileNode parentNode) {
        for (FileNode child : parentNode.getChildren()) {
            if (child.isFolder()) {
                child.setExpanded(false);
                collapseAllChildren(child);
            }
        }
    }

    private void sortFiles(List<File> fileList) {
        fileList.sort(Comparator.comparing((File f) -> f.isDirectory() ? 0 : 1).thenComparing(File::getName));
    }

    public static int getFileTypeByExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex != -1 && lastIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastIndex + 1).toLowerCase();
            switch (extension) {
                case "lua":
                    return R.drawable.ic_lua;
                case "java":
                    return R.drawable.ic_java;
                case "png":
                case "jpeg":
                case "jpg":
                    return R.drawable.ic_image;
                case "xml":
                    return R.drawable.ic_xml;
                case "svg":
                    return R.drawable.ic_svg;
                case "gradle":
                    return R.drawable.ic_gradle;
                case "html":
                    return R.drawable.ic_html;
                case "js":
                    return R.drawable.ic_js;
                case "css":
                    return R.drawable.ic_css;
                case "json":
                    return R.drawable.ic_json;
                case "txt":
                    return R.drawable.ic_txt;
                case "aly":
                    return R.drawable.ic_aly;
                case "sh":
                    return R.drawable.ic_sh;
                case "py":
                    return R.drawable.ic_py;
                case "zip":
                case "rar":
                case "tar":
                    return R.drawable.ic_zip;
                default:
                    return R.drawable.ic_unknown;
            }
        }
        return R.drawable.ic_unknown;
    }

    @Override
    public int getItemCount() {
        return fileNodes.size();
    }

    public interface OnFileClickListener {
        void onFileClick(String path);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView folderIcon;
        ImageView statusIcon;
        RecyclerView childRecyclerView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            folderIcon = itemView.findViewById(R.id.folder_icon);
            statusIcon = itemView.findViewById(R.id.status);
            childRecyclerView = itemView.findViewById(R.id.child_recycler_view);
        }
    }
}