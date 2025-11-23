package com.yan.luaeditor.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yan.luaeditor.adapter.SetListAdapter;
import com.yan.luaeditor.tools.ThemeSwitchHelper;
import com.yan.luaide.R;
import com.yan.luaide.databinding.GeneralSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class General_Settings extends AppCompatActivity {
    GeneralSettingsBinding binding;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    private SetListAdapter itemAdapter;
    private List<SetListAdapter.ItemModel> itemList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        updateTheme();


        ThemeSwitchHelper.installTransition(this);
        super.onCreate(savedInstanceState);
        binding=GeneralSettingsBinding.inflate(getLayoutInflater());
        materialToolbar=binding.activityGeneralSetToolbar;
        recyclerView=binding.generalSetList;
        final String[] item = new String[] {"蓝色", "阳光","靛蓝","鲜绿","鲜橙","深棕","Material You"};
        final String[] item2 = new String[] {"亮色", "暗色"};
        final String[] item3 = new String[] {"Luaide","Ylua","Darcula", "Eclipse","Github","NotepadXX","VS2019","Atom","GithubDark","IntelliJDark","Sublime","VimDark","WebStormDark"};
        SharedPreferences sps = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sps.edit();
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("常规设置");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, true);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.R.attr.colorSecondary);
            if (Build.VERSION.SDK_INT >= 34) {
                window.getInsetsController().setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
        View contentView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (!isKeyboardVisible) {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(),
                        statusBarHeight,
                        v.getPaddingRight(),
                        v.getPaddingBottom());
            }
            return insets;
        });
        itemList = new ArrayList<>();
        itemList.add(new SetListAdapter.ItemModel("主题","选择IDE主题"));
        itemList.add(new SetListAdapter.ItemModel("背景","选择IDE背景颜色"));
        itemList.add(new SetListAdapter.ItemModel("风格","选择编辑框颜色风格"));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new SetListAdapter(this, itemList);
        setContentView(binding.getRoot());
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setOnItemClickListener(new SetListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (itemList.get(position).getTitle()){
                    case "主题":
                        int choice = 2;
                        if ((sps.getInt("Scheme", -1)) != -1) {
                            choice = sps.getInt("Scheme", -1);
                        }
                        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(General_Settings.this);
                        alertDialogBuilder
                                .setTitle("请选择主题")
                                .setSingleChoiceItems(
                                        item,
                                        choice,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 处理选项点击事件
                                                ed.putInt("Scheme", which);
                                                ed.commit();
                                                dialog.dismiss();
                                            }
                                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    case "背景":
                        int choice2 = 0;
                        if ((sps.getInt("Background", -1)) != -1) {
                            choice2 = sps.getInt("Background", -1);
                        }
                        MaterialAlertDialogBuilder alertDialogBuilder2 = new MaterialAlertDialogBuilder(General_Settings.this);
                        alertDialogBuilder2
                                .setTitle("请选择背景色")
                                .setSingleChoiceItems(
                                        item2,
                                        choice2,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 处理选项点击事件
                                                ed.putInt("Background", which);
                                                ed.commit();
                                                dialog.dismiss();
                                            }
                                        });
                        AlertDialog alertDialog2 = alertDialogBuilder2.create();
                        alertDialog2.show();
                        break;
                    case "风格":
                        int choice3 = 0;
                        if ((sps.getInt("ColorScheme", -1)) != -1) {
                            choice3 = sps.getInt("ColorScheme", -1);
                        }
                        MaterialAlertDialogBuilder alertDialogBuilder3 = new MaterialAlertDialogBuilder(General_Settings.this);
                        alertDialogBuilder3
                                .setTitle("请选颜色风格")
                                .setSingleChoiceItems(
                                        item3,
                                        choice3,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 处理选项点击事件
                                                ed.putInt("ColorScheme", which);
                                                ed.commit();
                                                dialog.dismiss();
                                            }
                                        });
                        AlertDialog alertDialog3 = alertDialogBuilder3.create();
                        alertDialog3.show();
                        break;
                }
            }
        });
    }

    private void updateTheme(){
        SharedPreferences scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        int bg = scheme.getInt("Background", 0);
        int sc = scheme.getInt("Scheme", -1);
        int themeResId;
        if (bg == 0) {
            // Light theme
            if (sc == 6) {
                DynamicColors.applyToActivitiesIfAvailable(getApplication());
            } else {
                TypedArray lightThemes = getResources().obtainTypedArray(R.array.light_themes);
                themeResId = lightThemes.getResourceId(sc, R.style.app_theme);
                lightThemes.recycle();
                setTheme(themeResId);
            }
        } else {
            // Dark theme
            if (sc == 6) {
                DynamicColors.applyToActivitiesIfAvailable(getApplication());
            } else {
                TypedArray darkThemes = getResources().obtainTypedArray(R.array.dark_themes);
                themeResId = darkThemes.getResourceId(sc, R.style.app_theme);

                darkThemes.recycle();
                setTheme(themeResId);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
