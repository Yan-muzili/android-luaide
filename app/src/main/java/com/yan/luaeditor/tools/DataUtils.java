package com.yan.luaeditor.tools;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataUtils {
    private File openfile;
    public void open(String date,String path){
        File file=new File(date+path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        this.openfile=file;
    }

    public byte[] read() {
        if (!openfile.exists() || openfile.isDirectory()) {
            return null;
        }
        ByteArrayOutputStream bos = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(openfile);
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public boolean write(String data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(openfile);
            fos.write(data.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
