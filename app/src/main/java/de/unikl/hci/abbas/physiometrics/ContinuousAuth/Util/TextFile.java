package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util;

/**
 * Created by abbas on 1/1/18.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TextFile {

    public static void writeFile(String fileName, String content, boolean isAdd) throws IOException {
        try {
            FileOutputStream fout = new FileOutputStream(fileName, isAdd);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] temp = new byte[length];
            fin.read(temp);
            res = new String(temp, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void makeRootDirectory(String filePath) {
        File file;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] listFile(String rootPath) {
        File[] files = new File(rootPath + "/").listFiles();
        String[] result = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            result[i] = rootPath + "/" + files[i].getName();
        }
        return result;
    }
}
