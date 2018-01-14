package de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel;

/**
 * Created by abbas on 1/1/18.
 */

/**
 *   *
 *   * Forword class, inherited from the HMM class, to achieve the forward algorithm;
 *   * Given the known HMM model, calculate the probability of occurrence of a certain observation sequence;
 *   * Algorithm uses the idea of dynamic programming, the process is as follows:
 *   * 1. Calculate the local probability at t = 1;
 *   * 2. Calculate the local probability at time t + 1 according to the local probability at time t;
 *   * 3. Summing the local probabilities at time T is the final result.
 */

public class Forward extends HMM {

    public Forward(int stateNum, int observationSymbolNum) {
        super(stateNum, observationSymbolNum);
    }

    /* ob is a known observation sequence, and the returned result is the probability of observing
    the sequence */
    public double forward(int[] ob) {
        double[][] alpha = null;
        return forward(ob, alpha);
    }

    /* ob is known observation sequence; alpha: output intermediate result, local probability;
    probability of returning observation sequence */
    public double forward(int[] ob, double[][] alpha) {
        alpha = new double[ob.length][N];
        // 1. Initialize and calculate the local probabilities of all states at the initial time
        System.out.println("1.initialization：");
        for (int i = 0; i < N; i++) {
            alpha[0][i] = PI[i] * B[i][ob[0]];
            System.out.println("alpha[0][" + i + "]:" + alpha[0][i]);
        }
        // 2. Induction, recursive calculation of the local probability of each point in time
        System.out.println("2.induction：");
        for (int i = 1; i < ob.length; i++) //Loop from the first observation
        {
            for (int j = 0; j < N; j++) //For each state
            {
                double sum = 0;
                for (int k = 0; k < N; k++) {
                    sum += alpha[i - 1][k] * A[k][j];
                }
                alpha[i][j] = sum * B[j][ob[i]];
                System.out.println("alpha[" + i + "][" + j + "]:" + alpha[i][j]);
            }
        }
        /* 3. The probability of ending, observing a sequence equals the sum of all
        the local probabilities at the final moment */
        String s = "P(dry,damp,soggy)=";
        System.out.println("3.Termination, summation：");
        double probability = 0;
        for (int i = 0; i < N; i++) {
            probability += alpha[ob.length - 1][i];
            s += "alpha[" + (ob.length - 1) + "][" + i + "]";
            if (i != N - 1)
                s += "+";
        }
        System.out.println(s + "=" + probability);
        return probability;
    }
}
