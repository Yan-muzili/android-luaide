package com.yan.luaide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.FileProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.luajava.LuaState;
import com.luajava.LuaTable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class LuaApplication extends Application implements LuaContext {

    private static LuaApplication mApp;
    static private HashMap<String, Object> data = new HashMap<String, Object>();
    protected String localDir;
    protected String odexDir;
    protected String libDir;
    protected String luaMdDir;
    protected String luaCpath;
    protected String luaLpath;
    protected String luaExtDir;
    private boolean isUpdata;
    private SharedPreferences mSharedPreferences;
    private static Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    public Uri getUriForPath(String path) {
        return FileProvider.getUriForFile(this, getPackageName(), new File(path));
    }

    public Uri getUriForFile(File path) {
        return FileProvider.getUriForFile(this, getPackageName(), path);
    }

    public String getPathFromUri(Uri uri) {

        String path = null;
        if (uri != null) {
            String[] p = {
                    getPackageName()
            };
            switch (uri.getScheme()) {
                case "content":
                    /*try {
						InputStream in = getContentResolver().openInputStream(uri);
					} catch (IOException e) {
						e.printStackTrace();
					}*/
                    Cursor cursor = getContentResolver().query(uri, p, null, null, null);

                    if (cursor != null) {
                        int idx = cursor.getColumnIndexOrThrow(getPackageName());
                        if (idx < 0)
                            break;
                        path = cursor.getString(idx);
                        cursor.moveToFirst();
                        cursor.close();
                    }
                    break;
                case "file":
                    path = uri.getPath();
                    break;
            }
        }
        return path;
    }


    public static LuaApplication getInstance() {
        return mApp;
    }

    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        // TODO: Implement this method
        return null;
    }

    @Override
    public void regGc(LuaGcable obj) {
        // TODO: Implement this method
    }

    @Override
    public String getLuaPath() {
        // TODO: Implement this method
        return null;
    }

    @Override
    public String getLuaPath(String path) {
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaPath(String dir, String name) {
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String path) {
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String dir, String name) {
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    public int getWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public int getHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public String getLuaDir(String dir) {
        // TODO: Implement this method
        return localDir;
    }

    @Override
    public String getLuaExtDir(String name) {
        File dir = new File(getLuaExtDir(), name);
        if (!dir.exists())
            if (!dir.mkdirs())
                return dir.getAbsolutePath();
        return dir.getAbsolutePath();
    }

    public String getLibDir() {
        // TODO: Implement this method
        return libDir;
    }

    public String getOdexDir() {
        // TODO: Implement this method
        return odexDir;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        CrashHandler.getInstance().registerGlobal(this);
        CrashHandler.getInstance().registerPart(this);
        mSharedPreferences = getSharedPreferences(this);
        //初始化AndroLua工作目录
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = sdDir + "/AndroLua";
        } else {
            File[] fs = new File("/storage").listFiles();
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null)
                    continue;
                if (ls.length > 5)
                    luaExtDir = f.getAbsolutePath() + "/AndroLua";
            }
            if (luaExtDir == null)
                luaExtDir = getDir("AndroLua", Context.MODE_PRIVATE).getAbsolutePath();
        }

        File destDir = new File(luaExtDir);
        if (!destDir.exists())
            destDir.mkdirs();

        //定义文件夹
        localDir = getFilesDir().getAbsolutePath();
        odexDir = getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
        libDir = getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        luaMdDir = getDir("lua", Context.MODE_PRIVATE).getAbsolutePath();
        luaCpath = getApplicationInfo().nativeLibraryDir + "/lib?.so" + ";" + libDir + "/lib?.so";
        //luaDir = extDir;
        luaLpath = luaMdDir + "/?.lua;" + luaMdDir + "/lua/?.lua;" + luaMdDir + "/?/init.lua;";
        //checkInfo();
    }

    private static CrashHandler getCrashHandler(CrashHandler crashHandler) {
        return crashHandler;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Context deContext = context.createDeviceProtectedStorageContext();
            if (deContext != null)
                return PreferenceManager.getDefaultSharedPreferences(deContext);
            else
                return PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    @Override
    public String getLuaDir() {
        // TODO: Implement this method
        return localDir;
    }

    @Override
    public void call(String name, Object[] args) {
        // TODO: Implement this method
    }

    @Override
    public void set(String name, Object object) {
        // TODO: Implement this method
        data.put(name, object);
    }

    @Override
    public Map getGlobalData() {
        return data;
    }

    @Override
    public Object getSharedData() {
        return mSharedPreferences.getAll();
    }

    @Override
    public Object getSharedData(String key) {
        return mSharedPreferences.getAll().get(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        Object ret = mSharedPreferences.getAll().get(key);
        if (ret == null)
            return def;
        return ret;
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        if (value == null)
            edit.remove(key);
        else if (value instanceof String)
            edit.putString(key, value.toString());
        else if (value instanceof Long)
            edit.putLong(key, (Long) value);
        else if (value instanceof Integer)
            edit.putInt(key, (Integer) value);
        else if (value instanceof Float)
            edit.putFloat(key, (Float) value);
        else if (value instanceof Set)
            edit.putStringSet(key, (Set<String>) value);
        else if (value instanceof LuaTable)
            edit.putStringSet(key, (HashSet<String>) ((LuaTable) value).values());
        else if (value instanceof Boolean)
            edit.putBoolean(key, (Boolean) value);
        else
            return false;
        edit.apply();
        return true;
    }

    public Object get(String name) {
        // TODO: Implement this method
        return data.get(name);
    }

    public String getLocalDir() {
        // TODO: Implement this method
        return localDir;
    }


    public String getMdDir() {
        // TODO: Implement this method
        return luaMdDir;
    }

    @Override
    public String getLuaExtDir() {
        // TODO: Implement this method
        return luaExtDir;
    }

    @Override
    public void setLuaExtDir(String dir) {
        // TODO: Implement this method
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = new File(sdDir , dir).getAbsolutePath();
        } else {
            File[] fs = new File("/storage").listFiles();
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null)
                    continue;
                if (ls.length > 5)
                    luaExtDir = new File(f, dir).getAbsolutePath() ;
            }
            if (luaExtDir == null)
                luaExtDir = getDir(dir, Context.MODE_PRIVATE).getAbsolutePath();
        }
    }

    @Override
    public String getLuaLpath() {
        // TODO: Implement this method
        return luaLpath;
    }

    @Override
    public String getLuaCpath() {
        // TODO: Implement this method
        return luaCpath;
    }

    @Override
    public Context getContext() {
        // TODO: Implement this method
        return this;
    }

    @Override
    public LuaState getLuaState() {
        // TODO: Implement this method
        return null;
    }

    @Override
    public Object doFile(String path, Object[] arg) {
        // TODO: Implement this method
        return null;
    }

    @Override
    public void sendMsg(String msg) {
        // TODO: Implement this method
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void sendError(String title, Exception msg) {

    }
    public static void write(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[1024 * 8];
        int len;
        while ((len = input.read(buf)) != -1) {
            output.write(buf, 0, len);
        }
    }

    public static void write(File file, byte[] data) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            output.write(data);
        } catch (IOException e) {
            Log.e("GlobalApplication", "Failed to write file: " + file.getAbsolutePath(), e);
            throw e;
        }
    }

    public static String toString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        try {
            return output.toString("UTF-8");
        } finally {
            closeIO(input, output);
        }
    }

    public static void closeIO(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) closeable.close();
            } catch (IOException ignored) {}
        }
    }

    public static class CrashHandler {

        public static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

        private static CrashHandler sInstance;

        private PartCrashHandler mPartCrashHandler;

        public static CrashHandler getInstance() {
            if (sInstance == null) {
                sInstance = new CrashHandler();
            }
            return sInstance;
        }

        public void registerGlobal(Context context) {
            registerGlobal(context, null);
        }

        public void registerGlobal(Context context, String crashDir) {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl(context.getApplicationContext(), crashDir));
        }

        public void unregister() {
            Thread.setDefaultUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
        }

        public void registerPart(Context context) {
            unregisterPart(context);
            mPartCrashHandler = new PartCrashHandler(context.getApplicationContext());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(mPartCrashHandler);
        }

        private static class PartCrashHandler implements Runnable {
            private final Context mContext;
            public AtomicBoolean isRunning = new AtomicBoolean(true);

            public PartCrashHandler(Context context) {
                this.mContext = context;
            }

            @Override
            public void run() {
                while (isRunning.get()) {
                    try {
                        // 模拟线程工作
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (final Throwable e) {
                        e.printStackTrace();
                        if (isRunning.get()) {
                            MAIN_HANDLER.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
            }
        }

        public void unregisterPart(Context context) {
            if (mPartCrashHandler != null) {
                mPartCrashHandler.isRunning.set(false);
                mPartCrashHandler = null;
            }
        }


        private static class UncaughtExceptionHandlerImpl implements Thread.UncaughtExceptionHandler {

            private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");

            private final Context mContext;

            private final File mCrashDir;

            public UncaughtExceptionHandlerImpl(Context context, String crashDir) {
                this.mContext = context;
                this.mCrashDir = TextUtils.isEmpty(crashDir) ? new File(mContext.getExternalCacheDir(), "crash") : new File(crashDir);
            }

            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    LinkedHashMap<String, String> log = buildLog(throwable);
                    writeLog(log.toString());

                    Intent intent = new Intent(mContext, CrashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_TEXT, log);
                    mContext.startActivity(intent);
                } catch (Throwable e) {
                    Log.e("CrashHandler", "Failed to start CrashActivity", e);
                    writeLog("Failed to start CrashActivity: " + e.getMessage());
                } finally {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }

            private LinkedHashMap<String, String> buildLog(Throwable throwable) {
                String time = DATE_FORMAT.format(new Date());

                String versionName = "unknown";
                long versionCode = 0;
                try {
                    PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    versionName = packageInfo.versionName;
                    versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
                } catch (Throwable ignored) {}

                LinkedHashMap<String, String> head = new LinkedHashMap<>();
                head.put("Time Of Crash", time);
                head.put("Device", String.format("%s, %s", Build.MANUFACTURER, Build.MODEL));
                head.put("Android Version", String.format("%s (%d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                head.put("App Version", String.format("%s (%d)", versionName, versionCode));
                head.put("Kernel", getKernel());
                head.put("Support Abis", Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null ? Arrays.toString(Build.SUPPORTED_ABIS) : "unknown");
                head.put("Fingerprint", Build.FINGERPRINT);
                head.put("err",Log.getStackTraceString(throwable));

                return head;
            }

            private void writeLog(String log) {
                String time = DATE_FORMAT.format(new Date());
                File file = new File(mCrashDir, "crash_" + time + ".txt");
                try {
                    write(file, log.getBytes("UTF-8"));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            private static String getKernel() {
                try {
                    return LuaApplication.toString(new FileInputStream("/proc/version")).trim();
                } catch (Throwable e) {
                    return e.getMessage();
                }
            }
        }
    }

    public static final class CrashActivity extends AppCompatActivity {

        private HashMap<String, String> mLog;
        TextView crashtime,device,androidversion,appversion,kernel,abis,print,err;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(R.style.Theme_Material3_Blue);
            setContentView(R.layout.crash_activity);
            setTitle("App Crash");
            crashtime=findViewById(R.id.crashtime);
            device=findViewById(R.id.device);
            androidversion=findViewById(R.id.androidversion);
            appversion=findViewById(R.id.appversion);
            kernel=findViewById(R.id.kernel);
            abis=findViewById(R.id.abis);
            print=findViewById(R.id.print);
            err=findViewById(R.id.err);
            mLog = (HashMap<String, String>) getIntent().getExtras().get(Intent.EXTRA_TEXT);
            crashtime.setText(mLog.get("Time Of Crash"));
            device.setText(mLog.get("Device"));
            androidversion.setText(mLog.get("Android Version"));
            appversion.setText(mLog.get("App Version"));
            kernel.setText(mLog.get("Kernel"));
            abis.setText(mLog.get("Support Abis"));
            print.setText(mLog.get("Fingerprint"));
            err.setText(mLog.get("err"));
            /*SpannableString spannableString = new SpannableString(mLog);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, mLog.indexOf("\n"), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            ScrollView contentView = new ScrollView(this);
            contentView.setFillViewport(true);

            TextView textView = new TextView(this);
            int padding = dp2px(16);
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(spannableString);
            textView.setTextIsSelectable(true);
            textView.setTypeface(Typeface.DEFAULT);
            textView.setLinksClickable(true);

            contentView.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            */
        }

        private void restart() {
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        private static int dp2px(float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, android.R.id.copy, 0, android.R.string.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.copy:
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog.toString()));
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void onBackPressed() {
            restart();
        }
    }

} 



