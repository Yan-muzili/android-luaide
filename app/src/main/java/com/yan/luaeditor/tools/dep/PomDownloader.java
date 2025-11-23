package com.yan.luaeditor.tools.dep;

import android.content.Context;
import android.util.Xml;

import androidx.annotation.WorkerThread;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class PomDownloader {

    private final List<String> repos = Arrays.asList(
            "https://maven.google.com/",
            "https://jitpack.io/",
            "https://repo1.maven.org/maven2/"


    );

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface DownloadListener {
        void onProgress(String fileName, long current, long total);
        void onFileFinished(String fileName);
        void onTotalProgress(String string,int finished, int total);
    }

    private DownloadListener listener;
    public void setDownloadListener(DownloadListener l) { this.listener = l; }

    private final Set<String> visited = new HashSet<>();
    private final List<File> result = new ArrayList<>();
    private int totalFiles   = 0;
    private int finishedFiles= 0;

    @WorkerThread
    public List<File> downloadTransitive(Context ctx, String rootCoordinate) throws IOException {
        File dir = new File("/sdcard/Luaide");
        if (!dir.exists()) dir.mkdirs();

        visited.clear();
        totalFiles = 0;
        countDependencies(rootCoordinate, dir);

        visited.clear();
        finishedFiles = 0;
        downloadRecursive(rootCoordinate, dir);
        return result;
    }

    /* --------------------------------------------------------------------- */
    private void countDependencies(String coord, File storeDir) throws IOException {
        if (!visited.add(coord)) return;
        String[] gav = coord.split(":");
        if (gav.length < 3) return;
        totalFiles++;                      // 自己
        String group = gav[0], artifact = gav[1], version = gav[2];
        String path  = group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version;
        File pomFile = new File(storeDir, artifact + "-" + version + ".pom");
        if (downloadFromRepos(path, pomFile, "pom")) {
            Map<String,String> props = parseProperties(pomFile);          // 属性表
            List<String> deps = parsePomDependencies(pomFile, props);
            for (String d : deps) countDependencies(d, storeDir);
        }
    }

    /* --------------------------------------------------------------------- */
    private void downloadRecursive(String coord, File storeDir) throws IOException {
        if (!visited.add(coord)) return;
        String[] gav = coord.split(":");
        if (gav.length < 3) throw new IOException("非法坐标:" + coord);
        String group = gav[0], artifact = gav[1], version = gav[2];
        String path  = group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version;

        String ext = "aar";

        File artifactFile = new File(storeDir, artifact + "-" + version + "." + ext);
        boolean ok = downloadFromRepos(path, artifactFile, ext);
        if (!ok && !ext.equals("jar")) {
            ext = "jar";
            artifactFile = new File(storeDir, artifact + "-" + version + "." + ext);
            ok = downloadFromRepos(path, artifactFile, ext);
        }
        if (!ok) throw new IOException("所有仓库均找不到 " + coord + " 的 AAR/JAR");
        result.add(artifactFile);
        finishedFiles++;
        if (listener != null) listener.onTotalProgress(coord,finishedFiles, totalFiles);

        /* 2. POM 继续 */
        File pomFile = new File(storeDir, artifact + "-" + version + ".pom");
        boolean pomOk = downloadFromRepos(path, pomFile, "pom");
        if (!pomOk) throw new IOException("所有仓库均找不到 POM: " + coord);
        Map<String,String> props = parseProperties(pomFile);
        List<String> deps = parsePomDependencies(pomFile, props);
        for (String d : deps) downloadRecursive(d, storeDir);
        if (pomFile.exists()) pomFile.delete();
    }

    /* --------------------------------------------------------------------- */
    private boolean downloadFromRepos(String path, File saveTo, String ext) throws IOException {
        for (String repo : repos) {
            String url = repo + path + "." + ext;
            if (downloadFile(url, saveTo)) return true;
        }
        return false;
    }

    private boolean downloadFile(String url, File saveTo) throws IOException {
        if (saveTo.exists() && saveTo.length() > 0) return true;
        Request req = new Request.Builder().url(url).build();
        try (Response resp = HTTP.newCall(req).execute()) {
            if (!resp.isSuccessful()) return false;
            File parent = saveTo.getParentFile();
            if (!parent.exists()) parent.mkdirs();
            long total = resp.body().contentLength(), current = 0;
            String name = saveTo.getName();
            try (ResponseBody body = resp.body();
                 InputStream in = body.byteStream();
                 FileOutputStream out = new FileOutputStream(saveTo)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    current += len;
                    if (listener != null && total > 0) listener.onProgress(name, current, total);
                }
            }
            if (listener != null) listener.onFileFinished(name);
            return true;
        }
    }

    private Map<String,String> parseProperties(File pom) throws IOException {
        Map<String,String> map = new HashMap<>();
        try (FileInputStream in = new FileInputStream(pom)) {
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(in, "UTF-8");
            int event;
            while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && "properties".equals(xpp.getName())) {
                    parsePropertySection(xpp, map);
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            throw new IOException("解析 properties 失败", e);
        }
        return map;
    }

    private void parsePropertySection(XmlPullParser xpp, Map<String,String> map) throws XmlPullParserException, IOException {
        int event;
        String tag, text;
        while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                tag = xpp.getName();
                text = readText(xpp);
                if (text != null && !text.isEmpty()) map.put(tag, text);
            } else if (event == XmlPullParser.END_TAG && "properties".equals(xpp.getName())) {
                break;
            }
        }
    }

    private List<String> parsePomDependencies(File pom, Map<String,String> props) throws IOException {
        List<String> list = new ArrayList<>();
        try (FileInputStream in = new FileInputStream(pom)) {
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(in, "UTF-8");
            int event;
            String g = null, a = null, v = null, scope = null;
            boolean insideDeps = false;
            while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    if ("dependencies".equals(tag)) insideDeps = true;
                    if (!insideDeps) continue;
                    if ("dependency".equals(tag)) { g = null; a = null; v = null; scope = null; }
                    else if ("groupId".equals(tag)) g = resolve(readText(xpp), props);
                    else if ("artifactId".equals(tag)) a = resolve(readText(xpp), props);
                    else if ("version".equals(tag)) v = resolve(readText(xpp), props);
                    else if ("scope".equals(tag)) scope = readText(xpp);
                } else if (event == XmlPullParser.END_TAG && "dependency".equals(xpp.getName())) {
                    if (g != null && a != null && v != null) {
                        if (scope == null || "compile".equals(scope) || "runtime".equals(scope))
                            list.add(g + ":" + a + ":" + v);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new IOException("POM 解析失败", e);
        }
        return list;
    }

    private String resolve(String raw, Map<String,String> props) {
        if (raw == null) return null;
        raw = raw.replaceAll("[\\[\\]{}]", "");
        int start;
        while ((start = raw.indexOf("${")) >= 0) {
            int end = raw.indexOf("}", start);
            if (end == -1) break;
            String key = raw.substring(start + 2, end);
            String val = props.get(key);
            if (val == null) val = "";
            raw = raw.substring(0, start) + val + raw.substring(end + 1);
        }
        return raw.isEmpty() ? null : raw;
    }

    private String readText(XmlPullParser xpp) throws IOException, XmlPullParserException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int e = xpp.next();
            if (e == XmlPullParser.TEXT) sb.append(xpp.getText());
            else if (e == XmlPullParser.END_TAG) break;
        }
        return sb.toString().trim();
    }
}