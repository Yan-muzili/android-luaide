package com.yan.luaeditor.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;
import com.yan.luaeditor.adapter.ToolboxListAdapter;
import com.yan.luaeditor.Editor;
import com.yan.luaeditor.ToolboxListItem;
import com.yan.luaide.LuaActivity;
import com.yan.luaide.R;
import com.yan.luaide.databinding.ToolboxFragmentBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ToolboxListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ToolboxListAdapter listAdapter;
    private List<ToolboxListItem> itemList;
    private ToolboxFragmentBinding binding;
    int[] id = {R.drawable.ic_colorpick,R.drawable.layouthelper};
    String[] title = {"调色","布局"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ToolboxFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = binding.recyclerView;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);

        itemList = new ArrayList<>();
        for (int i = 0; i < id.length; i++) itemList.add(new ToolboxListItem(title[i], id[i]));

        listAdapter = new ToolboxListAdapter(itemList);
        listAdapter.setOnItemClickListener(new ToolboxListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        ColorPickerDialog.Builder build = ColorPickerDialog.newBuilder();
                        build.setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                                .setColorShape(ColorShape.CIRCLE)
                                .setAllowPresets(true)
                                .setAllowCustom(true)
                                .setShowAlphaSlider(true)
                                .setShowColorShades(true);
                        ColorPickerDialog dialog = build.create();
                        dialog.show(getActivity().getSupportFragmentManager(), "colorPicker");
                        dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                            @Override
                            public void onColorSelected(int dialogId, int color) {

                            }

                            @Override
                            public void onDialogDismissed(int dialogId) {
                            }
                        });
                        break;
                    case 1:
                        Editor activity=(Editor) getActivity();
                        Intent intent1=new Intent(activity, LuaActivity.class);
                        intent1.setData(Uri.fromFile(new File(activity.getFilesDir().getAbsolutePath()+"/layouthelper/main.lua")));
                        intent1.putExtra("arg",new String[]{new File(activity.mdir).getParent()+"/",activity.fragments.get(activity.pager_choice).fileName});
                        startActivity(intent1);
                        break;
                }
            }
        });
        recyclerView.setAdapter(listAdapter);
        return view;
    }
}