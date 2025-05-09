package com.yan.luaide;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

import static java.io.File.separator;

import com.Day.Studio.Function.ApkXmlEditor;
import com.Day.Studio.Function.axmleditor.decode.AXMLDoc;
import com.mcal.apksigner.ApkSigner;
import com.yan.luaeditor.tools.PermissionEditor;
import com.yan.luaeditor.tools.PermissionTagEditor;
import com.yan.luaeditor.tools.ProviderEditor;

public class LuaUtil {
    /**
     * 截屏
     *
     * @param activity
     * @return
     */
    public static Bitmap captureScreen(Activity activity) {
// 获取屏幕大小：
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager WM = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = WM.getDefaultDisplay();
        display.getMetrics(metrics);
        int height = metrics.heightPixels; // 屏幕高
        int width = metrics.widthPixels; // 屏幕的宽
// 获取显示方式
        int pixelformat = display.getPixelFormat();
        PixelFormat localPixelFormat1 = new PixelFormat();
        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
        int deepth = localPixelFormat1.bytesPerPixel;// 位深
        byte[] piex = new byte[height * width * deepth];
        try {
            Runtime.getRuntime().exec(
                    new String[]{"/system/bin/su", "-c",
                            "chmod 777 /dev/graphics/fb0"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
// 获取fb0数据输入流
            InputStream stream = new FileInputStream(new File(
                    "/dev/graphics/fb0"));
            DataInputStream dStream = new DataInputStream(stream);
            dStream.readFully(piex);
        } catch (Exception e) {
            e.printStackTrace();
        }
// 保存图片
        int[] colors = new int[height * width];
        for (int m = 0; m < colors.length; m++) {
            int r = (piex[m * 4] & 0xFF);
            int g = (piex[m * 4 + 1] & 0xFF);
            int b = (piex[m * 4 + 2] & 0xFF);
            int a = (piex[m * 4 + 3] & 0xFF);
            colors[m] = (a << 24) + (r << 16) + (g << 8) + b;
        }
// piex生成Bitmap
        Bitmap bitmap = Bitmap.createBitmap(colors, width, height,
                Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    //读取asset文件

    public static byte[] readAsset(Context context, String name) throws IOException {
        AssetManager am = context.getAssets();
        InputStream is = am.open(name);
        byte[] ret = readAll(is);
        is.close();
        //am.close();
        return ret;
    }

    public static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }

    //复制asset文件到sd卡
    public static void assetsToSD(Context context, String InFileName, String OutFileName) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(OutFileName);
        myInput = context.getAssets().open(InFileName);
        byte[] buffer = new byte[4096];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public static void copyFile(String from, String to) {
        try {
            copyFile(new FileInputStream(from), new FileOutputStream(to));
        } catch (IOException e) {
            Log.i("lua", e.getMessage());
        }
    }

    public static boolean copyFile(InputStream in, OutputStream out) {
        try {
            int byteread = 0;
            byte[] buffer = new byte[4096];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            //in.close();
            //out.close();
        } catch (Exception e) {
            Log.i("lua", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean copyDir(String from, String to) {
        return copyDir(new File(from), new File(to));
    }

    public static boolean copyDir(File from, File to) {
        boolean ret = true;
        File p = to.getParentFile();
        if (!p.exists())
            p.mkdirs();
        if (from.isDirectory()) {
            File[] fs = from.listFiles();
            if (fs != null && fs.length != 0) {
                for (File f : fs)
                    ret = copyDir(f, new File(to, f.getName()));
            } else {
                if (!to.exists())
                    ret = to.mkdirs();
            }
        } else {
            try {
                if (!to.exists())
                    to.createNewFile();
                ret = copyFile(new FileInputStream(from), new FileOutputStream(to));
            } catch (IOException e) {
                Log.i("lua", e.getMessage());
                ret = false;
            }
        }
        return ret;
    }

    public static boolean rmDir(File dir) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs)
                rmDir(f);
        }
        return dir.delete();
    }

    public static void rmDir(File dir, String ext) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs)
                rmDir(f, ext);
            dir.delete();
        }
        if (dir.getName().endsWith(ext))
            dir.delete();
    }

    public static byte[] readZip(String zippath, String filepath) throws IOException {
        ZipFile zip = new ZipFile(zippath);
        ZipEntry entey = zip.getEntry(filepath);
        InputStream is = zip.getInputStream(entey);
        return readAll(is);
    }

// 计算文件的 MD5 值

