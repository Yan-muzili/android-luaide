package com.yan.luaeditor.tools.apk;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.tools.r8.*;
import com.android.tools.r8.origin.Origin;
import com.yan.luaide.R;

import java.io.*;
import java.nio.file.*;
import java.util.*;


@RequiresApi(api = android.os.Build.VERSION_CODES.O)
public final class ApkTools {


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean jar2dex(Context context, String inputJar, String outputDexFile) {
        try {
            Path outDir = Paths.get(outputDexFile).getParent();
            Files.createDirectories(outDir);

            D8Command cmd = D8Command.builder()
                    .addProgramFiles(Paths.get(inputJar))
                    .setOutput(outDir, OutputMode.DexIndexed)
                    .setMinApiLevel(27)
                    .build();

            D8.run(cmd);

            Path classesDex = outDir.resolve("classes.dex");
            if (Files.notExists(classesDex)) {
                return true;
            }

            Files.move(classesDex, Paths.get(outputDexFile), StandardCopyOption.REPLACE_EXISTING);
            Log.i("Jar2Dex", "生成: " + outputDexFile);
            return true;
        } catch (Exception e) {

            return false;
        }

    }
}