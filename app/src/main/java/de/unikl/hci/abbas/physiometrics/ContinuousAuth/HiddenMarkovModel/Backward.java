package de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel;

/**
 * Created by abbas on 1/1/18.
 */

import de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel.HMM;

public class Backward extends HMM {
    public Backward(int stateNum, int observationSymbolNum) {
        super(stateNum, observationSymbolNum);
    }

    //ob is Known observation sequence
    public double backward(int[] ob) {
        double[][] beta = null;     // Only statement, not defined
        return backward(ob, beta);
    }

    //ob is Known observation sequence; beta backward variable; returns the probability of the observed sequence
    public double backward(int[] ob, double[][] beta) {

        beta = new double[ob.length][N];
        // initialization
        System.out.println("1.initialization：");
        for (int i = 0; i < N; i++) {
            beta[ob.length - 1][i] = 1.0;
            System.out.println("beta[" + (ob.length - 1) + "][" + i + "]:" + beta[ob.length - 1][i]);
        }

        // induction
        System.out.println("2.induction：");
        for (int t = ob.length - 2; t >= 0; t--) {
            for (int j = 0; j < N; j++) {
                double Sum = 0;
                for (int i = 0; i < N; i++) {
                    Sum += A[j][i] * B[i][ob[t + 1]] * beta[t + 1][i];
                }

                beta[t][j] = Sum;
                System.out.println("beta[" + t + "][" + j + "]:" + beta[t][j]);
            }
        }

        // termination
        String s = "P(red,yellow,blue)=";
        System.out.println("3.Termination, summation：");
        double probability = 0;
        for (int i = 0; i < N; i++) {
            probability += beta[0][i];
            s += "beta[0][" + i + "]";
            if (i != N - 1)
                s += "+";
        }
        System.out.println(s + "=" + probability);
        return probability;
    }
}