    public static String getFileMD5(String file) {
        return getFileMD5(new File(file));
    }

    public static String getFileMD5(File file) {
        try {
            return getFileMD5(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String getFileMD5(InputStream in) {
        byte buffer[] = new byte[4096];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // 计算文件的 SHA-1 值
    public static String getFileSha1(String file) {
        return getFileMD5(new File(file));
    }

    public static String getFileSha1(File file) {
        try {
            return getFileSha1(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String getFileSha1(InputStream in) {
        byte buffer[] = new byte[4096];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] getAllName(Context context, String path) {
        ArrayList<String> ret = new ArrayList<>();
        try {
            DexFile dex = new DexFile(context.getPackageCodePath());
            Enumeration<String> cls = dex.entries();
            while (cls.hasMoreElements()) {
                ret.add(cls.nextElement());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ZipFile zip = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ret.add(entries.nextElement().getName().replaceAll("/", ".").replace(".class", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] arr = new String[ret.size()];
        ret.toArray(arr);
        return arr;
    }


    public static void unZip(String SourceDir) throws IOException {
        unZip(SourceDir, new File(SourceDir).getParent(), "");
    }

    public static void unZip(String SourceDir, boolean bool) throws IOException {
        if (!bool) {
            unZip(SourceDir);
            return;
        }
        String name = new File(SourceDir).getName();
        int i = name.lastIndexOf(".");
        if (i > 0) {
            name = name.substring(0, i);
        }
        i = name.indexOf("_");
        if (i > 0) {
            name = name.substring(0, i);
        }
        i = name.indexOf("(");
        if (i > 0) {
            name = name.substring(0, i);
        }
        unZip(SourceDir, new File(SourceDir).getParent() + separator + name, "");
    }

    public static void unZip(String SourceDir, String extDir) throws IOException {
        unZip(SourceDir, extDir, "");
    }

    public static void unZip(String SourceDir, String extDir, String fileExt) throws IOException {
        ZipFile zip = new ZipFile(SourceDir);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith(fileExt))
                continue;
            String path = name;
            if (entry.isDirectory()) {
                File f = new File(extDir + separator + path);
                if (!f.exists())
                    f.mkdirs();
            } else {
                String fname = extDir + separator + path;
                File temp = new File(fname).getParentFile();
                if (!temp.exists()) {
                    if (!temp.mkdirs()) {
                        throw new RuntimeException("create file " + temp.getName() + " fail");
                    }
                }

                FileOutputStream out = new FileOutputStream(extDir + separator + path);
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


    private static final byte[] BUFFER = new byte[4096];

    public static boolean zip(String sourceFilePath) {
        return zip(sourceFilePath, new File(sourceFilePath).getParent());
    }

    public static boolean zip(String sourceFilePath, String zipFilePath) {
        File f = new File(sourceFilePath);
        return zip(sourceFilePath, zipFilePath, f.getName() + ".zip");
    }

    public static boolean zip(String sourceFilePath, String zipFilePath, String zipFileName) {
        boolean result = false;
        File source = new File(sourceFilePath);
        File zipFile = new File(zipFilePath, zipFileName);
        if (!zipFile.getParentFile().exists()) {
            if (!zipFile.getParentFile().mkdirs()) {
                return result;
            }
        }
        if (zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                return result;
            }
        }

        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            //out.setMethod(ZipOutputStream.DEFLATED);
            compress(source, out, "");
            checksum.getChecksum().getValue();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static void compress(File file, ZipOutputStream out, String mainFileName) {
        if (file.isFile()) {
            FileInputStream fi = null;
            BufferedInputStream origin = null;
            try {
                fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER.length);
                //int index=file.getAbsolutePath().indexOf(mainFileName);
                String entryName = mainFileName + file.getName();
                System.out.println(entryName);
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                //			byte[] data = new byte[BUFFER];
                int count;
                while ((count = origin.read(BUFFER, 0, BUFFER.length)) != -1) {
                    out.write(BUFFER, 0, count);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (origin != null) {
                    try {
                        origin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (file.isDirectory()) {
            File[] fs = file.listFiles();
            if (fs != null && fs.length > 0) {
                for (File f : fs) {
                    if (f.isFile())
                        compress(f, out, mainFileName);
                    else
                        compress(f, out, mainFileName + f.getName() + "/");

                }
            }
        }
    }

    public static boolean unZipBase64(String source, String extDir) {
        ByteArrayInputStream zbuf = new ByteArrayInputStream(Base64.decode(source, Base64.NO_WRAP));
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zbuf));
        ZipEntry entry;
        try {
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) {
                    File f = new File(extDir + File.separator + name);
                    if (!f.exists())
                        f.mkdirs();
                } else {
                    String fname = extDir + File.separator + name;
                    File temp = new File(fname).getParentFile();
                    if (!temp.exists()) {
                        if (!temp.mkdirs()) {
                            throw new RuntimeException("create file " + fname + temp.getName() + " fail");
                        }
                    }
                    FileOutputStream out = new FileOutputStream(extDir + File.separator + name);
                    byte[] buf = new byte[8 * 1000];
                    int count = 0;
                    while ((count = zin.read(buf)) != -1) {
                        out.write(buf, 0, count);
                    }
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String zipToBase64(String sourceFilePath) {
        String[] fs = new File(sourceFilePath).list();
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = null;
        try {
            //CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setLevel(9);
            for (String s : fs) {
                compress(new File(sourceFilePath,s), out, "");
            }
            //checksum.getChecksum().getValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Base64.encodeToString(dest.toByteArray(), Base64.NO_WRAP);
    }

    public static boolean zip(String[] sourceFilePath, String zipFilePath, String zipFileName) {
        boolean result = false;
        //File source=new File(sourceFilePath);
        File zipFile = new File(zipFilePath, zipFileName);
        if (!zipFile.getParentFile().exists()) {
            if (!zipFile.getParentFile().mkdirs()) {
                return result;
            }
        }
        if (zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                return result;
            }
        }

        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            //out.setMethod(ZipOutputStream.DEFLATED);
            for (String s : sourceFilePath) {
                compress(new File(s), out, "");
            }
            checksum.getChecksum().getValue();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }



    public static final HashMap<String, String> mFileTypes = new HashMap<String, String>();

    static {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //other
        mFileTypes.put("41433130", "dwg"); // CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); // 日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "docx");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
    }

    public static String getFileType(String path) {
        try {
            return mFileTypes.get(getFileHeader(new FileInputStream(path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }


    public static String getFileType(File file) {
        try {
            return mFileTypes.get(getFileHeader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * 获取文件类型
     * ps:流会关闭
     *
     * @param inputStream
     * @return
     */
    public static String getFileType(InputStream inputStream) {
        return mFileTypes.get(getFileHeader(inputStream));
    }

    public static String getFileHeader(InputStream inputStream) {
        String value = null;
        try {
            byte[] b = new byte[4];
        /*int read() 从此输入流中读取一个数据字节。
        *int read(byte[] b) 从此输入流中将最多 b.length 个字节的数据读入一个 byte 数组中。
        * int read(byte[] b, int off, int len) 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
        */
            inputStream.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    public BitmapDrawable toBlack(String path, float n, int h, int o) {
        Bitmap image = BitmapFactory.decodeFile(path);
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap imageRet = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //int[][] colors = new int[width][height];
        int[] colors = new int[width * height];
        float[] vs = new float[width * height];
        float[] hsv = new float[3];
        float v = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color1 = image.getPixel(x, y);
                Color.colorToHSV(color1, hsv);
                //int v= ((int) (hsv[2]*n))*255/n;
                //float v2=v/10;
                vs[x + width * y] = hsv[2];
                v += hsv[2];
                //imageRet.setPixel(x,y, Color.rgb(v,v,v));
            }
        }
        float vv = v / (width * height) * n;
        int[][] color = new int[width][height];
       /*for (int i=0;i<width*height;i++){
           if(vs[i]>vv)
               colors[i]=-1;
           else
               colors[i]=0xff000000;
       }*/
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = x + width * y;
                if (vs[i] > vv) {
                    colors[i] = -1;
                    color[x][y] = 1;
                } else {
                    colors[i] = 0xff000000;
                    color[x][y] = 0;
                }
            }
        }
        int ret = 0;
        for (int x = width / 2; x < width - 10; x++) {
            for (int y = width / 3; y < width; y++) {
                if (check(x, y, color, h, o)) {
                    ret = x;
                    Log.i("find_color", ret + "");
                    break;
                }
            }

        }
        return new BitmapDrawable(Bitmap.createBitmap(colors, width, height, Bitmap.Config.RGB_565));
    }

    private boolean check(int x, int y, int[][] color, int h, int o) {
        for (int i = 0; i < h; i++) {
            if (!(color[x][y + i] == 1 && color[x + o][y + i] == 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过像素对比来计算偏差值
     *
     * @param path1 原图位置
     * @param path2 滑块图位置
     * @return 偏差值
     */
    public int getDifferenceValue(String path1, String path2) {
        int result = 0;
        File file = new File(path1);
        File file1 = new File(path2);
        try {
            Bitmap image = BitmapFactory.decodeFile(path1);
            Bitmap image1 = BitmapFactory.decodeFile(path2);

            int width = image.getWidth();
            int height = image.getHeight();
            int[][] colors = new int[width][height];
            for (int x = 1; x < width; x++) {
                for (int y = 1; y < height; y++) {
                    int color1 = image.getPixel(x, y);
                    int color2 = image1.getPixel(x, y);
                    if (color1 == color2) {
                        colors[x - 1][y - 1] = 0;
                    } else {
                        colors[x - 1][y - 1] = 1;
                    }
                }
            }
            int min = 999;
            int max = -1;
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[x].length; y++) {
                    if (colors[x][y] == 1) {
                        colors[x][y] = checkPixel(x, y, colors);
                        if (colors[x][y] == 1) {
                            if (x > max) {
                                max = x;
                            } else if (x < min) {
                                min = x;
                            }
                        }
                    }
                }
            }
            result = (max + min) / 2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int checkPixel(int x, int y, int[][] colors) {
        int result = colors[x][y];
        int num = 0;
        if ((y + 30) < colors[x].length) {
            for (int i = 1; i <= 30; i++) {
                int color = colors[x][y + i];
                if (color == 0) {
                    num += 1;
                }
            }
            if (num > 15) {
                return 0;
            }
        }
        return result;
    }

    public static byte[] readAllf(String name) throws IOException {
        InputStream is = new FileInputStream(name);
        byte[] ret = readAll(is);
        is.close();
        return ret;
    }

    public static void save2(String path, String text) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            out.write(text.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static String bin(
                Context context,
                String mRootDir,
                String mDir,
                String aname,
                String pkg,
                String ver,
                String code,
                String amph,
                String[] ps) {
            String[] repomission = {
                    "com.yan.luaide.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
            };
            File bf = new File(mRootDir, "bin");
            bf.mkdirs();
            File op = new File(bf, aname);
            byte[] buffer;
            Log.i("luaj", "bin: " + op);
            try {
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(op));

                File[] fs = new File(mDir).listFiles();
                for (File f : fs) {
                    addZip(zip, f, "assets");
                }

                ZipFile zf = new ZipFile(context.getPackageCodePath());
                Enumeration<? extends ZipEntry> es = zf.entries();
                while (es.hasMoreElements()) {
                    ZipEntry z = es.nextElement();
                    if (z.getName().startsWith("assets")) continue;

                    if (z.getName().equals("resources.arsc")) {
                        InputStream inputStream = zf.getInputStream(z);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        buffer = new byte[4096];
                        int bytesRead;
                        CRC32 crc32 = new CRC32();
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                            crc32.update(buffer, 0, bytesRead);
                        }
                        byte[] fileData = bos.toByteArray();
                        inputStream.close();
                        ZipEntry newEntry = new ZipEntry(z.getName());
                        newEntry.setMethod(ZipOutputStream.STORED);
                        newEntry.setSize(fileData.length);
                        newEntry.setCompressedSize(fileData.length);
                        newEntry.setCrc(crc32.getValue());

                        zip.putNextEntry(newEntry);
                        zip.write(fileData);
                    } else {
                        zip.putNextEntry(new ZipEntry(z.getName()));
                        if (z.getName().equals("AndroidManifest.xml")) {
                            InputStream inputStream = zf.getInputStream(z);
                            if (!new File("/storage/emulated/0/Luaide/Manifest/" + amph).exists()){
                                new File("/storage/emulated/0/Luaide/Manifest/" + amph).mkdir();
                            }
                            OutputStream outputStream = new FileOutputStream("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml");
                            buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.close();
                            inputStream.close();
                            editorXml("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", aname, pkg, Integer.parseInt(code), ver);
                            setProvider("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", pkg);
                            try {
                                for (int i = 0; i < ps.length; i++) {
                                    //System.out.println(ps[i]);
                                    setPermission("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", ps[i], false);
                                }
                                for (int i = 0; i < repomission.length; i++) {
                                    setPermission("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", repomission[i], true);
                                }
                                edpermission("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml",pkg);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            for (int i = 0; i < ps.length; i++) {
                                System.out.println(ps[i]);
                                setPermission("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", ps[i], false);
                            }
                            setPermission("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml", pkg + ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION", false);

                            FileInputStream fis = new FileInputStream(new File("/storage/emulated/0/Luaide/Manifest/" + amph + "/AndroidManifest.xml"));
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = fis.read(bytes)) >= 0) {
                                zip.write(bytes, 0, length);
                            }
                            fis.close();
                        } else {
                            if (z.getName().endsWith("icon.png")) {
                                if (new File(mDir + "/icon.png").exists()) {
                                    LuaUtil.copyFile(new FileInputStream(mDir + "/icon.png"), zip);
                                }else {
                                    LuaUtil.copyFile(zf.getInputStream(z), zip);
                                }
                            } else {
                                LuaUtil.copyFile(zf.getInputStream(z), zip);
                            }
                        }
                    }
                }

                zf.close();
                zip.closeEntry();
                zip.close();
                try {
                    RandomAccessFile zipFile = new RandomAccessFile(op.getAbsolutePath(), "r");
                    FileOutputStream zipOut = new FileOutputStream(op.getAbsolutePath() + "s.apk");
                    com.iyxan23.zipalignjava.ZipAlign.alignZip(zipFile, zipOut, 4, 4096);
                    zipFile.close();
                    zipOut.close();
                    //System.out.println("4k");
                } catch (IOException | com.iyxan23.zipalignjava.InvalidZipException e) {
                    e.printStackTrace();
                }
                //apksigner.Main.sign(op.getAbsolutePath() + "s.apk", op.getAbsolutePath() + ".apk");
                copyAssetFileToExternal(context,"release_key.jks","/storage/emulated/0/Luaide/Manifest/");
                signerApk("/storage/emulated/0/Luaide/Manifest/release_key.jks","hhh12345","yan","hhh12345",op.getAbsolutePath() + "s.apk",op.getAbsolutePath() + ".apk");
                new File(op.getAbsolutePath() + "s.apk").delete();
                Log.i("luaj", "bin: finish " + op);
                return op.getAbsolutePath() + ".apk";
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return "error:" + e.getMessage();
            } finally {
                op.delete();
            }
        }


        private static void addZip( ZipOutputStream zip, File dir, String root) {
        Log.i("luaj", "addZip: " + root + ";" + dir);
        if (dir.getName().startsWith("."))
            return;
        String name = root + "/" + dir.getName();
        if (name.endsWith(".apk"))
            return;
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs) {
                addZip( zip, f, name);
            }
        } else {
            try {
                zip.putNextEntry(new ZipEntry(name));
            } catch (IOException e) {

            }
                try {
                    byte[] b = LuaUtil.readAllf(dir.getAbsolutePath());
                    zip.write(b, 0, b.length);
                    zip.flush();
                } catch (IOException e) {

                }
        }
    }

    public static boolean signerApk(
            String signFilePath,
            String signPassword,
            String keyName,
            String keyWord,
            String apkPath,
            String outApkpath) {
        try {

            final ApkSigner signer = new ApkSigner(new File(apkPath), new File(outApkpath));
            signer.setUseDefaultSignatureVersion(false);
            signer.setV1SigningEnabled(true);
            signer.setV2SigningEnabled(true);
            signer.setV3SigningEnabled(true);
            signer.setV4SigningEnabled(false);
            signer.signRelease(new File(signFilePath), signPassword, keyName, keyWord);
            // signer.signDebug();

            return true;
        } catch (Exception e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    public static void copyAssetFileToExternal(Context context, String assetFileName, String targetDir) {
        try {
            InputStream inputStream = context.getAssets().open(assetFileName);
            File targetDirFile = new File(targetDir);
            if (!targetDirFile.exists()) {
                targetDirFile.mkdirs();
            }
            File outputFile = new File(targetDir, assetFileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Boolean editorXml(
            String filePath, String apkName, String packageName, int versionId, String versionName) {
        try {
            ApkXmlEditor.setXmlPaht(filePath);
            ApkXmlEditor.setAppName(apkName);
            ApkXmlEditor.setAppPack(packageName);
            ApkXmlEditor.setAppbcode(versionId);
            ApkXmlEditor.setAppbname(versionName);
            ApkXmlEditor.operation();
            return true;
        } catch (Exception err) {

            return false;
        }
    }

    public static void setPermission(String path, String permission, boolean type) {
        try {
            File file = new File(path);
            AXMLDoc aXMLDoc = new AXMLDoc();
            Object object = new FileInputStream(file);
            aXMLDoc.parse((InputStream) object);
            PermissionEditor permissionEditor = new PermissionEditor(aXMLDoc);
            PermissionEditor.EditorInfo editorInfo = new PermissionEditor.EditorInfo();
            PermissionEditor.PermissionOpera permissionOpera;
            if (type) {
                permissionOpera = new PermissionEditor.PermissionOpera(permission).remove();
            } else {
                permissionOpera = new PermissionEditor.PermissionOpera(permission).add();
            }
            editorInfo.with(permissionOpera);
            permissionEditor.setEditorInfo(editorInfo);
            permissionEditor.commit();
            object = new FileOutputStream(file);
            aXMLDoc.build((OutputStream) object);
            aXMLDoc.release();
            ((OutputStream) object).flush();
            ((OutputStream) object).close();
        } catch (Exception e) {

        }

    }

    public static void setProvider(String path, String pkg) {
        try {
            File file = new File(path);
            AXMLDoc aXMLDoc = new AXMLDoc();
            Object object = new FileInputStream(file);
            aXMLDoc.parse((InputStream) object);
            ProviderEditor.EditorInfo editorInfo = new ProviderEditor.EditorInfo();
            editorInfo.with(new ProviderEditor.ProviderOpera(
                    "androidx.startup.InitializationProvider",
                    pkg+".androidx-startup"
            ).update());
            editorInfo.with(new ProviderEditor.ProviderOpera(
                    "android.content.FileProvider",
                    pkg+".provider"
            ).update());
            ProviderEditor providerEditor = new ProviderEditor(aXMLDoc);
            providerEditor.setEditorInfo(editorInfo);

            providerEditor.commit();
            object = new FileOutputStream(file);
            aXMLDoc.build((OutputStream) object);
            aXMLDoc.release();
            ((OutputStream) object).close();
            ((OutputStream) object).flush();
            //System.out.println("cg");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void edpermission(String path,String pkg){
        try {
            AXMLDoc aXMLDoc = new AXMLDoc();
            aXMLDoc.parse(new FileInputStream(path));

            PermissionTagEditor.EditorInfo editorInfo = new PermissionTagEditor.EditorInfo();
            PermissionTagEditor.PermissionOperation addOperation = new PermissionTagEditor.PermissionOperation(pkg+".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION").add();
            PermissionTagEditor.PermissionOperation removeOperation = new PermissionTagEditor.PermissionOperation("com.yan.luaide.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION").remove();
            editorInfo.with(addOperation).with(removeOperation);

            PermissionTagEditor editor = new PermissionTagEditor(aXMLDoc);
            editor.setEditorInfo(editorInfo);
            editor.commit();

            aXMLDoc.build(new java.io.FileOutputStream(path));
            aXMLDoc.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetsFolder(Context context, String assetsFolderName, String destinationPath) {
        AssetManager assetManager = context.getAssets();
        String[] files;
        try {
            // 获取assets文件夹下的所有文件和文件夹
            files = assetManager.list(assetsFolderName);
            // 创建目标文件夹
            File destinationFolder = new File(destinationPath);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }
            // 遍历文件和文件夹进行复制
            for (String file : files) {
                String assetPath = assetsFolderName + "/" + file;
                String destinationFile = destinationPath + "/" + file;
                if (assetManager.list(assetPath).length == 0) {
                    // 如果是文件，调用复制文件方法
                    copyFile(assetManager, assetPath, destinationFile);
                } else {
                    // 如果是文件夹，递归调用复制文件夹方法
                    copyAssetsFolder(context, assetPath, destinationFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 复制单个文件
    private static void copyFile(AssetManager assetManager, String assetPath, String destinationPath) {
        try (InputStream in = assetManager.open(assetPath);
             OutputStream out = new FileOutputStream(destinationPath)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer))!= -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static void createImage(int width, int height, int ints[][], String name) throws IOException {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphic = bi.createGraphics();
        graphic.setColor(new Color(0x003D1CFF));
        graphic.fillRect(0, 0, width, height);
        for (int x = 0; x < ints.length; x++) {
            for (int y = 0; y < ints[x].length; y++) {
                if (ints[x][y] == 1) {
                    bi.setRGB(x, y, 0xFF7F2E);
                }
            }
        }
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = it.next();
        File f = new File("c://" + name + ".png");
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        writer.write(bi);
    }*/


}
