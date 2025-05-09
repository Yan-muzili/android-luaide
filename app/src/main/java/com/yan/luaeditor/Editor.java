package com.yan.luaeditor;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.FileProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.yan.luaeditor.tools.AndroidBug5497Workaround;
import com.yan.luaeditor.tools.ClassMethodScanner;
import com.yan.luaeditor.tools.CompleteHashmapUtils;
import com.yan.luaeditor.tools.DrawableUtil;
import com.yan.luaeditor.tools.PackageUtil;
import com.yan.luaeditor.tools.ThreadManager;
import com.yan.luaeditor.tools.YanDialog;
import com.yan.luaeditor.format.AutoIndent;
import com.yan.luaeditor.tools.YanToast;
import com.yan.luaeditor.ui.ActivitySet;
import com.yan.luaeditor.ui.FileTreeFragment;
import com.yan.luaeditor.ui.ToolboxListFragment;
import com.yan.luaide.LuaActivity;
import com.yan.luaide.LuaUtil;
import com.yan.luaide.R;
import com.yan.luaide.databinding.EditorActivityBinding;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SelectionMovement;
import lide.luaj.vm2.Globals;
import lide.luaj.vm2.LuaTable;
import lide.luaj.vm2.lib.jse.JsePlatform;
import lide.luaj.vm2.LuaValue;

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
    SharedPreferences sps;
    SharedPreferences.Editor ed;
    HashMap<String, HashMap<String, CompletionName>> base;
    HashMap<String, List<String>> classMap2;
    int bg, sc = 0;

    /**
     * --------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences Scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        if ((Scheme.getInt("Background", 0) == 0)) {
            if ((Scheme.getInt("Scheme", -1)) != -1) {
                switch (Scheme.getInt("Scheme", -1)) {
                    case 0:
                        setTheme(R.style.Theme_AndroidIDE_BlueWave);
                        break;
                    case 1:
                        setTheme(R.style.Theme_AndroidIDE_SunnyGlow);
                        break;
                    case 2:
                        setTheme(R.style.Theme_Material3_Blue_NoActionBar);
                        break;
                    case 3:
                        setTheme(R.style.Theme_Material3_Green_NoActionBar);
                        break;
                    case 4:
                        setTheme(R.style.Theme_Material3_Orange_NoActionBar);
                        break;
                    case 5:
                        setTheme(R.style.Theme_Material3_Brown_NoActionBar);
                        break;
                    case 6:
                        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
                        break;
                }
                sc = (Scheme.getInt("Scheme", -1));
                bg = 0;
            }
        } else {
            if ((Scheme.getInt("Scheme", -1)) != -1) {
                switch (Scheme.getInt("Scheme", -1)) {
                    case 0:
                        setTheme(R.style.Theme_AndroidIDE_BlueWave_Dark);
                        break;
                    case 1:
                        setTheme(R.style.Theme_AndroidIDE_SunnyGlow_Dark);
                        break;
                    case 2:
                        setTheme(R.style.Theme_Material3_Blue_Dark_NoActionBar);
                        break;
                    case 3:
                        setTheme(R.style.Theme_Material3_Green_Dark_NoActionBar);
                        break;
                    case 4:
                        setTheme(R.style.Theme_Material3_Orange_Dark_NoActionBar);
                        break;
                    case 5:
                        setTheme(R.style.Theme_Material3_Brown_Dark_NoActionBar);
                        break;
                    case 6:
                        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
                        break;
                }
                sc = (Scheme.getInt("Scheme", -1));
                bg = 1;
            }
        }


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
        if (!new File(getFilesDir().getAbsolutePath() + "/complete.base").exists() || !new File(getFilesDir().getAbsolutePath() + "/complete2.base").exists()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("是否初始化Luaide的代码补全")
                    .setPositiveButton("确定", (dialog, which) -> {

                        ThreadManager.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {

                                ThreadManager.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressIndicator.setVisibility(View.VISIBLE);
                                    }
                                });
                                //MakeCompleteTree.scanClassesAndMethods(activity);
                                try {
                                    HashMap<String, List<String>> basemap = PackageUtil.load(Editor.this);
                                    basemap.get("R$style").add("android.R$style");
                                    for(String na:basemap.keySet()) {
                                        for (String className : basemap.get(na)) {
                                            try {
                                                Class<?> clazz = Class.forName(className);
                                                Class<?>[] classes = clazz.getClasses();
                                                for (Class<?> innerClass : classes) {
                                                    String fullInnerClassName = className + "$" + innerClass.getSimpleName();
                                                    //newClassNames.add(fullInnerClassName);
                                                    String[] ss=fullInnerClassName.split("\\.");
                                                    String ccl="";
                                                    if (ss.length > 0) {
                                                        ccl  = ss[ss.length - 1];
                                                    }
                                                    if (basemap.get(ccl)!=null)
                                                    basemap.get(ccl).add(fullInnerClassName);
                                                }
                                            } catch (ClassNotFoundException |
                                                     NoClassDefFoundError | NoSuchMethodError e) {
                                                // 打印错误信息，方便调试
                                                System.err.println("Class not found: " + className);
                                            }
                                        }

                                    }


                                    List<String> allclassname = new ArrayList<>();
                                    for (String list : basemap.keySet()) {
                                        allclassname.addAll(basemap.get(list));
                                    }

                                    //LuaUtil.save2("/sdcard/Luaide/allclassname.log",allclassname.toString());
                                    Editor.this.classMap2 = new HashMap<>();
                                    for (String k : basemap.keySet()) {
                                        List<String> strlist = new ArrayList<>();
                                        for (String str : basemap.get(k)) {
                                            if (str.startsWith("com.yan.luaide.R")){
                                                strlist.add(str.replaceAll("\\$","."));
                                                Editor.this.classMap2.put(str.replaceAll("com.yan.luaide.R\\$","R."),strlist);
                                            } else if (str.contains("R$")) {
                                                strlist.add(str.replaceAll("\\$","."));
                                                Editor.this.classMap2.put(str.replaceAll("\\$","."),strlist);
                                            }else {
                                            strlist.add(str.replaceAll("\\$", "."));
                                                Editor.this.classMap2.put(k.replaceAll("\\$", "."), strlist);
                                            }
                                        }

                                    }

// 分批处理类名
                                    int batchSize = 1000;
                                    Editor.this.base = new HashMap<>();
                                    ClassMethodScanner scanner = new ClassMethodScanner();
                                    for (int i = 0; i < allclassname.size(); i += batchSize) {
                                        int endIndex = Math.min(i + batchSize, allclassname.size());
                                        List<String> batch = allclassname.subList(i, endIndex);
                                        HashMap<String, HashMap<String, CompletionName>> batchResult = scanner.scanClassesAndMethods(batch);
                                        Editor.this.base.putAll(batchResult);
                                        System.gc();
                                    }
                                    //LuaUtil.save2("/sdcard/Luaide/yyy.log",basemap.toString());
                                    CompleteHashmapUtils.saveHashMapToFile(Editor.this, Editor.this.base, "complete.base");
                                    CompleteHashmapUtils.saveHashMapToFile2(Editor.this, Editor.this.classMap2, "complete2.base");
                                } catch (Exception e) {
                                    //YanDialog.show(Editor.this,"Error",e.getMessage());
                                    System.out.println(e.getMessage());
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
                            }
                        });
                        dialog.dismiss();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            base = CompleteHashmapUtils.loadHashMapFromFile(this, "complete.base");
            classMap2 = CompleteHashmapUtils.loadHashMapFromFile2(this, "complete2.base");
            //LuaUtil.save2("/sdcard/Luaide/yyy.log",base+"\n"+classMap2);
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
        setSupportActionBar(binding.toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, 0, 0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT);
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
        func.add(1,4,0,"搜索文本");
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
                                                ((io.github.rosemoe.sora.text.Content) text).beginBatchEdit();
                                                ((io.github.rosemoe.sora.text.Content) text).delete(0, text.length());
                                                ((io.github.rosemoe.sora.text.Content) text).insert(0, 0, format);
                                                ((io.github.rosemoe.sora.text.Content) text).endBatchEdit();
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
                                    }
                                });
                                break;
                            case 4:
                                fragments.get(pager_choice).setSearchPanel(true);
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

    // 获取文件和文件夹列表
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
                        this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
                // 可以在这里提示用户权限被拒绝的信息
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
        SharedPreferences Scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        if (Scheme.getInt("Scheme", 0) != sc || Scheme.getInt("Background", 0) != bg) {
            for (int i = 0; i < fragments.size(); i++) {
                removeTabAndFragment(i);
            }
            recreate();
        }
    }
}
