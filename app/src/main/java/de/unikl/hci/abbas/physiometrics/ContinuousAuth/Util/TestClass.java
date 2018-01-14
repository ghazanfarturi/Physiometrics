package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util;

/**
 * Created by abbas on 1/1/18.
 */

import android.util.Log;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.*;

import de.unikl.hci.abbas.physiometrics.Demo.MainActivityCA;

public class TestClass {

    public static void trans() {
        for (int i = 0; i < 4; ++i) {
            String s;
            try {
                s = TextFile.readFile(MainActivityCA.rootPath + "PearsonPValue - " + i + ".txt");
                String[] lines = s.split("\r");
                double[][] vectors = new double[lines.length][lines[0].split("\t").length];
                for (int j = 0; j < vectors.length; ++j) {
                    String[] line = lines[j].split("\t");
                    for (int k = 0; k < vectors[j].length; ++k) {
                        vectors[j][k] = Double.parseDouble(line[k]);
                        vectors[j][k] = vectors[j][k] < 0.05 ? 0 : 1;
                    }
                }
                s = "";
                for (double[] vector : vectors) {
                    for (double value : vector) {
                        s = s + value + "\t";
                    }
                    s = s + "\r";
                }
                TextFile.writeFile(MainActivityCA.rootPath + "newPearsonPValue - " + i + ".txt", s, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double[][] pearson(int w) {
        String rawFeatureVectors = "";
        try {
            rawFeatureVectors = TextFile.readFile(MainActivityCA.rootPath + "group" + w + "/FeatureVectors.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] lines = rawFeatureVectors.split("\r");
        double[][] featureVectors = new double[lines.length][lines[0].split("\t").length];
        for (int i = 0; i < lines.length; ++i) {
            String[] values = lines[i].split("\t");
            for (int j = 0; j < values.length; ++j) {
                featureVectors[i][j] = Double.parseDouble(values[j]);
            }
        }

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(featureVectors);
        RealMatrix pValues = pearsonsCorrelation.getCorrelationPValues();
        RealMatrix realMatrix = pearsonsCorrelation.getCorrelationMatrix();

        try {
            String stringPValue = "";
            for (double[] vector : pValues.getData()) {
                for (double value : vector) {
                    stringPValue = stringPValue + value + "\t";
                }
                stringPValue += "\r";
            }
            String string = "";
            for (double[] vector : realMatrix.getData()) {
                for (double value : vector) {
                    string = string + value + "\t";
                }
                string += "\r";
            }
            TextFile.writeFile(MainActivityCA.rootPath + "PearsonPValue - " + w + ".txt", stringPValue, false);
            TextFile.writeFile(MainActivityCA.rootPath + "Pearson - " + w + ".txt", string, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pValues.getData();
    }

    public static double[][] tTest(int w) {

        String rawFeatureVectors = "";
        try {
            rawFeatureVectors = TextFile.readFile(MainActivityCA.rootPath + "group" + w + "/FeatureVectors.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] lines = rawFeatureVectors.split("\r");
        double[][] featureVectors = new double[lines.length][lines[0].split("\t").length];
        for (int i = 0; i < lines.length; ++i) {
            String[] values = lines[i].split("\t");
            for (int j = 0; j < values.length; ++j) {
                featureVectors[i][j] = Double.parseDouble(values[j]);
            }
        }

        double[] a = new double[featureVectors.length];
        double[] b = new double[featureVectors.length];
        double[][] result = new double[featureVectors[0].length][featureVectors[0].length];
        for (int i = 0; i < featureVectors[0].length; ++i) {
            for (int j = 0; j <= i; ++j) {
                for (int k = 0; k < featureVectors.length; ++k) {
                    a[k] = featureVectors[k][i];
                    b[k] = featureVectors[k][j];
                }
                TTest tTest = new TTest();
                result[i][j] = tTest.tTest(a, b);
                result[i][j] = result[i][j] > 0.05 ? 9999 : 0;
                result[j][i] = result[i][j];
                Log.i("T-Test " + i + " - " + j, "" + result[i][j]);
            }
        }

        try {
            String stringResult = "";
            for (double[] vector : result) {
                for (double value : vector) {
                    stringResult = stringResult + value + "\t";
                }
                stringResult += "\r";
            }
            TextFile.writeFile(MainActivityCA.rootPath + "T - Test - " + w + ".txt", stringResult, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static double geodesicDistance() {

        String rawVectorLines[] = new String[1];
        try {
            rawVectorLines = TextFile.readFile(MainActivityCA.rootPath + "group1/Centroids.txt").split("\r");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double[][] centroids = new double[100][88];
        for (int i = 0; i < 100; ++i) {
            String[] values = rawVectorLines[i].split("\t");
            for (int j = 0; j < values.length; ++j) {
                centroids[i][j] = Double.parseDouble(values[j]);
            }
        }

        Log.i("Distance", "" + distance(centroids[18], centroids[75], centroids, true));

        return 0;
    }

    public static double[] distances(double[] element, double[][] centroids,
                                     int numberOfCentroids, boolean isInVectors) {
        double[] ans = new double[numberOfCentroids];
        for (int i = 0; i < numberOfCentroids; ++i) {
            // ans[i] = distance(element, centroids[i]);
            ans[i] = distance(element, centroids[i], centroids, isInVectors);
        }
        return ans;
    }

    public static double distance(int start, int end, int now, double[][] vectors,
                                  int number, int[] history, double distance) {
        while (now != end) {
            if (number == 0) {
                history[0] = start;
                now = start;
            }
            double minimumDistance = Double.MAX_VALUE;
            int minimumIndex = -1;
            for (int i = 0; i < vectors.length; ++i) {
                boolean isInHistory = false;
                for (int aHistory : history) {
                    if (i == aHistory) {
                        isInHistory = true;
                        break;
                    }
                }
                if (now != i && !isInHistory) {
                    double tempDistance = distance(vectors[now], vectors[i]);
                    if (minimumDistance > tempDistance) {
                        minimumDistance = tempDistance;
                        minimumIndex = i;
                    }
                }
            }
            int[] tempHistory = new int[history.length + 1];
            System.arraycopy(history, 0, tempHistory, 0, history.length);
            tempHistory[history.length] = minimumIndex;
            history = tempHistory;
            ++number;
            now = tempHistory[number];
            distance = distance + minimumDistance;
        }
        return distance;
        /*if (now == end) {
            return distance;
        }
        if (number == 0) {
            history[0] = start;
            now = start;
        }
        double minimumDistance = Double.MAX_VALUE;
        int minimumIndex = -1;
        for (int i = 0; i < vectors.length; ++i) {
            boolean isInHistory = false;
            for (int aHistory : history) {
                if (i == aHistory) {
                    isInHistory = true;
                    break;
                }
            }
            if (now != i && !isInHistory) {
                double tempDistance = distance(vectors[now], vectors[i]);
                if (minimumDistance > tempDistance) {
                    minimumDistance = tempDistance;
                    minimumIndex = i;
                }
            }
        }
        int[] tempHistory = new int[history.length + 1];
        System.arraycopy(history, 0, tempHistory, 0, history.length);
        tempHistory[history.length] = minimumIndex;
        ++number;
        return distance(start, end, tempHistory[number], vectors, number, tempHistory, distance + minimumDistance);
        */
    }

    public static double distance(double[] a, double[] b, double[][] vectors, boolean isAInVectors) {
        if (isAInVectors) {
            int start = -1;
            int end = -1;
            for (int i = 0; i < vectors.length; ++i) {
                if (distance(a, vectors[i]) < 1e-6) {
                    start = i;
                }
                if (distance(b, vectors[i]) < 1e-6) {
                    end = i;
                }
                if (start != -1 && end != -1) {
                    break;
                }
            }
            return distance(start, end, start, vectors, 0, new int[1], 0);
        } else {
            double[][] tempVectors = new double[vectors.length + 1][vectors[0].length];
            for (int i = 0; i < vectors.length; ++i) {
                System.arraycopy(vectors[i], 0, tempVectors[i], 0, vectors[0].length);
            }
            System.arraycopy(a, 0, tempVectors[tempVectors.length - 1], 0, a.length);
            int start = tempVectors.length - 1;
            int end = -1;
            for (int i = 0; i < vectors.length; ++i) {
                if (distance(b, vectors[i]) < 1e-6) {
                    end = i;
                    break;
                }
            }
            return distance(start, end, start, tempVectors, 0, new int[1], 0);
        }
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

    public void train(double[][] features, int K, double[][] clusterCenters, int[] clusterIndex) {
        int numIns = features.length;
        int numAtt = features[0].length;

        boolean isOK = true; // End of cycle judgment
        dealNaN(features);
        Random rand = new Random();
        int randNum;
        for (int i = 0; i < K; i++) {
            do {
                randNum = rand.nextInt(numIns);
            } while (clusterIndex[randNum] == 1);
            clusterCenters[i] = Arrays.copyOf(features[randNum], features[randNum].length);
            clusterIndex[randNum] = 1;
        }

        while (isOK) {
            int[] preClusterIndex;
            preClusterIndex = Arrays.copyOf(clusterIndex, clusterIndex.length);
            updateIndex(features, K, clusterCenters, clusterIndex);
            setClusterCenters(features, K, clusterCenters, clusterIndex); // Re-determine the center of each cluster
            if (equals(preClusterIndex, clusterIndex)) {
                isOK = false;
            }
        }
    }

    // Handle missing attributes
    public void dealNaN(double[][] array) {
        for (int i = 0; i < array[0].length; i++) {
            for (int j = 0; j < array.length; j++) {
                if (Double.isNaN(array[j][i])) {
                    array[j][i] = plural(array, i);
                }
            }
        }
    }

    // Returns the median of a column
    public double plural(double[][] array, int col) {
        Map<Double, Integer> plu = new HashMap<>();
        int maxval = 0;
        double maxkey = 0;
        for (double[] anArray : array) {
            if (plu.containsKey(anArray[col])) {
                int Val = plu.get(anArray[col]);
                Val++;
                plu.put(anArray[col], Val);
            } else {
                plu.put(anArray[col], 1);
            }
        }

        for (Map.Entry<Double, Integer> entry : plu.entrySet()) {
            double key = entry.getKey();
            int value = entry.getValue();
            if (value > maxval) {
                maxval = value;
                maxkey = key;
            }
        }
        return maxkey;
    }

    // Look for the cluster number
    public void updateIndex(double[][] features, int K, double[][] clusterCenters, int[] clusterIndex) {
        double minDis; // Define variables
        int index = 0; // Cluster number
        double distance = 0;

        // Look for the cluster number
        for (int i = 0; i < features.length; i++) {
            minDis = EuclidDis(features[i], clusterCenters[0]); // Initialize minDis to the distance from the first center point
            for (int j = 0; j < K; j++) {
                distance = EuclidDis(features[i], clusterCenters[j]);
                if (distance < minDis) {
                    minDis = distance;
                    index = j;
                }
            }
            clusterIndex[i] = index;
        }
    }

    // Print the array
    public void printArray(double[][] array) {
        int m = array.length;
        int n = array[0].length;
        for (double[] anArray : array) {
            for (int j = 0; j < n; j++) {
                System.out.print(anArray[j] + "  ");
            }
            System.out.println();
        }
    }

    // Overloaded print array methods
    public void printArray(int[] array) {
        for (int anArray : array) {
            System.out.println(anArray);
        }
    }

    // Update cluster center
    public void setClusterCenters(double[][] features, int K, double[][] clusterCenters, int[] clusterIndex) {
        int numIns = features.length;
        int numAtt = features[0].length;
        double[] averEuclidDis = new double[numIns]; // The average of the Euclidean distances for storing a point to all points in its cluster
        double sum = 0;
        double num = 0.0; // The number of points in each cluster
        // For some point
        for (int i = 0; i < numIns; i++) {
            sum = 0;
            num = 0.0;
            // Search for points within the same cluster and add the Euclidean distance
            for (int j = 0; j < numIns; j++) {
                if (clusterIndex[i] == clusterIndex[j]) {
                    sum = sum + EuclidDis(features[i], features[j]);
                    num++;
                }
            }
            averEuclidDis[i] = sum / num;
        }
        int minIndex = 0; // Find the center
        double minVal;
        for (int i = 0; i < K; i++) {
            minVal = Double.MAX_VALUE;
            for (int j = 0; j < numIns; j++) {
                // If belongs to k cluster
                if (clusterIndex[j] == i) {
                    // minVal=averEuclidDis[j]
                    if (minVal > averEuclidDis[j]) {
                        minVal = averEuclidDis[j];
                        minIndex = j;
                    }
                }
            }
            clusterCenters[i] = Arrays.copyOf(features[minIndex], features[minIndex].length); // Change the center point
        }
    }

    // Return Euclidean distance between two vectors, a, b must be the same size
    public double EuclidDis(double[] a, double[] b) {
        // The two vectors are not equal in length
        if (a.length != b.length) {
            System.out.println("The two vector are not the same size!");
            return 0;
        }
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(sum);
    }

    // Determine whether two plastic arrays are equal
    public boolean equals(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
