package de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel;

/**
 * Created by abbas on 1/1/18.
 */

public class Geodesic {

    private double[][] vectors;
    private double[][] distances;

    public Geodesic(double[][] vs) {
        vectors = vs;
        setDistances();
    }

    public int findNearestCentroidIndex(int k, double[][] centroids) {
        int index = 0;
        double minimumDistance = Double.MAX_VALUE;
        for (int i = 0; i < centroids.length; ++i) {
            double temp = geodesicDistance(k, centroids[i]);
            if (minimumDistance > temp) {
                minimumDistance = temp;
                index = i;
            }
        }
        return index;
    }

    public double findNearestCentroidDistance(int k, double[][] centroids) {
        double minimumDistance = Double.MAX_VALUE;
        for (double[] centroid : centroids) {
            double temp = geodesicDistance(k, centroid);
            if (minimumDistance > temp) {
                minimumDistance = temp;
            }
        }
        return minimumDistance;
    }

    public double geodesicDistance(int k, double[] vNotInVectors) {
        int centroidIndex = findMinimumEuclidDistanceIndex(vNotInVectors, vectors);
        double ans = euclidDistance(vNotInVectors, vectors[centroidIndex]);
        ans = ans + geodesicDistance(k, centroidIndex);
        return ans;
    }

    public double geodesicDistance(int start, int end) {
        int now = start;
        int[] history = new int[1];
        int number = 0;
        double distance = 0;

        while (now != end) {
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
                    double temp = distances[now][i];
                    //double temp = euclidDistance(vectors[now], vectors[i]);
                    if (minimumDistance > temp) {
                        minimumDistance = temp;
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
    }

    public double geodesicDistance(double[] a, double[] b) {
        int start = findIndexInVectors(a);
        int end = findIndexInVectors(b);
        return geodesicDistance(start, end);
    }

    public double euclidDistance(double[] a, double[] b) {
        if (a.length != b.length) {
            return -1;
        }

        double sum = 0;
        for (int i = 0; i < a.length; ++i) {
            sum = sum + (a[i] - b[i]) * (a[i] - b[i]);
        }

        return Math.sqrt(sum);
    }

    public int findMinimumEuclidDistanceIndex(double[] v, double[][] vectors) {
        int index = 0;
        double minimumDistance = Double.MAX_VALUE;
        for (int i = 0; i < vectors.length; ++i) {
            double temp = euclidDistance(v, vectors[i]);
            if (minimumDistance > temp) {
                minimumDistance = temp;
                index = i;
            }
        }
        return index;
    }

    public int findIndexInVectors(double[] v) {
        int index = 0;
        for (; index < vectors.length; ++index) {
            if (euclidDistance(v, vectors[index]) < 1e-6) {
                break;
            }
        }
        return index;
    }

    public void setDistances() {
        distances = new double[vectors.length][vectors.length];
        for (int i = 0; i < vectors.length; ++i) {
            for (int j = 0; j <= i; ++j) {
                distances[i][j] = euclidDistance(vectors[i], vectors[j]);
                distances[j][i] = distances[i][j];
            }
        }
    }
}
