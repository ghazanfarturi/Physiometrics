package de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel;

/**
 * Created by abbas on 1/1/18.
 * <p>
 *   * Viterbi class, inherited from the HMM class, implements the Viterbi algorithm;
 *   * In the case of known HMM model, calculate the most likely hidden sequence of a certain observation sequence;
 *   * Algorithm uses the idea of dynamic programming, the process is as follows:
 *   * 1.  t = 1 when initialized;
 *   * 2.  Calculate the best path at time t + 1 according to the local best path at time t;
 *   * 3.  The optimal path at time T is the final result, and the best path is traced back through the backward pointer.
 * <p>
 *   * Viterbi class, inherited from the HMM class, implements the Viterbi algorithm;
 *   * In the case of known HMM model, calculate the most likely hidden sequence of a certain observation sequence;
 *   * Algorithm uses the idea of dynamic programming, the process is as follows:
 *   * 1.  t = 1 when initialized;
 *   * 2.  Calculate the best path at time t + 1 according to the local best path at time t;
 *   * 3.  The optimal path at time T is the final result, and the best path is traced back through the backward pointer.
 */

/**
   * Viterbi class, inherited from the HMM class, implements the Viterbi algorithm;
   * In the case of known HMM model, calculate the most likely hidden sequence of a certain observation sequence;
   * Algorithm uses the idea of dynamic programming, the process is as follows:
   * 1.  t = 1 when initialized;
   * 2.  Calculate the best path at time t + 1 according to the local best path at time t;
   * 3.  The optimal path at time T is the final result, and the best path is traced back through the backward pointer.
 */

import java.util.ArrayList;
import java.util.List;

public class Viterbi extends HMM {

    public Viterbi(int stateNum, int observationSymbolNum) {
        super(stateNum, observationSymbolNum);
    }

    /* ob is known sequence of observations; the probability of the most likely hidden state sequence;
    the most likely hidden state sequence to return */
    public List viterbi(int[] ob, double probability) {
        double[][] delta = null;
        int[][] psi = null;
        return viterbi(ob, delta, psi, probability);
    }

    // delta output intermediate result, local maximum probability;
    // psi output intermediate result, reverse pointer indicates the most probable path;
    // probability of returning the most likely hidden state sequence
    public List viterbi(int[] ob, double[][] delta, int[][] psi, double probability) {
        delta = new double[ob.length][N];   // Local probability
        psi = new int[ob.length][N];        /* Inverted pointer, used to store the most likely to
                                            reach a state of the previous state index */
        System.out.println("1.initialization：");

        // 1. initialization
        for (int j = 0; j < N; j++) {
            delta[0][j] = PI[j] * B[j][ob[0]];
            System.out.println("delta[0][" + j + "]:" + delta[0][j]);
        }

        // 2. Recursive
        System.out.println("2.induction：");
        for (int t = 1; t < ob.length; t++) {
            for (int j = 0; j < N; j++) {
                double MaxValue = delta[t - 1][0] * A[0][j];    /* Initially set the value at the 0th
                                                                state to the maximum value */
                int MaxValueIndex = 0;  // Store the index of the state at which the maximum is taken
                for (int i = 1; i < N; i++) {
                    double Value = delta[t - 1][i] * A[i][j];
                    if (Value > MaxValue) {
                        MaxValue = Value;
                        MaxValueIndex = i;
                    }
                }

                delta[t][j] = MaxValue * B[j][ob[t]];
                System.out.println("delta[" + t + "][" + j + "]:" + delta[t][j]);
                psi[t][j] = MaxValueIndex;  // Record the last status that most likely reached this status
            }
        }

        // 3. termination
        System.out.println("3.Termination, backtracking for the best path");
        int[] q = new int[ob.length];            // Define the best path
        probability = delta[ob.length - 1][0];  //0 is defined as the maximum weight
        q[ob.length - 1] = 0;
        for (int i = 1; i < N; i++) {
            if (delta[ob.length - 1][i] > probability) {
                probability = delta[ob.length - 1][i];
                q[ob.length - 1] = i;   // The last time the best state
            }
        }

        // 4. Backtrack
        for (int t = ob.length - 2; t >= 0; t--) {
            q[t] = psi[t + 1][q[t + 1]];
        }

        List list = new ArrayList();
        list.add(q);
        list.add(probability);
        return list;
    }
}
