package de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel;

/**
 * Created by abbas on 1/1/18.
 * <p>
 * Linear Discriminant Analysis
 * LDA class:
 * 1.
 * 2.
 * <p>
 * Linear Discriminant Analysis
 * LDA class:
 * 1.
 * 2.
 */

/**
 * Linear Discriminant Analysis
 * LDA class:
 * 1.
 * 2.
 */

import java.util.ArrayList;

import Jama.Matrix;

public class LDA {

    private double[][] groupMean;
    private double[][] pooledInverseCovariance;
    private double[] probability;
    private ArrayList<Integer> groupList = new ArrayList<>();

    /**
     * Calculates a linear discriminant analysis (LDA) with all necessary
     *
     * @param p
     *            Set to true, if the probability estimation should be based on
     *            the real group sizes (true), or if the share of each group
     *            should be equal
     */
    @SuppressWarnings("unchecked")
    public LDA(double[][] d, int[] g, boolean p) {
        // check if data and group array have the same size
        if (d.length != g.length)
            return;

        double[][] data = new double[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            System.arraycopy(d[i], 0, data[i], 0, d[i].length);
        }

        int[] group = new int[g.length];
        System.arraycopy(g, 0, group, 0, g.length);

        double[] globalMean;
        double[][][] covariance;

        // determine number and label of groups
        for (int aGroup : group) {
            if (!groupList.contains(aGroup)) {
                groupList.add(aGroup);
            }
        }

        // divide data into subsets
        ArrayList<double[]>[] subset = new ArrayList[groupList.size()];
        for (int i = 0; i < subset.length; i++) {
            subset[i] = new ArrayList<>();
            for (int j = 0; j < data.length; j++) {
                if (group[j] == groupList.get(i)) {
                    subset[i].add(data[j]);
                }
            }
        }

        // calculate group mean
        groupMean = new double[subset.length][data[0].length];
        for (int i = 0; i < groupMean.length; i++) {
            for (int j = 0; j < groupMean[i].length; j++) {
                groupMean[i][j] = getGroupMean(j, subset[i]);
            }
        }

        // calculate global mean
        globalMean = new double[data[0].length];
        for (int i = 0; i < data[0].length; i++) {
            globalMean[i] = getGlobalMean(i, data);
        }

        // correct subset data
        for (ArrayList<double[]> aSubset : subset) {
            for (int j = 0; j < aSubset.size(); j++) {
                double[] v = aSubset.get(j);

                for (int k = 0; k < v.length; k++)
                    v[k] = v[k] - globalMean[k];

                aSubset.set(j, v);
            }
        }

        // calculate covariance
        covariance = new double[subset.length][globalMean.length][globalMean.length];
        for (int i = 0; i < covariance.length; i++) {
            for (int j = 0; j < covariance[i].length; j++) {
                for (int k = 0; k < covariance[i][j].length; k++) {
                    for (int l = 0; l < subset[i].size(); l++)
                        covariance[i][j][k] += (subset[i].get(l)[j] * subset[i].get(l)[k]);

                    covariance[i][j][k] = covariance[i][j][k] / subset[i].size();
                }
            }
        }

        // calculate pooled within group covariance matrix and invert it
        pooledInverseCovariance = new double[globalMean.length][globalMean.length];
        for (int j = 0; j < pooledInverseCovariance.length; j++) {
            for (int k = 0; k < pooledInverseCovariance[j].length; k++) {
                for (int l = 0; l < subset.length; l++) {
                    pooledInverseCovariance[j][k] += ((double) subset[l].size() / (double) data.length)
                            * covariance[l][j][k];
                }
            }
        }

        pooledInverseCovariance = new Matrix(pooledInverseCovariance).inverse().getArray();

        // calculate probability for different groups
        this.probability = new double[subset.length];
        if (!p) {
            double prob = 1.0d / groupList.size();
            for (int i = 0; i < groupList.size(); i++) {
                this.probability[i] = prob;
            }
        } else {
            for (int i = 0; i < subset.length; i++) {
                this.probability[i] = (double) subset[i].size() / (double) data.length;
            }
        }
    }

    /**
     * Returns the mean of the given values. On error or empty data returns 0.
     *
     * @param values
     *            The values.
     * @return The mean.
     * @since 1.5
     */
    public static double getMean(final double[] values) {
        if (values == null || values.length == 0)
            return Double.NaN;

        double mean = 0.0d;

        for (double value : values) mean += value;

        return mean / (double) values.length;
    }

    /**
     * Test case with the original values from the tutorial of Kardi Teknomo
     */
    public static void main(String[] args) {
        int[] group = {1, 1, 1, 1, 2, 2, 2};
        double[][] data = {
                {2.95, 6.63},
                {2.53, 7.79},
                {3.57, 5.65},
                {3.16, 5.47},
                {2.58, 4.46},
                {2.16, 6.22},
                {3.27, 3.52}
        };

        LDA test = new LDA(data, group, true);
        double[] testData = {2.81, 5.46};

        //test
        double[] values = test.getDiscriminantFunctionValues(testData);
        for (int i = 0; i < values.length; i++) {
            System.out.println("Discriminant function " + (i + 1) + ": " + values[i]);
        }

        System.out.println("Predicted group: " + test.predict(testData));
    }

