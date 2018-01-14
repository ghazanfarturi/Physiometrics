package de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel;

/**
 * Created by abbas on 12/31/17.
 */

public class Assessment {
    public double[][] centroids;
    public int[] clusterMark;
    public double[] clusterError;

    public Assessment() {
        centroids = new double[0][];
        clusterMark = new int[0];
        clusterError = new double[0];
    }
}
