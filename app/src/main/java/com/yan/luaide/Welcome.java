package com.yan.luaide;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;
import com.luajava.LuaFunction;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;
import com.yan.luaeditor.Editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Welcome extends AppCompatActivity {

    private boolean isUpdata;

    private LuaApplication app;

    private String luaMdDir;

    private String localDir;

    private long mLastTime;

    private long mOldLastTime;

    private ProgressDialog pd;

    private boolean isVersionChanged;

    private String mVersionName;

    private String mOldVersionName;

    private ArrayList<String> permissions;
    SharedPreferences sps, sps2;
    SharedPreferences.Editor ed, ed2;
    String[] symbols =
            new String[]{
                    "->", "F", "←", "→", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/", "=", "'",
                    "[", "]", ":", "\\", "<", ">", "~", "!"
            };
    String[] equivalents =
            new String[]{
                    "\t",
                    "function",
                    "LEFT",
                    "RIGHT",
                    "{",
                    "}",
                    "(",
                    ")",
                    ",",
                    ".",
                    ";",
                    "\"",
                    "?",
                    "+",
                    "-",
                    "*",
                    "/",
                    "=",
                    "'",
                    "[",
                    "]",
                    ":",
                    "\\",
                    "<>",
                    ">",
                    "~",
                    "!"
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences Scheme = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        if ((Scheme.getInt("Background", 0) == 0)) {
            switch (Scheme.getInt("Scheme", 0)) {
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
        } else {
            switch (Scheme.getInt("Scheme", 0)) {
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
        }

        setContentView(R.layout.layout);
        app = (LuaApplication) getApplication();
        luaMdDir = app.luaMdDir;
        localDir = app.localDir;
        try {
            if (new File(app.getLuaPath("setup.png")).exists())
                getWindow().setBackgroundDrawable(new LuaBitmapDrawable(app, app.getLuaPath("setup.png"), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (checkInfo()) {
            if (Build.VERSION.SDK_INT >= 23) {

                LuaState L = LuaStateFactory.newLuaState();
                L.openLibs();
                try {
                    if (L.LloadBuffer(LuaUtil.readAsset(Welcome.this, "init.lua"), "init") == 0) {
                        if (L.pcall(0, 0, 0) == 0) {
                            int func = L.getGlobal("check_permissions");
                            if (func == LuaState.LUA_TBOOLEAN && L.toBoolean(-1)) {
                                new UpdateTask().execute();
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }


                try {
                    permissions = new ArrayList<String>();
                    String[] ps2 = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                    for (String p : ps2) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                if (p.equals("android.permission.MANAGE_EXTERNAL_STORAGE")&&!Environment.isExternalStorageManager()){
                                    Intent intent = null;
                                    intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 1);
                                }else {
                                    checkPermission(p);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!permissions.isEmpty()) {
                        String[] ps = new String[permissions.size()];
                        permissions.toArray(ps);
                        requestPermissions(ps,
                                0);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            new UpdateTask().execute();
        } else {
            startActivity();
        }
    }

    private void checkPermission(String permission) {
        if (checkCallingOrSelfPermission(permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new UpdateTask().execute();
    }

    public void startActivity() {
        try {
            InputStream f = getAssets().open("main.lua");
            if (f != null) {
                Intent intent = new Intent(Welcome.this, Main.class);
                if (isVersionChanged) {
                    intent.putExtra("isVersionChanged", isVersionChanged);
                    intent.putExtra("newVersionName", mVersionName);
                    intent.putExtra("oldVersionName", mOldVersionName);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }
        } catch (Exception e) {
            sps = getSharedPreferences("EditorSymbol", Context.MODE_PRIVATE);
            sps2 = getSharedPreferences("Equivalents", Context.MODE_PRIVATE);
            ed = sps.edit();
            ed2 = sps2.edit();
            String filePath = getApplicationInfo().dataDir + "/shared_prefs/EditorSymbol.xml";
            File file = new File(filePath);
            if (!file.exists()) {
                for (int i = 0; i < symbols.length; i++) {
                    ed.putString(String.valueOf(i), symbols[i]);
                    ed2.putString(String.valueOf(i), equivalents[i]);
                }
                ed.commit();
                ed2.commit();
            }
            sps = getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
            Intent intent = new Intent(Welcome.this, Editor.class);
            if (!sps.getString("OpenFile", "nil").equals("nil"))
                intent.putExtra("mdir", sps.getString("OpenFile", "nil"));
            startActivity(intent);
            finish();
        }

    }

    public boolean checkInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            long lastTime = packageInfo.lastUpdateTime;
            String versionName = packageInfo.versionName;
            SharedPreferences info = getSharedPreferences("appInfo", 0);
            String oldVersionName = info.getString("versionName", "");
            if (!versionName.equals(oldVersionName)) {
                SharedPreferences.Editor edit = info.edit();
                edit.putString("versionName", versionName);
                edit.apply();
                isVersionChanged = true;
                mVersionName = versionName;
                mOldVersionName = oldVersionName;
            }
            long oldLastTime = info.getLong("lastUpdateTime", 0);
            if (oldLastTime != lastTime) {
                SharedPreferences.Editor edit = info.edit();
                edit.putLong("lastUpdateTime", lastTime);
                edit.apply();
                isUpdata = true;
                mLastTime = lastTime;
                mOldLastTime = oldLastTime;
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    @SuppressLint("StaticFieldLeak")
    private class UpdateTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String[] p1) {
            // TODO: Implement this method
            onUpdate(mLastTime, mOldLastTime);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            startActivity();
        }

        private void onUpdate(long lastTime, long oldLastTime) {

            LuaState L = LuaStateFactory.newLuaState();
            L.openLibs();
            try {
                if (L.LloadBuffer(LuaUtil.readAsset(Welcome.this, "update.lua"), "update") == 0) {
                    if (L.pcall(0, 0, 0) == 0) {
                        LuaFunction func = L.getFunction("onUpdate");
                        if (func != null)
                            func.call(mVersionName, mOldVersionName);
                    }
                    ;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                //LuaUtil.rmDir(new File(localDir),".lua");
                //LuaUtil.rmDir(new File(luaMdDir),".lua");


                unApk("assets", localDir);
                unApk("lua", luaMdDir);
                //unZipAssets("main.alp", extDir);
            } catch (IOException e) {
                sendMsg(e.getMessage());
            }
        }

        private void sendMsg(String message) {
            // TODO: Implement this method

        }

        private void unApk(String dir, String extDir) throws IOException {
            int i = dir.length() + 1;
            ZipFile zip = new ZipFile(getApplicationInfo().publicSourceDir);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.indexOf(dir) != 0)
                    continue;
                String path = name.substring(i);
                if (entry.isDirectory()) {
                    File f = new File(extDir + File.separator + path);
                    if (!f.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        f.mkdirs();
                    }
                } else {
                    String fname = extDir + File.separator + path;
                    File ff = new File(fname);
                    File temp = new File(fname).getParentFile();
                    if (!temp.exists()) {
                        if (!temp.mkdirs()) {
                            throw new RuntimeException("create file " + temp.getName() + " fail");
                        }
                    }
                    try {
                        if (ff.exists() && entry.getSize() == ff.length() && LuaUtil.getFileMD5(zip.getInputStream(entry)).equals(LuaUtil.getFileMD5(ff)))
                            continue;
                    } catch (NullPointerException ignored) {
                    }
                    FileOutputStream out = new FileOutputStream(extDir + File.separator + path);
                    InputStream in = zip.getInputStream(entry);
                    byte[] buf = new byte[4096];
                    int count = 0;
                    while ((count = in.read(buf)) != -1) {
                        out.write(buf, 0, count);
                    }
                    out.close();
                    in.close();
                }
            }
            zip.close();
        }

    }
}
