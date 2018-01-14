package de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel;

/**
 * Created by abbas on 1/1/18.
 */

public class NGramModel {

    public static int N = 5;
    private static int M = 200;
    private int[] dictionary;
    private double[][] model;

    public NGramModel(int n, double[][] centroids, double[][] featureVectors) {
        N = n;
        M = centroids.length;
        dictionary = buildDictionary(centroids, featureVectors);
        model = buildModel();
    }

    public NGramModel(int n, int m, int[] mark) {
        N = n;
        M = m;
        dictionary = mark;
        model = buildModel();
    }

    public NGramModel(double[][] newModel) {
        if (newModel.length == newModel[0].length) {
            model = newModel;
        }
    }

    public static int[] buildDictionary(double[][] centroids, double[][] featureVectors) {

        int[] rawDict = new int[featureVectors.length];

        for (int i = 0; i < featureVectors.length; ++i) {
            rawDict[i] = findNearestCentroid(featureVectors[i], centroids);
        }

        return rawDict;
    }

    public static int findNearestCentroid(double[] element, double[][] centroids) {
        int minimumIndex = 0;
        double minimumDistance = Double.MAX_VALUE;
        for (int i = 0; i < centroids.length; ++i) {
            double distance = distance(element, centroids[i]);
            if (minimumDistance > distance) {
                minimumDistance = distance;
                minimumIndex = i;
            } else if (minimumDistance == distance && Math.random() < 0.5) {
                minimumIndex = i;
            }
        }
        return minimumIndex;
    }

    public static double distance(double[] a, double[] b) {
        if (a.length != b.length) {
            return -1;
        } else {
            double tempSum = 0;
            for (int i = 0; i < a.length; ++i) {
                tempSum = tempSum + (a[i] - b[i]) * (a[i] - b[i]);
            }
            return Math.sqrt(tempSum);
        }
    }

    public static int[] buildLdaDictionary(double[][] featureVectors, LDA lda) {

        int[] rawDict = new int[featureVectors.length];

        for (int i = 0; i < featureVectors.length; ++i) {
            rawDict[i] = lda.predict(featureVectors[i]);
        }

        return rawDict;
    }

    public static double getLength(double[] vector) {
        double tempSum = 0;
        for (double value : vector) {
            tempSum += (value * value);
        }
        return Math.sqrt(tempSum);
    }

    public static void rankLength(double[][] vectors, int left, int right) {
        if (left < right) {
            int i = left, j = right;
            double x = getLength(vectors[left]);
            double[] xVector = new double[vectors[left].length];
            System.arraycopy(vectors[left], 0, xVector, 0, xVector.length);
            while (i < j) {
                while (i < j && getLength(vectors[j]) <= x) {
                    j--;
                }
                if (i < j) {
                    System.arraycopy(vectors[j], 0, vectors[i++], 0, vectors[j].length);
                }

                while (i < j && getLength(vectors[i]) > x) {
                    i++;
                }
                if (i < j) {
                    System.arraycopy(vectors[i], 0, vectors[j--], 0, vectors[i].length);
                }
            }
            System.arraycopy(xVector, 0, vectors[i], 0, vectors[i].length);
            rankLength(vectors, left, i - 1);
            rankLength(vectors, i + 1, right);
        }
    }

    public int count(int a, int b) {
        int ans = 0;
        for (int i = 0; i < dictionary.length - 1; ++i) {
            if (dictionary[i] == a && dictionary[i + 1] == b) {
                ++ans;
            }
        }
        return ans;
    }

    public int count(int a) {
        int ans = 0;
        for (int x : dictionary) {
            if (a == x) {
                ++ans;
            }
        }
        return ans;
    }

    public double[][] buildModel() {
        double[][] ans = new double[M][M];
        for (int i = 0; i < M; ++i) {
            double denominator = (double) count(i);
            for (int j = 0; j < M; ++j) {
                ans[i][j] = count(i, j) / denominator;
                if (ans[i][j] < 1e-6) {
                    ans[i][j] = 1e-3;
                }
            }
        }
        return ans;
    }