    private double getGroupMean(int column, ArrayList<double[]> data) {
        double[] d = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            d[i] = data.get(i)[column];
        }

        return getMean(d);
    }

    /**
     * Calculates the discriminant function values for the different groups based on Mahalanobis distance
     */
    /*public double[] getMahalanobisDistance(double[] values) {
        double[] function = new double[groupList.size()];
		for (int i = 0; i < groupList.size(); i++) {
			double[] dist = new double[groupMean[i].length];
			for (int j = 0; j < dist.length; j++)
				dist[j] = values[j] - groupMean[i][j];
			function[i] = matrixMultiplication(matrixMultiplication(dist,
					this.pooledInverseCovariance), dist);
		}
		return function;
	}*/

    /**
     * Predict the membership of an object to one of the different groups based on Mahalanobis distance
     */
	/*public int predictM(double[] values) {
		int group = -1;
		double max = Double.NEGATIVE_INFINITY;
		double[] discr = this.getMahalanobisDistance(values);
		for (int i = 0; i < discr.length; i++) {
			if (discr[i] > max) {
				max = discr[i];
				group = groupList.get(i);
			}
		}
		return group;
	}*/

    /**
     * Calculates the probability for the membership in the different groups
     */
	/*public double[] getProbabilityEstimates(double[] values) {
		return new double[] {};
	}*/

    /**
     * Returns the weight for the linear fisher's discrimination functions
     *
     * @return the weights
     */
	/*public double[] getFisherWeights() {
		return new double[] {};
	}*/
    private double getGlobalMean(int column, double data[][]) {
        double[] d = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            d[i] = data[i][column];
        }

        return getMean(d);
    }

    /**
     * Calculates the discriminant function values for the different groups
     */
    public double[] getDiscriminantFunctionValues(double[] values) {
        double[] function = new double[groupList.size()];
        for (int i = 0; i < groupList.size(); i++) {
            double[] tmp = matrixMultiplication(groupMean[i],
                    pooledInverseCovariance);
            function[i] = (matrixMultiplication(tmp, values))
                    - (.5d * matrixMultiplication(tmp, groupMean[i]))
                    + Math.log(probability[i]);
        }

        return function;
    }

    /**
     * Predict the membership of an object to one of the different groups.
     */
    public int predict(double[] values) {
        int group = -1;
        double max = Double.NEGATIVE_INFINITY;
        double[] discr = this.getDiscriminantFunctionValues(values);
        for (int i = 0; i < discr.length; i++) {
            if (discr[i] > max) {
                max = discr[i];
                group = groupList.get(i);
            }
        }

        return group;
    }

    /**
     * Multiplies two matrices and returns the result as a double[][]-array.
     * Please not, that the number of rows in matrix a must be equal to the
     * number of columns in matrix b
     *
     * @return the resulting matrix
     */
    @SuppressWarnings("unused")
    private double[][] matrixMultiplication(final double[][] matrixA, final double[][] matrixB) {
        int rowA = matrixA.length;
        int colA = matrixA[0].length;
        int colB = matrixB[0].length;

        double c[][] = new double[rowA][colB];
        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < colB; j++) {
                c[i][j] = 0;
                for (int k = 0; k < colA; k++) {
                    c[i][j] = c[i][j] + matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        return c;
    }

    /**
     * Multiplies two matrices and returns the result as a double[]-array.
     * Please not, that the number of rows in matrix a must be equal to the
     * number of columns in matrix b
     */
    public double[] matrixMultiplication(double[] A, double[][] B) {

        if (A.length != B.length) {
            throw new IllegalArgumentException("A:Rows: " + A.length + " did not match B:Columns " + B.length + ".");
        }

        double[] C = new double[A.length];
        for (int i = 0; i < C.length; i++) {
            C[i] = 0.00000;
        }

        for (int i = 0; i < A.length; i++) { // aRow
            for (int j = 0; j < B[0].length; j++) { // bColumn
                C[i] += A[j] * B[i][j];
            }
        }

        return C;
    }

    /**
     * Multiplies two matrices and returns the result as a double (the second
     * matrix is transposed automatically). Please note, that the number of rows
     * in matrix a must be equal to the number of columns in matrix b
     */
    private double matrixMultiplication(double[] matrixA, double[] matrixB) {

        double c = 0d;
        for (int i = 0; i < matrixA.length; i++) {
            c += matrixA[i] * matrixB[i];
        }

        return c;
    }

    /**
     * Transposes a matrix
     *
     * @param matrix
     *            the matrix to transpose
     * @return the transposed matrix
     */
    @SuppressWarnings("unused")
    private double[][] transpose(final double[][] matrix) {
        double[][] trans = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                trans[j][i] = matrix[i][j];
            }
        }

        return trans;
    }

    /**
     * Transposes a matrix
     *
     * @param matrix
     *            the matrix to transpose
     * @return the transposed matrix
     */
    @SuppressWarnings("unused")
    private double[][] transpose(final double[] matrix) {
        double[][] trans = new double[1][matrix.length];
        System.arraycopy(matrix, 0, trans[0], 0, matrix.length);

        return trans;
    }

}
