package de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel;

/**
 * Created by abbas on 1/1/18.
 * Principal Component Analysis
 * PCA class:
 * 1.
 * 2.
 * Principal Component Analysis
 * PCA class:
 * 1.
 * 2.
 */

/** Principal Component Analysis
 *  PCA class:
 *  1.
 *  2.
 */

import Jama.Matrix;

public class PCA {

    public static double[][] getResult(double[][] rawData) {

        PCA pca = new PCA();

        double[][] standard = pca.standardizing(rawData);
        double[][] association = pca.coefficientOfAssociation(standard);
        double[][] flagValue = pca.flagValue(association);
        double[][] flagVector = pca.flagVector(association);
        int[] flag = pca.selectPrincipalComponent(flagValue);
        double[][] principalComponent = pca.principalComponent(flagVector, flag);

        Matrix A = new Matrix(rawData);
        Matrix B = new Matrix(principalComponent);
        Matrix C = A.times(B);

        return C.getArray();
    }

    public double[][] standardizing(double[][] x) {

        int n = x.length;
        int m = x[0].length;
        double[] mean = new double[m];
        double[] standardDeviation = new double[m];
        double[][] result = new double[n][m];

        for (int j = 0; j < m; ++j) {
            double sum = 0;
            for (double[] vector : x) {
                sum = sum + vector[j];
            }
            mean[j] = sum / n;
        }

        for (int j = 0; j < m; ++j) {
            double sum = 0;
            for (double[] vector : x) {
                sum = sum + (vector[j] - mean[j]) * (vector[j] - mean[j]);
            }
            standardDeviation[j] = Math.sqrt(sum / (n - 1));
        }

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                result[i][j] = (x[i][j] - mean[j]) / standardDeviation[j];
            }
        }
        return result;

    }

    public double[][] coefficientOfAssociation(double[][] x) {
        int n = x.length;
        int m = x[0].length;
        double[][] result = new double[m][m];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < m; ++j) {
                double temp = 0;
                for (double[] vector : x) {
                    temp = temp + vector[i] * vector[j];
                }
                result[i][j] = temp / (n - 1);
            }
        }
        return result;
    }

    public double[][] flagValue(double[][] x) {
        return new Matrix(x).eig().getD().getArray();
    }

    public double[][] flagVector(double[][] x) {
        return new Matrix(x).eig().getV().getArray();
    }

    public int[] selectPrincipalComponent(double[][] x) {

        int n = x.length;
        double[] a = new double[n];
        int[] result = new int[n];
        int k = 0;
        double temp;
        int m = 0;
        double total = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i == j) {
                    a[k] = x[i][j];
                }
            }
            ++k;
        }
        double[] temp1 = new double[n];
        System.arraycopy(a, 0, temp1, 0, n);
        for (int i = 0; i < n; ++i) {
            temp = temp1[i];
            for (int j = i; j < n; ++j) {
                if (temp <= temp1[j]) {
                    temp = temp1[j];
                    temp1[j] = temp1[i];
                }
                temp1[i] = temp;
            }
        }
        for (int i = 0; i < n; ++i) {
            temp = a[i];
            for (int j = 0; j < n; ++j) {
                if (a[j] >= temp) {
                    temp = a[j];
                    k = j;
                }
                result[m] = k;
            }
            a[k] = -1000;
            m++;
        }
        for (int i = 0; i < n; ++i) {
            total += temp1[i];
        }
        int sum = 1;
        temp = temp1[0];
        for (int i = 0; i < n; ++i) {
            if (temp / total <= 0.9) {
                temp += temp1[i + 1];
                ++sum;
            }
        }
        int[] end = new int[sum];
        System.arraycopy(result, 0, end, 0, sum);
        return end;
    }

    public double[][] principalComponent(double[][] x, int[] y) {

        double[][] result = new double[x.length][y.length];
        int k = y.length - 1;
        for (int i = 0; i < y.length; ++i) {
            for (int j = 0; j < x.length; ++j) {
                result[j][i] = x[j][y[k]];
            }
            --k;
        }
        return result;
    }
}