    public double[][] getModel() {
        return model;
    }

    public double[] getProbabilities(double[][] centroids, double[][] samples) {
        int[] L = buildDictionary(centroids, samples);
        double[] ans = new double[L.length - N + 1];
        for (int i = 0; i < ans.length; ++i) {
            int[] temp = new int[N];
            System.arraycopy(L, i, temp, 0, N);
            ans[i] = getProbability(temp);
        }
        return ans;
    }

    public double[] getProbabilities(double[][] samples, LDA lda) {
        int[] L = buildLdaDictionary(samples, lda);
        double[] ans = new double[L.length - N + 1];
        for (int i = 0; i < ans.length; ++i) {
            int[] temp = new int[N];
            System.arraycopy(L, i, temp, 0, N);
            ans[i] = getProbability(temp);
        }
        return ans;
    }

    public double getProbability(int[] L) {
        double ans = 1;
        for (int i = 0; i < L.length - 1; ++i) {
            ans = ans * model[L[i]][L[i + 1]];
        }
        return ans;
    }

    /*public static int[] cleanDictionary(int[] rawDict) {
        int n = 0;
        boolean[] flag = new boolean[rawDict.length];
        for (int i = 0; i < rawDict.length - 2; ) {
            if (rawDict[i] != rawDict[i + 1]) {
                flag[i] = true;
                ++n;
                ++i;
            } else if (rawDict[i] == rawDict[i + 1] && rawDict[i] != rawDict[i + 2]) {
                flag[i] = true;
                flag[i + 1] = true;
                n = n + 2;
                i = i + 2;
            } else if (rawDict[i] == rawDict[i + 1] && rawDict[i] == rawDict[i + 2]) {
                flag[i] = false;
                ++i;
            }
        }
        int[] dict = new int[n];
        n = 0;
        for (int i = 0; i < flag.length; ++i) {
            if (flag[i]) {
                dict[n] = rawDict[i];
                ++n;
            }
        }
        for (int aDict : dict) {
            try {
                TextFile.writeFile(MainActivity.rootPath + "1.txt", "" + aDict + "\r", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dict;
    }*/

    //public static int[] getDict(double[][]centroids, double[][] featureVectors) {
        /*int[] dict = new int[featureVectors.length];
        for (int i = 0; i < featureVectors.length; ++i) {
            dict[i] = KMeansClustering.minDistance(featureVectors[i], centroids, false);
            try {
                TextFile.writeFile(MainActivity.rootPath + "1.txt", "" + dict[i] + "\r", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dict;*/
        /*int[] rawDict = new int[featureVectors.length];
        for (int i = 0; i < featureVectors.length; ++i) {
            //rawDict[i] = KMeans.minDistance(featureVectors[i], centroids, centroids.length, false);
            rawDict[i] = KMeans.minDistance(featureVectors[i], centroids, centroids.length);
        }
        int n = 0;
        boolean[] flag = new boolean[rawDict.length];
        for (int i = 0; i < rawDict.length - 2; ) {
            if (rawDict[i] != rawDict[i + 1]) {
                flag[i] = true;
                ++n;
                ++i;
            } else if (rawDict[i] == rawDict[i + 1] && rawDict[i] != rawDict[i + 2]) {
                flag[i] = true;
                flag[i + 1] = true;
                n = n + 2;
                i = i + 2;
            } else if (rawDict[i] == rawDict[i + 1] && rawDict[i] == rawDict[i + 2]) {
                flag[i] = false;
                ++i;
            }
        }
        int[] dict = new int[n];
        n = 0;
        for (int i = 0; i < flag.length; ++i) {
            if (flag[i]) {
                dict[n] = rawDict[i];
                ++n;
            }
        }
        for (int aDict : dict) {
            try {
                TextFile.writeFile(MainActivity.rootPath + "1.txt", "" + aDict + "\r", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dict;
    }*/
}
