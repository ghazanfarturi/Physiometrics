package de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel;

/**
 * Created by abbas on 1/1/18.
 */

/**
 * Hidden Markov Model (HMM)
 * HMM class:
 * 1. HMM model of several important parameters, two states, three matrices;
 * 2. Two constructors are defined;
 */

public class HMM {

    protected int N;        //Number of states
    protected int M;        //Observe the number of symbols
    protected double[][] A; //State transition probability matrix, N * N matrix
    protected double[][] B; //Symbol observation probability matrix, namely confusion matrix, N * M matrix
    protected double[] PI;  //Initial state probability distribution matrix, N-dimensional vector

    public HMM() {
    }

    //Parameter 1 Status Number;
    //Parameter 2 Observe the number of symbols
    public HMM(int stateNum, int observationSymbolNum) {
        N = stateNum;
        M = observationSymbolNum;
        A = new double[N][N];
        B = new double[N][M];
        PI = new double[N];
    }

}
