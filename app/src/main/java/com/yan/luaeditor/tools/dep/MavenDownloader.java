package com.yan.luaeditor.tools.dep;

import android.content.Context;
import androidx.annotation.WorkerThread;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MavenDownloader {

    private final PomDownloader resolver = new PomDownloader();

    @WorkerThread
    public List<File> downloadSync(Context ctx, String coordinate) throws IOException {
        return resolver.downloadTransitive(ctx, coordinate);
    }
}