package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util;

/**
 * Created by abbas on 1/1/18.
 */

import java.util.Objects;

public class DataProcessing {

    private static int VECTORS_PER_SECOND = 50;
    private static int VECTORS_PER_WINDOW = 100;
    private static double STEP_OFFSET = 0.5;
    private double[][] accelerometer;
    private double[][] orientation;
    private double[][] magnetic;
    private double[][] gyroscope;
    private Window[] windows;

    public static double[][] clean(double[][] vectors) {
        int N = vectors.length;
        int M = vectors[0].length;
        int k = 0;
        boolean[] flag = new boolean[N];
        for (int i = 0; i < N; ++i) {
            if (hasInfiniteOrNaN(vectors[i])) {
                flag[i] = false;
            } else {
                flag[i] = true;
                ++k;
            }
        }
        double[][] newVectors = new double[k][M];
        k = 0;
        for (int i = 0; i < N; ++i) {
            if (flag[i]) {
                System.arraycopy(vectors[i], 0, newVectors[k], 0, M);
                ++k;
            }
        }
        return newVectors;
    }

    public static boolean hasInfiniteOrNaN(double[] vector) {
        for (double value : vector) {
            if (Double.isInfinite(value) || Double.isNaN(value)) {
                return true;
            }
        }
        return false;
    }

    public static double[][] standardizing(double[][] vectors) {

        int N = vectors.length;
        int M = vectors[0].length;

        double[] mean = new double[M];
        double[] standardDeviation = new double[M];
        double[][] result = new double[N][M];

        for (int j = 0; j < M; ++j) {
            double sum = 0;
            for (double[] vector : vectors) {
                sum = sum + vector[j];
            }
            mean[j] = sum / N;
        }

        for (int j = 0; j < M; ++j) {
            double sum = 0;
            for (double[] vector : vectors) {
                sum = sum + (vector[j] - mean[j]) * (vector[j] - mean[j]);
            }
            standardDeviation[j] = Math.sqrt(sum / N);
        }

        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < M; ++j) {
                result[i][j] = (vectors[i][j] - mean[j]) / standardDeviation[j];
            }
        }
        return result;

    }

    public static void quickSort(double s[], int l, int r) {
        if (l < r) {
            int i = l, j = r;
            double x = s[l];
            while (i < j) {
                while (i < j && s[j] >= x) {
                    j--;
                }
                if (i < j) {
                    s[i++] = s[j];
                }
                while (i < j && s[i] < x) {
                    i++;
                }
                if (i < j) {
                    s[j--] = s[i];
                }
            }
            s[i] = x;
            quickSort(s, l, i - 1);
            quickSort(s, i + 1, r);
        }
    }

    public void readData(int k, String fileName, double seconds) {
        int total = (int) (VECTORS_PER_SECOND * seconds);

        String rawString = "";
        try {
            rawString = TextFile.readFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] lines = rawString.split("\r");

        switch (k) {
            case 0:
                accelerometer = new double[4][total];
                break;
            case 1:
                orientation = new double[4][total];
                break;
            case 2:
                magnetic = new double[4][total];
                break;
            case 3:
                gyroscope = new double[4][total];
        }

        for (int i = 0; i < lines.length && i < total; ++i) {
            String[] values = lines[i].split("\t");
            for (int j = 0; j < values.length; ++j) {
                if (!Objects.equals(values[j], "")) {
                    switch (k) {
                        case 0:
                            accelerometer[j][i] = Double.parseDouble(values[j]);
                            break;
                        case 1:
                            orientation[j][i] = Double.parseDouble(values[j]);
                            break;
                        case 2:
                            magnetic[j][i] = Double.parseDouble(values[j]);
                            break;
                        case 3:
                            gyroscope[j][i] = Double.parseDouble(values[j]);
                    }
                }
            }
        }
    }

    public void readData(String fileNameAcc, String fileNameOri, String fileNameMag, String fileNameGyr, double seconds) {
        readData(0, fileNameAcc, seconds);
        readData(1, fileNameOri, seconds);
        readData(2, fileNameMag, seconds);
        readData(3, fileNameGyr, seconds);
    }

    public Window[] getWindows(double totalTime, double windowTime) {
        VECTORS_PER_WINDOW = (int) (VECTORS_PER_SECOND * windowTime);
        int k = (int) ((totalTime * VECTORS_PER_SECOND - STEP_OFFSET *
                VECTORS_PER_WINDOW) / ((1 - STEP_OFFSET) * VECTORS_PER_WINDOW));
        windows = new Window[k];
        int p = 0;
        for (int i = 0; i < k; ++i) {
            double[][] tempAcc = new double[4][VECTORS_PER_WINDOW];
            double[][] tempOri = new double[4][VECTORS_PER_WINDOW];
            double[][] tempMag = new double[4][VECTORS_PER_WINDOW];
            double[][] tempGyr = new double[4][VECTORS_PER_WINDOW];
            for (int j = 0; j < VECTORS_PER_WINDOW; ++j) {
                for (int m = 0; m < 4; ++m) {
                    if (p >= accelerometer[m].length) {
                        break;
                    } else {
                        tempAcc[m][j] = accelerometer[m][p];
                        tempOri[m][j] = orientation[m][p];
                        tempMag[m][j] = magnetic[m][p];
                        tempGyr[m][j] = gyroscope[m][p];
                    }
                }
                ++p;
            }
            windows[i] = new Window(tempAcc, tempOri, tempMag, tempGyr);
            p = (int) (p - STEP_OFFSET * VECTORS_PER_WINDOW + 1);
        }
        return windows;
    }

    public double[][] getAllFeatureVectors() {
        int N = windows.length;
        int M = windows[0].getFeatureVector().length;
        double[][] ans = new double[N][M];
        for (int i = 0; i < N; ++i) {
            double[] temp = windows[i].getFeatureVector();
            System.arraycopy(temp, 0, ans[i], 0, M);
        }
        return ans;
    }
}
