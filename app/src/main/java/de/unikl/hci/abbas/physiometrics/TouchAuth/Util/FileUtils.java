package de.unikl.hci.abbas.physiometrics.TouchAuth.Util;

/**
 * Created by abbas on 12/31/17.
 */

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.FeatureVector;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData.TouchEvents;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean read_negative_num_state = false;
    public static boolean read_positive_num_state = false;
    public static String FILE_FEATURE_NUM_NAME = Environment.getExternalStorageDirectory() + "/Auth/Touch/feature_num.txt";
    public static String FILE_NEGATIVE_FEATURE_NUM_NAME = Environment.getExternalStorageDirectory() + "/Auth/Touch/negativefeature_num.txt";
    public static String FILE_FEATUREVECTURE_NAME = Environment.getExternalStorageDirectory() + "/Auth/Touch/positive_user_fv.csv";
    public static String FILE_NEGATIVE_FEATURE_NAME = Environment.getExternalStorageDirectory() + "/Auth/Touch/negative_user.csv";


    public static String TAG = "FileUtils";

    public static void writeFeatureVector(String fileName, FeatureVector fv) {
        Log.i("TAG", "writeFeatureVector file:" + fileName);
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName, true), '\t');
            String[] strArr = new String[fv.size()];
            int i = 0;
            for (; i < fv.size(); ++i) {
                String str = String.format("%.2f", fv.get(i));
                strArr[i] = str;
            }
            writer.writeNext(strArr);
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "An exception occurs when the feature vector is written");
            e.printStackTrace();
        }
    }

    public static List<FeatureVector> readFeatureVectors(String fileName) {
        Log.i("TAG", "readFeatureVectors file:" + fileName);
        List<FeatureVector> listFV = new ArrayList<>();
        try {
            int classlabel = -1;
            CSVReader reader = new CSVReader(new FileReader(fileName), '\t');
            String[] strs = reader.readNext();
            if (fileName.contains("positive"))
                classlabel = 1;
            List<String[]> list = reader.readAll();
            for (String[] ss : list) {
                FeatureVector fv = new FeatureVector(TouchEvents.NUM_FEATURES);
                fv.setClassLabel(classlabel);
                for (int i = 0; i < ss.length; ++i) {
                    if (null != ss[i] && !ss[i].equals(""))
                        fv.set(i, Double.parseDouble(ss[i]));
                }
                listFV.add(fv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listFV;
    }

    public static void writeFeatureNum(String filename, int number) {
        Log.i("FileUtils", "writeFeatureNum file:" + filename);
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(filename, false);
            writer.write(String.valueOf(number));
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "An error occurred while writing the number of feature vectors");
            e.printStackTrace();
        }
    }

    public static int readFeatureNum(String filename) {
        //  Log.i("TAG","readFeatureNum file:"+filename);
        int num = 0;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                /*
                if (filename.contains("negative")) {
                    read_negative_num_state = true;
                }
                else {
                    read_positive_num_state = true;
                }
                */
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                String numstr = reader.readLine();
                if (numstr != null) {
                    numstr = numstr.trim();
                }
                if (!numstr.equals("")) {
                    num = Integer.parseInt(numstr);
                } else {
                    num = 0;
                }
                reader.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "There was an error reading the number of feature vectors");
            e.printStackTrace();
        }
        return num;
    }

    public static void writeFile(String fileName, String content, boolean isAdd) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName, isAdd);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String fileName) {
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

    public static void writeFileFromNums(String filename, double[][] nums, boolean isAdd) {
        int size = nums.length;
        if (size < 1) return;
        if (isAdd) {
            writeFileFromNums(filename, nums[0], true, false, -1);
        } else {
            writeFileFromNums(filename, nums[0], false, false, -1);
        }
        for (int i = 1; i < size; ++i) {
            writeFileFromNums(filename, nums[i], true, false, -1);
        }
    }

    public static void writeFileFromNums(String filename, double[] nums, boolean isAdd, boolean isSvmMode, int label) {

        StringBuilder sb = new StringBuilder("");
        if (isSvmMode) {
            sb.append(label > 0 ? "+" + label : label).append("\t");
            for (int i = 0; i < nums.length; ++i) {
                sb.append(i + 1).append(":").append(nums[i]).append("\t");
            }
        } else {
            for (double num : nums) {
                sb.append(num).append("\t");
            }
        }
        sb.append("\n");
        writeFile(filename, sb.toString(), isAdd);
    }

    public static void rootPermission(String... command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            if (command != null && command.length > 0) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        process.getOutputStream()));
                for (int i = 0; i < command.length; i++) {
                    writer.write(command[i]);
                }
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static double[][] readFileToMatrix(String filename) {

        String rawString = readFile(filename);
        String[] lines = rawString.split("\n");
        double[][] vectors = new double[lines.length][lines[0].split("\t").length];
        for (int i = 0; i < lines.length; ++i) {
            String[] items = lines[i].split("\t");
            for (int j = 0; j < items.length; ++j) {
                vectors[i][j] = Double.parseDouble(items[j]);
            }
        }
        return vectors;
    }
}
