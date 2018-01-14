package de.unikl.hci.abbas.physiometrics.ContinuousAuth.HiddenMarkovModel;

/**
 * Created by abbas on 1/1/18.
 */

import java.util.List;

public class TestHMM2 {

    public static void main(String[] args) {
        // Test the forward algorithm
        CheckForward();
        System.out.println(" ");
        // Test the Viterbi algorithm
        CheckViterbi();
    }  // Visible state (seaweed morphology: dry, slightly wet, wet through)

    // Test the forward algorithm
    static void CheckForward() {
        // State transition matrix
        double[][] A =
                {
                        {0.500, 0.375, 0.125},
                        {0.250, 0.125, 0.625},
                        {0.250, 0.375, 0.375}
                };

        // Confusion matrix
        double[][] B =
                {
                        {0.60, 0.20, 0.15, 0.05},
                        {0.25, 0.25, 0.25, 0.25},
                        {0.05, 0.10, 0.35, 0.50}
                };

        // Initial probability vector
        double[] PI = {0.63, 0.17, 0.20};

        // Observe the sequence
        int[] OB = {Algae.dry.ordinal(), Algae.damp.ordinal(), Algae.soggy.ordinal()};
        System.out.println("--------------------------Forward algorithm test--------------------------");
        System.out.println("State transition probability matrix：");
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                System.out.print(A[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("Symbol observation probability matrix：");
        for (int i = 0; i < B.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                System.out.print(B[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("Initial probability vector：{" + PI[0] + " " + PI[1] + " " + PI[2] + "}");
        System.out.println("Hide state sequence：{" + Weather.sunny + " " + Weather.cloudy + " " + Weather.rainy + "}");
        System.out.println("Observation sequence：{" + Algae.dry + " " + Algae.damp + " " + Algae.soggy + "}");

        // Initialize the HMM model
        Forward forward = new Forward(A.length, B[0].length);
        forward.A = A;
        forward.B = B;
        forward.PI = PI;

        // Observe the probability of the sequence
        double probability = forward.forward(OB);

    } // Hidden state (weather conditions: sunny, cloudy, rainy)

    // Test the Viterbi algorithm
    static void CheckViterbi() {
        // State transition matrix
        double[][] A =
                {
                        {0.500, 0.250, 0.250},
                        {0.375, 0.125, 0.375},
                        {0.125, 0.675, 0.375}
                };

        // Confusion matrix
        double[][] B =
                {
                        {0.60, 0.20, 0.15, 0.05},
                        {0.25, 0.25, 0.25, 0.25},
                        {0.05, 0.10, 0.35, 0.50}
                };

        // Initial probability vector
        double[] PI = {0.63, 0.17, 0.20};
        // Observe the sequence
        int[] OB = {Algae.dry.ordinal(), Algae.damp.ordinal(), Algae.soggy.ordinal()};
        System.out.println("--------------------------Viterbi algorithm test--------------------------");
        System.out.println("State transition probability matrix：");
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                System.out.print(A[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("Symbol observation probability matrix：");
        for (int i = 0; i < B.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                System.out.print(B[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("Initial probability vector：{" + PI[0] + " " + PI[1] + " " + PI[2] + "}");
        System.out.println("Hide state sequence：{" + Weather.sunny + " " + Weather.cloudy + " " + Weather.rainy + "}");
        System.out.println("Observation sequence：{" + Algae.dry + " " + Algae.damp + " " + Algae.soggy + "}");

        // Initialize the HMM model
        Viterbi viterbi = new Viterbi(A.length, B[0].length);
        viterbi.A = A;
        viterbi.B = B;
        viterbi.PI = PI;

        // Find the most likely sequence of hidden states
        double probability = 0;

        List list = viterbi.viterbi(OB, probability);
        int[] Q = (int[]) list.get(0);  //Return hidden sequence
        System.out.print("The most likely sequence of hidden states is：{");
        for (int value : Q) {
            System.out.print(Weather.values()[value] + " ");
        }
        System.out.println("}");
        System.out.println("The greatest possibility is：" + list.get(1));
    }

    enum Algae {dry, damp, soggy}

    enum Weather {sunny, cloudy, rainy}
}
