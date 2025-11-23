package com.yan.luaeditor;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.FileProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;
import com.yan.luaeditor.tools.AndroidBug5497Workaround;
import com.yan.luaeditor.tools.ClassMethodScanner;
import com.yan.luaeditor.tools.CompleteHashmapUtils;
import com.yan.luaeditor.tools.DrawableUtil;
import com.yan.luaeditor.tools.PackageUtil;
import com.yan.luaeditor.tools.ThemeSwitchHelper;
import com.yan.luaeditor.tools.ThreadManager;
import com.yan.luaeditor.tools.YanDialog;
import com.yan.luaeditor.format.AutoIndent;
import com.yan.luaeditor.tools.YanToast;
import com.yan.luaeditor.tools.apk.ApkTools;
import com.yan.luaeditor.tools.dep.Material3ProgressDialog;
import com.yan.luaeditor.tools.dep.MavenDownloader;
import com.yan.luaeditor.tools.dep.PomDownloader;
import com.yan.luaeditor.tools.memorytool.MemoryTool;
import com.yan.luaeditor.ui.ActivitySet;
import com.yan.luaeditor.ui.FileTreeFragment;
import com.yan.luaeditor.ui.MemoryTest;
import com.yan.luaeditor.ui.ToolboxListFragment;
import com.yan.luaeditor.vtl.VNodeUtils;
import com.yan.luaeditor.vtl.Vtl2View;
import com.yan.luaeditor.vtl.VtlLexer;
import com.yan.luaeditor.vtl.VtlParser;
import com.yan.luaide.LuaActivity;
import com.yan.luaide.LuaUtil;
import com.yan.luaide.R;
import com.yan.luaide.databinding.EditorActivityBinding;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SelectionMovement;
import lide.luaj.vm2.Globals;
import lide.luaj.vm2.LuaTable;
import lide.luaj.vm2.lib.jse.JsePlatform;
import lide.luaj.vm2.LuaValue;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Editor extends AppCompatActivity implements View.OnClickListener {
    /**
     * 初始化变量名，这里设置全局变量名
     */
    private EditorActivityBinding binding;
    TextView undo, redo, run, menu;
    LinearLayout drawerLeft;
    public DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FileTreeFragment filetree = new FileTreeFragment();
    ToolboxListFragment toolboxListFragment = new ToolboxListFragment();
    NavigationView NavigationView;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MaterialToolbar toolbar;
    LinearProgressIndicator progressIndicator;
    public int pager_choice;
    List<String> AllPath = new ArrayList<>();
    public final ArrayList<FileContentFragment> fragments = new ArrayList<>();
    DrawableUtil dbu = new DrawableUtil();
    public String mdir;
    private PopupMenu popo;
    boolean isbin = false;
    MemoryTool memoryTool = new MemoryTool();

    SharedPreferences sps;
    SharedPreferences.Editor ed;
    HashMap<String, Object> base;
    HashMap<String, List<String>> classMap2;
    int bg, sc = 0;
    private void updateTheme(){
        SharedPreferences scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        int bg = scheme.getInt("Background", 0);
        int sc = scheme.getInt("Scheme", -1);
        int themeResId = 0;
        if (bg == 0) {
            // Light theme
            if (sc == 6) {
                DynamicColors.applyToActivitiesIfAvailable(getApplication());
            } else {
                TypedArray lightThemes = getResources().obtainTypedArray(R.array.light_themes);
                if (sc >= 0) {
                    themeResId = lightThemes.getResourceId(sc, R.style.app_theme);
                }
                lightThemes.recycle();
                setTheme(themeResId);
            }
        } else {
            // Dark theme
            if (sc == 6) {
                DynamicColors.applyToActivitiesIfAvailable(getApplication());
            } else {
                TypedArray darkThemes = getResources().obtainTypedArray(R.array.dark_themes);
                if (sc >= 0) {
                    themeResId = darkThemes.getResourceId(sc, R.style.app_theme);
                }

                darkThemes.recycle();
                setTheme(themeResId);
            }
        }
    }
    /**
     * --------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        updateTheme();


        ThemeSwitchHelper.installTransition(this);
        super.onCreate(savedInstanceState);
        binding = EditorActivityBinding.inflate(getLayoutInflater());
        requestStoragePermissions();
        try {
            if (!new File(Environment.getExternalStorageDirectory() + "/Luaide/").exists() || !new File("/storage/emulated/0/Luaide/Projects").exists() ||
                    !new File(Environment.getExternalStorageDirectory() + "/Luaide/Manifest").exists() ||
                    !new File(Environment.getExternalStorageDirectory() + "/Luaide/bin").exists()) {
                new File(Environment.getExternalStorageDirectory() + "/Luaide/").mkdirs();
                new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects").mkdirs();
                new File(Environment.getExternalStorageDirectory() + "/Luaide/Manifest").mkdirs();
                new File(Environment.getExternalStorageDirectory() + "/Luaide/bin").mkdirs();
                if (new File(Environment.getExternalStorageDirectory() + "/Luaide/").isDirectory()
                        && new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects").isDirectory()
                        && new File(Environment.getExternalStorageDirectory() + "/Luaide/Manifest").isDirectory() && new File("/storage/emulated/0/Luaide/bin").isDirectory()) {
                    Toast.makeText(Editor.this, "文件夹创建成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Editor.this, "文件夹创建失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            YanDialog.show(this, "", e.getMessage());
        }

        setContentView(binding.getRoot());
        sps = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        ed = sps.edit();
        setTitle("暂无项目");
        Intent intent = getIntent();
        mdir = intent.getStringExtra("mdir");
        //YanDialog.show(this,"",mdir);
        //mdir="/storage/emulated/0/Luaide/Projects/yan/main.lua";
        initView();
        initClick();
        try {
            if (!new File(getFilesDir().getAbsolutePath() + "/complete.base").exists() || !new File(getFilesDir().getAbsolutePath() + "/complete2.base").exists()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("提示")
                        .setMessage("是否初始化Luaide的代码补全")
                        .setPositiveButton("确定", (dialog, which) -> {

                            new Thread(() -> {

                                ThreadManager.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressIndicator.setVisibility(View.VISIBLE);
                                    }
                                });
                                //MakeCompleteTree.scanClassesAndMethods(activity);
                                try {

                                    InitCompletion initCompletion = new InitCompletion(Editor.this);
                                    classMap2 = initCompletion.getCM();
                                    base = new InitCompletion(Editor.this).getClassTree(initCompletion.getClassNameList(classMap2));
                                    LuaUtil.save2("/sdcard/Luaide/yyy.log", classMap2.toString());
                                    CompleteHashmapUtils.saveHashMapToFile(Editor.this, Editor.this.base, "complete.base");
                                    CompleteHashmapUtils.saveHashMapToFile2(Editor.this, Editor.this.classMap2, "complete2.base");
                                } catch (Exception e) {
                                    ThreadManager.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            YanDialog.show(Editor.this, "Error", e.getMessage());
                                        }
                                    });

                                    //System.out.println(e.getMessage());
                                } finally {


                                }
                                ThreadManager.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ThreadManager.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressIndicator.setVisibility(View.GONE);
                                                for (int i = 0; i < fragments.size(); i++) {
                                                    removeTabAndFragment(i);
                                                }
                                                Editor.this.recreate();
                                            }
                                        });
                                    }
                                });
                            }).start();
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();

            } else {
                base = CompleteHashmapUtils.loadHashMapFromFile(Editor.this, "complete.base");
                classMap2 = CompleteHashmapUtils.loadHashMapFromFile2(Editor.this, "complete2.base");
                //memoryTool.bindService(Editor.this);
                //progressIndicator.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void run() {
                        ThreadManager.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisibility(View.VISIBLE);
                            }

                        });

                        //System.out.println(mdir+"/libs");
                        if (mdir != null) {
                            if (new File(new File(mdir).getParent() + "/libs").exists()) {
                                File[] libs = new File(new File(mdir).getParent() + "/libs").listFiles();
                                //System.out.println(libs.toString());
                                /*for (File f : libs) {
                                    List<String> dex = ClassMethodScanner.getClassNames(f.getAbsolutePath());
                                    for (String str : dex) {
                                        //System.out.println(str);
                                        String[] l = str.split("\\.");
                                        if (l.length > 0) {
                                            if (classMap2.get(l[l.length - 1].replaceAll("\\$", "\\.")) == null) {
                                                classMap2.put(l[l.length - 1].replaceAll("\\$", "\\."),
                                                        new ArrayList<>(Arrays.asList(str.replaceAll("\\$", "\\."))));
                                            } else {
                                                try {
                                                    if (!classMap2.get(l[l.length - 1].replaceAll("\\$", "\\.")).contains(str.replaceAll("\\$", "\\.")))
                                                        classMap2.get(l[l.length - 1].replaceAll("\\$", "\\.")).add(str.replaceAll("\\$", "\\."));

                                                } catch (Exception e) {
                                                    System.out.println(e.getMessage());
                                                }
                                            }
                                        }
                                    }
                                    HashMap<String, HashMap<String, CompletionName>> class2 = new ClassMethodScanner().scanClassesAndMethods(dex, f.getAbsolutePath());
                                    //LuaUtil.save2("/sdcard/Luaide/yyy.log",base.toString());
                                    base.putAll(class2);


                                }*/
                            }
                        }

                        ThreadManager.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();

                //LuaUtil.save2("/sdcard/Luaide/yyy.log",ClassMethodScanner.getClassNames("/sdcard/Luaide/classes.dex").toString());

            }
        } catch (RuntimeException e) {
            System.out.println(e);
        }
        setSupportActionBar(toolbar);
        if (mdir != null) {
            ed.putString("OpenFile", mdir);
            ed.commit();
            int lastIndex = mdir.lastIndexOf('.');
            if (lastIndex != -1 && lastIndex < mdir.length() - 1) {
                String extension = mdir.substring(lastIndex + 1).toLowerCase();
                if (!extension.equals("png") || !extension.equals("jpg") || !extension.equals("jpeg")) {
                    addFileToUI(mdir);
                    toolbar.setSubtitle(new File(mdir).getName());
                }
            }
            String[] na = new File(mdir).getParent().split("/");
            filetree.setTrees(new File(mdir).getParent());
            setTitle(na[na.length - 1]);
            dbu.setDrawableColor(run, 0xff53b457);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, true);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.R.attr.colorSecondary);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            if (Build.VERSION.SDK_INT >= 34) {
                window.getInsetsController().setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
                AndroidBug5497Workaround.assistActivity(this);
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
        //SoftHideKeyBoardUtil.assistActivity(this);
        //System.out.println(Shell.isAppGrantedRoot());


        setSupportActionBar(binding.toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, 0, 0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                View mainContent = binding.octa;
                int width = drawerView.getWidth();
                mainContent.setTranslationX(slideOffset * width);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager_choice = tab.getPosition();
                toolbar.setSubtitle(tab.getText());
                ed.putString("OpenFile", fragments.get(pager_choice).fileName);
                ed.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Toast.makeText(MainActivity.this, tab.getText().toString(),
                // Toast.LENGTH_SHORT).show();
                // removeTabAndFragment(tab.getText().toString());
                PopupMenu pop = new PopupMenu(Editor.this, tab.view);
                Menu men = pop.getMenu();
                men.add(0, 0, 0, "关闭当前");
                men.add(0, 1, 0, "关闭其他");
                men.add(0, 2, 0, "关闭所有");
                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        int id = arg0.getItemId();
                        switch (id) {
                            case 0:
                                removeTabAndFragment(pager_choice);
                                break;
                        }
                        return false;
                    }
                });
                pop.show();
            }
        });

        popo = new PopupMenu(this, menu);
        Menu men = popo.getMenu();
        SharedPreferences sps = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        men.add(0, 0, 0, "打包");
        SubMenu func = men.addSubMenu(0, 1, 0, "功能");
        SubMenu sign = men.addSubMenu(0, 2, 0, "光标");
        SubMenu project = men.addSubMenu(0, 3, 0, "项目");
        func.add(1, 0, 0, "自动换行").setCheckable(true).setChecked(sps.getBoolean("Wordwarp", true));
        func.add(1, 1, 0, "显示行号").setCheckable(true).setChecked(sps.getBoolean("linenumber", true));
        func.add(1, 2, 0, "固定行号").setCheckable(true).setChecked(sps.getBoolean("pin", false));
        func.add(1, 3, 0, "格式化代码").setIcon(R.drawable.format);
        func.add(1, 4, 0, "搜索文本");
        func.add(1, 5, 0, "parsertest");
        sign.add(2, 0, 0, "移到最后");
        sign.add(2, 1, 0, "左移");
        sign.add(2, 2, 0, "右移");
        sign.add(2, 3, 0, "上移");
        sign.add(2, 4, 0, "下移");
        sign.add(2, 5, 0, "行首");
        sign.add(2, 6, 0, "行末");
        project.add(3, 0, 0, "打开");
        project.add(3, 1, 0, "新建");
        project.add(3, 2, 0, "保存");
        func.setHeaderTitle("功能");
        sign.setHeaderTitle("光标");
        project.setHeaderTitle("项目");
        popo.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                SharedPreferences.Editor ed = sps.edit();
                int gro = arg0.getGroupId();
                switch (gro) {
                    case 0:
                        switch (arg0.getItemId()) {
                            case 0:
                                if (mdir != null) {

                                    ThreadManager.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressIndicator.setVisibility(View.VISIBLE);
                                            run.setBackgroundResource(R.drawable.ic_stop_daemons);
                                            //run.setEnabled(false);
                                        }
                                    });
                                    try {

                                        //startActivity(new Intent(this, LuaActivity.class).setData(Uri.fromFile(new File(new File(fragments.get(pager_choice).fileName).getPath()))));
                                        isbin = true;
                                        //new LuaActivity().doAsset("bin.lua");
                                        for (int i = 0; i <= fragments.size() - 1; i++) {
                                            LuaUtil.save2(AllPath.get(i), fragments.get(i).edit.getText().toString());
                                        }
                                        Globals g = JsePlatform.standardGlobals();
                                        g.loadfile(new File(mdir).getParent() + "/init.lua").call();
                                        LuaValue env = g.checkglobals();
                                        String appName = "demo";
                                        String verName = "1.0";
                                        String verCode = "1";
                                        String pkgName = "com.yan.test";
                                        LuaValue value = env.get("appname");
                                        if (!value.isstring()) value = env.get("app_name");
                                        if (value.isstring()) appName = value.tojstring();

                                        value = env.get("appver");
                                        if (!value.isstring()) value = env.get("app_ver");
                                        if (!value.isstring()) value = env.get("ver_name");
                                        if (value.isstring()) verName = value.tojstring();

                                        value = env.get("appcode");
                                        if (!value.isstring()) value = env.get("app_code");
                                        if (!value.isstring()) value = env.get("ver_code");
                                        if (value.isstring()) verCode = value.tojstring();

                                        value = env.get("packagename");
                                        if (!value.isstring()) value = env.get("package_name");
                                        if (value.isstring()) pkgName = value.tojstring();

                                        String[] ps = new String[0];
                                        value = env.get("permissions");
                                        if (!value.istable()) value = env.get("user_permission");
                                        if (value.istable()) {
                                            LuaTable tb = value.checktable();

                                            int len = tb.length();
                                            ps = new String[len];
                                            for (int i = 0; i < len; i++) {
                                                String p = tb.get(i + 1).tojstring();
                                                if (!p.contains(".")) p = "android.permission." + p;
                                                ps[i] = p;
                                            }
                                        }
                                        try {
                                            String finalAppName = appName;
                                            String finalPkgName = pkgName;
                                            String finalVerName = verName;
                                            String finalVerCode = verCode;
                                            String[] finalPs = ps;
                                            //new LuaActivity().doAsset("bin.lua");
                                            ThreadManager.runOnMainThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isbin != false) {
                                                        try {
                                                            LuaUtil.bin(Editor.this, Environment.getExternalStorageDirectory() + "/Luaide", new File(mdir).getParent(), finalAppName, finalPkgName, finalVerName, finalVerCode, new File(new File(mdir).getParent()).getName(), finalPs);
                                                        } catch (Exception e) {
                                                            YanDialog.show(Editor.this, "", e.getMessage());
                                                        }
                                                        installApk(Environment.getExternalStorageDirectory() + "/Luaide/bin/" + finalAppName + ".apk");
                                                    }
                                                    ThreadManager.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressIndicator.setVisibility(View.GONE);
                                                            run.setBackgroundResource(R.drawable.ic_run_outline);
                                                            dbu.setDrawableColor(run, 0xff53b457);
                                                            //run.setEnabled(true);
                                                        }
                                                    });
                                                }
                                            });
                                        } catch (Exception e) {
                                            progressIndicator.setVisibility(View.GONE);
                                            run.setBackgroundResource(R.drawable.ic_run_outline);
                                            dbu.setDrawableColor(run, 0xff53b457);
                                            YanDialog.show(Editor.this, "", e.getMessage());
                                        }
                                    } catch (Exception e) {
                                        progressIndicator.setVisibility(View.GONE);
                                        run.setBackgroundResource(R.drawable.ic_run_outline);
                                        dbu.setDrawableColor(run, 0xff53b457);
                                        YanDialog.show(Editor.this, "", e.getMessage());
                                    }

                                }
                                break;
                        }
                        break;
                    case 1:
                        int id = arg0.getItemId();
                        switch (id) {
                            case 0:
                                ed.putBoolean("Wordwarp", (!arg0.isChecked()));
                                ed.commit();
                                for (int i = 0; i <= fragments.size() - 1; ++i) {
                                    fragments.get(i).edit.setWordwrap(!arg0.isChecked());
                                }
                                arg0.setChecked(!arg0.isChecked());
                                break;
                            case 1:
                                ed.putBoolean("linenumber", (!arg0.isChecked()));
                                ed.commit();
                                for (int i = 0; i <= fragments.size() - 1; ++i) {
                                    fragments.get(i).edit.setLineNumberEnabled(!arg0.isChecked());
                                }
                                arg0.setChecked(!arg0.isChecked());
                                break;
                            case 2:
                                ed.putBoolean("pin", (!arg0.isChecked()));
                                ed.commit();
                                for (int i = 0; i <= fragments.size() - 1; ++i) {
                                    fragments.get(i).edit.setPinLineNumber(!arg0.isChecked());
                                }
                                arg0.setChecked(!arg0.isChecked());
                                break;
                            case 3:
                                ThreadManager.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        /*String str = fragments.get(pager_choice).edit.getText().toString();
                                        int line=fragments.get(pager_choice).edit.getCursor().getLeftLine();
                                        int col=fragments.get(pager_choice).edit.getCursor().getLeftColumn();
                                        fragments.get(pager_choice).edit.selectAll();
                                        fragments.get(pager_choice).edit.deleteText();
                                        fragments.get(pager_choice).edit.commitText(AutoIndent.format(str, 4).toString());
                                        try {
                                            fragments.get(pager_choice).edit.setSelection(line, col, true);
                                        }catch (Exception e){
                                            fragments.get(pager_choice).edit.setSelection(line,fragments.get(pager_choice).edit.getText().getColumnCount(line),true);
                                        }*/
                                        CodeEditor editor = fragments.get(pager_choice).edit;
                                        int line = editor.getCursor().getLeftLine();
                                        int col = editor.getCursor().getLeftColumn();
                                        editor.formatCodeAsync();
                                        editor.postInLifecycle(new Runnable() {
                                            @Override
                                            public void run() {
                                                CharSequence text = editor.getText();
                                                CharSequence charSequence = text;
                                                CharSequence format = AutoIndent.format(charSequence, 4);
                                                ((Content) text).beginBatchEdit();
                                                ((Content) text).delete(0, text.length());
                                                ((Content) text).insert(0, 0, format);
                                                ((Content) text).endBatchEdit();
                                            }
                                        });
                                        editor.postInLifecycle(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    editor.setSelection(line, col, true);
                                                } catch (Exception e) {
                                                    editor.setSelection(line, editor.getText().getColumnCount(line), true);
                                                }
                                            }
                                        });
                                        //LuaUtil.save2("/sdcard/Luaide/ispid.log",memoryTool.getProcessPid("com.yan.luaide")+"");
                                        /*memoryTool.searchMemory(memoryTool.getProcessPid("com.yan.luaide"), 999, new MemoryTool.OnSearchListener() {
                                            @Override
                                            public void onSuccess(int resultCount) {
                                                LuaUtil.save2("/sdcard/Luaide/ispid.log",resultCount+"");
                                            }

                                            @Override
                                            public void onFailure(String message) {
                                                LuaUtil.save2("/sdcard/Luaide/ispid.log",message);
                                            }
                                        });*/
                                    }
                                });
                                //startActivity(new Intent(Editor.this, MemoryTest.class));
                                break;
                            case 4:
                                fragments.get(pager_choice).setSearchPanel(true);
                                break;
                            case 5:
                                /*new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            InitCompletion initCompletion = new InitCompletion(Editor.this);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                HashMap<String,Object> map=initCompletion.getClassTree();
                                                HashMap<String,String> varMap=new HashMap<>(),importMap=new HashMap<>();
                                                //varMap.put("show","")
                                                importMap.put("Builder","");
                                                LuaUtil.save2("/sdcard/Luaide/new.log", initCompletion.getClassTree().toString());
                                            }
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }).start();*/
                                //analyzeErrors(fragments.get(pager_choice).edit.getText().toString());
                                Material3ProgressDialog dlg = new Material3ProgressDialog(Editor.this);
                                dlg.show();
                                PomDownloader downloader = new PomDownloader();
                                downloader.setDownloadListener(new PomDownloader.DownloadListener() {
                                    @Override
                                    public void onProgress(String fileName, long current, long total) {
                                        int pct = total > 0 ? (int)(current * 100 / total) : 0;
                                        dlg.updateProgress1(pct);               // 当前文件
                                        dlg.setMessage(fileName);               // 文件名

                                    }
                                    @Override
                                    public void onFileFinished(String fileName) {
                                        //dlg.dismiss();
                                        //System.out.println(fileName);
                                    }
                                    @Override
                                    public void onTotalProgress(String string,int finished, int total) {
                                        dlg.updateTotalProgress(string,finished, total);
                                    }
                                });
                                new Thread(() -> {
                                    try {
                                        List<File> deps = downloader.downloadTransitive(Editor.this,
                                                "androidx.appcompat:appcompat:1.6.1");
                                        for (File f : deps) {
                                            //System.out.println("下载完成：" + f.getAbsolutePath());
                                            try {
                                                if (f.getAbsolutePath().endsWith("jar"))
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        new ApkTools().jar2dex(Editor.this,f.getAbsolutePath(), f.getAbsolutePath().replaceFirst(".jar", ".dex"));
                                                    }
                                            } catch (Exception e) {
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                        runOnUiThread(dlg::dismiss);
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                        runOnUiThread(() -> {
                                            dlg.setTitle("下载失败");
                                            dlg.setMessage(e.getMessage());
                                            //new android.os.Handler().postDelayed(dlg::dismiss, 3000);
                                        });
                                    }
                                }).start();
                                break;
                        }
                        break;
                    case 2:
                        int id2 = arg0.getItemId();
                        CodeEditor edit = fragments.get(pager_choice).edit;
                        switch (id2) {
                            case 0:
                                edit.setSelection(edit.getText().getLineCount() - 1, edit.getText().getColumnCount(edit.getText().getLineCount() - 1));
                                break;
                            case 1:
                                edit.moveSelection(SelectionMovement.LEFT);
                                break;
                            case 2:
                                edit.moveSelection(SelectionMovement.RIGHT);
                                break;
                            case 3:
                                edit.moveSelection(SelectionMovement.UP);
                                break;
                            case 4:
                                edit.moveSelection(SelectionMovement.DOWN);
                                break;
                            case 5:
                                edit.moveSelection(SelectionMovement.LINE_START);
                                break;
                            case 6:
                                edit.moveSelection(SelectionMovement.LINE_END);
                                break;
                        }
                        break;
                    case 3:
                        int id3 = arg0.getItemId();
                        switch (id3) {
                            case 0:
                                showFileChooserDialog();
                                break;
                            case 1:
                                show(Editor.this);
                                break;
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化所有控件
     */
    public void initView() {
        undo = binding.undo;
        redo = binding.redo;
        run = binding.run;
        menu = binding.menu;
        drawerLeft = binding.drawerLeft;
        drawerLayout = binding.drawerLayout;
        NavigationView = binding.navView;
        tabLayout = binding.tablayout;
        viewPager2 = binding.viewpager2;
        toolbar = binding.toolbar;
        progressIndicator = binding.progress;
        //toolbar.setBackgroundColor(0xfefbff);
        int color = 0xff000000;
        if (bg == 1) color = 0xffffffff;
        dbu.setDrawableColor(undo, color);
        dbu.setDrawableColor(redo, color);
        dbu.setDrawableColor(run, color);
        dbu.setDrawableColor(menu, color);
        NavigationView.getMenu().findItem(R.id.files).setChecked(true);
        setupDrawerContent(NavigationView);
        displayFragment(filetree);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setUserInputEnabled(false);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    /**
     * 初始化点击事件
     */
    public void initClick() {
        run.setOnClickListener(this);
        redo.setOnClickListener(this);
        undo.setOnClickListener(this);
        menu.setOnClickListener(this);
    }

    /**
     * 管理侧边栏菜单
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int iid = item.getItemId();
            if (iid == R.id.files) {
                fragment = filetree;
            } else if (iid == R.id.toolbox) {
                fragment = toolboxListFragment;
            } else if (iid == R.id.set) {
                startActivity(new Intent(this, ActivitySet.class));
            } /*else if (iid == R.id.paring) {
              fragment = codet;
            }*/
            if (fragment != null) {
                // 检查是否已经选中
                if (!navigationView.getMenu().findItem(iid).isChecked()) {
                    displayFragment(fragment);
                    navigationView.getMenu().findItem(item.getItemId()).setChecked(true);
                    return true;
                }
            }
            return false;
        });
    }

    private boolean isTransactionInProgress = false;

    private void displayFragment(Fragment fragment) {
        if (isTransactionInProgress) {
            return;
        }
        isTransactionInProgress = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();

        isTransactionInProgress = false;
    }

    /**
     * 向tab和pager里添加项
     *
     * @fileName :要打开的文件的路径
     * @position :要删除的项在tab中的位置
     */
    public void addFileToUI(String fileName) {
        if (fragments.size() == 0) {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager2.setVisibility(View.VISIBLE);
            new DrawableUtil().setDrawableColor(run, 0xff53b457);
        }
        boolean isequal = false;
        if (AllPath.size() != 0) {
            for (String ss : AllPath) {
                if (fileName.equals(ss)) {
                    isequal = true;
                    break;
                }
            }
        }
        if (isequal) {
            for (int i = 0; i <= fragments.size() - 1; ++i) {
                if (AllPath.get(i).equals(fileName)) {
                    viewPager2.setCurrentItem(i);
                }
            }
            isequal = false;
        } else {
            AllPath.add(fileName);
            // 创建新的 Fragment
            FileContentFragment fragment = FileContentFragment.newInstance();
            // 将文件名传递给 Fragment
            fragment.setFileName(fileName);
            FragmentStateAdapter adapter = (FragmentStateAdapter) viewPager2.getAdapter();
            if (adapter == null) {
                adapter = new FragmentStateAdapter(this) {
                    @NonNull
                    @Override
                    public FileContentFragment createFragment(int position) {
                        return (FileContentFragment) fragments.get(position);
                    }

                    @Override
                    public int getItemCount() {
                        return fragments.size();
                    }
                };
                viewPager2.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
            // 添加到列表并更新 TabLayout
            fragments.add(fragment);
            new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                // 设置标签文本为文件名
                tab.setText(fragments.get(position).getFileName());
                binding.toolbar.setSubtitle(fragments.get(position).getFileName());
            }).attach();
        }
        viewPager2.setCurrentItem(fragments.size() - 1);
    }

    private void removeTabAndFragment(int position) {
        fragments.remove(position);
        AllPath.remove(position);
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public FileContentFragment createFragment(int position) {
                return (FileContentFragment) fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        };
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, newPosition) -> {
            // 设置标签文本为文件名
            tab.setText(fragments.get(newPosition).getFileName());
        }).attach();
    }


    /**
     * 管理点击事件
     */
    @Override
    public void onClick(View arg0) {
        int mid = arg0.getId();
        if (mid == R.id.run) {
            if (mdir != null) {
                if (isbin == true) {
                    ThreadManager.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressIndicator.setVisibility(View.GONE);
                            run.setBackgroundResource(R.drawable.ic_run_outline);
                            dbu.setDrawableColor(run, 0xff53b457);
                        }
                    });
                    isbin = false;
                } else {
                    for (int i = 0; i <= fragments.size() - 1; i++) {
                        LuaUtil.save2(AllPath.get(i), fragments.get(i).edit.getText().toString());
                    }
                    startActivity(new Intent(Editor.this, LuaActivity.class).setData(Uri.fromFile(new File(fragments.get(pager_choice).fileName))));
                }
            }
        } else if (mid == R.id.redo) {
            if (fragments.size() != 0) {
                if (fragments.get(pager_choice).canredo()) fragments.get(pager_choice).redo();
            }
        } else if (mid == R.id.undo) {
            if (fragments.size() != 0) {
                if (fragments.get(pager_choice).canundo()) fragments.get(pager_choice).undo();
            }
        } else if (mid == R.id.menu) {
            popo.show();
        }
    }

    /**
     * 安装apk
     * String 文件绝对路径
     */
    public void installApk(String path) {
        File file = new File(path);
        Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 文件选择器
     */
    File currentDirectory = new File(String.valueOf(Environment.getExternalStorageDirectory()));

    private void showFileChooserDialog() {
        // 获取当前目录下的所有文件和文件夹
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
        for (File file : files) {
            sortedFileNames.add(file.getName());
            sortedFilePaths.add(file.getPath());
        }
        // 创建文件选择对话框
        // Toast.makeText(MainActivity.this,"",Toast.);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder
                .setTitle("选择文件")
                .setItems(
                        sortedFileNames.toArray(new String[0]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 用户点击文件时的处理逻辑
                                String selectedFilePath = sortedFilePaths.get(which);
                                File selectedFile = new File(selectedFilePath);
                                if (selectedFile.isDirectory()) {
                                    // 如果是文件夹，则进入文件夹
                                    currentDirectory = selectedFile;
                                    showFileChooserDialog(); // 递归显示文件选择对话框
                                } else {
                                    // 如果是文件，则处理文件的逻辑，例如打开文件等
                                    Toast.makeText(Editor.this, "选择了文件：" + selectedFilePath, Toast.LENGTH_SHORT)
                                            .show();
                                    try {
                                        Intent intent1 = new Intent(Editor.this, Editor.class);
                                        intent1.putExtra("mdir", selectedFilePath);
                                        startActivity(intent1);
                                        finish();
                                    } catch (Exception e) {
                                        YanDialog.show(Editor.this, "error", e.getMessage());
                                    }
                                }
                            }
                        })
                .setNegativeButton(
                        "返回上层",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 用户点击返回上层时的处理逻辑
                                if (!currentDirectory.equals(Environment.getExternalStorageDirectory())) {
                                    currentDirectory = currentDirectory.getParentFile();
                                    showFileChooserDialog(); // 递归显示文件选择对话框
                                }
                            }
                        })
                .setNeutralButton(
                        "取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 用户点击取消时的处理逻辑
                                dialog.dismiss();
                                // 结束Activity
                            }
                        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *
     */
    public void show(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.new_projects_dialog, null);
        TextInputEditText inputEditText = dialogView.findViewById(R.id.edit_text);
        TextInputEditText inputEditText2 = dialogView.findViewById(R.id.edit_project);
        MaterialButton new_project_true = dialogView.findViewById(R.id.new_project_true);
        MaterialButton new_project_false = dialogView.findViewById(R.id.new_project_false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        List<String> options = Arrays.asList("无", "侧滑栏", "导航栏", "Androidx", "普通布局", "TabLayout");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Editor.this, android.R.layout.simple_list_item_1, options);
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapter);
        new_project_false.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        new_project_true.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputEditText.getText().toString().equals("")) {
                    YanDialog.show(Editor.this, "错误", "名称不能为空");
                    return;
                }
                switch (autoCompleteTextView.getText().toString()) {
                    case "无":
                        new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString()).mkdirs();
                        new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "layout").mkdirs();
                        new File(Environment.getExternalStorageDirectory() + "/Luaide/Manifest/" + inputEditText.getText().toString()).mkdirs();
                        if (!new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "init.lua").exists()) {
                            LuaUtil.save2(
                                    new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "init.lua").getAbsolutePath(),
                                    String.format(
                                            "appname=\"%s\"\n"
                                                    + "ver_name=\"1.0\"\n"
                                                    + "ver_code=\"1\"\n"
                                                    + "packagename=\"%s\"\n"
                                                    + "developer=\"\"\n"
                                                    + "description=\"\"\n"
                                                    + "debug_mode=true\n"
                                                    + "user_permission={\n"
                                                    + "  \"INTERNET\",\n"
                                                    + "  \"WRITE_EXTERNAL_STORAGE\"\n"
                                                    + "}",
                                            inputEditText.getText().toString(), inputEditText2.getText().toString()));
                            LuaUtil.save2(
                                    new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "layout/main_activity.aly").getAbsolutePath(),
                                    "{\n"
                                            + "  LinearLayout,\n"
                                            + "  orientation=\"vertical\",\n"
                                            + "  layout_width=\"fill\",\n"
                                            + "  layout_height=\"fill\",\n"
                                            + "  {\n"
                                            + "    TextView,\n"
                                            + "    id=\"tv\",\n"
                                            + "    text=\"Hello Luaide\",\n"
                                            + "    layout_width=\"fill\",\n"
                                            + "  },\n"
                                            + "}");
                            if (!new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "main.lua").exists()) {
                                LuaUtil.save2(
                                        new File(Environment.getExternalStorageDirectory() + "/Luaide/Projects/" + inputEditText.getText().toString(), "main.lua").getAbsolutePath(),
                                        "require \"import\"\n"
                                                + "import \"android.app.*\"\n"
                                                + "import \"android.widget.*\"\n"
                                                + "import \"com.yan.luaide.*\"\n"
                                                + "import \"java.lang.*\"\n"
                                                + "import \"java.util.*\"\n"
                                                + "import \"layout.main_activity\"\n"
                                                + "activity.setTitle(\"" + inputEditText.getText().toString() + "\")\n"
                                                + "activity.setTheme(R.style.Theme_Material3_Blue)\n"
                                                + "activity.setContentView(loadlayout(main_activity))");
                            }
                        }
                        break;
                    case "侧滑栏":
                        /*LuaUtil.copyAssetsFolder(getActivity(), "侧滑栏", "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString());
                        LuaUtil.save(
                                "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString(),
                                str);*/
                        YanDialog.show(Editor.this, "未开发", "暂未开发此模板");
                        break;
                    case "导航栏":
                        YanDialog.show(Editor.this, "未开发", "暂未开发此模板");
                        break;
                    case "Androidx":
                        /*LuaUtil.copyAssetsFolder(getActivity(), "Androidx", "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString());
                        LuaUtil.save(
                                "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString(),
                                str);*/
                        YanDialog.show(Editor.this, "未开发", "暂未开发此模板");
                        break;
                    case "普通布局":
                        /*LuaUtil.copyAssetsFolder(getActivity(), "普通布局", "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString());
                        LuaUtil.save(
                                "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString(),
                                str);*/
                        YanDialog.show(Editor.this, "未开发", "暂未开发此模板");
                        break;
                    case "TabLayout":
                        /*LuaUtil.copyAssetsFolder(getActivity(), "TabLayout", "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString());
                        LuaUtil.save(
                                "/sdcard/YLuaApp/Projects/" + inputEditText.getText().toString(),
                                str);*/
                        YanDialog.show(Editor.this, "未开发", "暂未开发此模板");
                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * listfunc listFiles and setproject and getList
     */

    private void listFiles(File directory, List<String> fileNames, List<String> filePaths) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
                filePaths.add(file.getAbsolutePath());
            }
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否已经拥有所有文件的管理权限
            if (Environment.isExternalStorageManager()) {
                Log.d("Permission", "Already have permission");
            } else {
                // 申请权限
                finish();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        } else {
            Log.d("Permission", "Not required on this Android version");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public static void onRequestPermissionsResult(AppCompatActivity activity, int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        if (requestCode == 1) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                Log.d("Permission", "All permissions granted");
            } else {
                Log.d("Permission", "Some permissions denied");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*SharedPreferences Scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        if (Scheme.getInt("Scheme", 0) != sc || Scheme.getInt("Background", 0) != bg) {
            for (int i = 0; i < fragments.size(); i++) {
                removeTabAndFragment(i);
            }
            recreate();
        }

         */
        updateTheme();
    }
}
