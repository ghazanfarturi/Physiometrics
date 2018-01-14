package de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel;

/**
 * Created by abbas on 1/1/18.
 * <p>
 * Client class for Hidden Markov Model
 * HMMClient class:
 * 1.
 * 2.
 * <p>
 * Client class for Hidden Markov Model
 * HMMClient class:
 * 1.
 * 2.
 */

/**
 * Client class for Hidden Markov Model
 * HMMClient class:
 * 1.
 * 2.
 */

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.TextFile;
import de.unikl.hci.abbas.physiometrics.Demo.MainActivityCA;

public class HMMClient {

    public static void newTest(int[] dict) {
        List<List<ObservationInteger>> sequences = new ArrayList<>();
        int k = 0;
        for (int i = 0; i < dict.length / 200; ++i) {
            List<ObservationInteger> temp = new ArrayList<>();
            for (int j = 0; j < 200; ++j) {
                temp.add(new ObservationInteger(dict[k]));
                ++k;
            }
            sequences.add(temp);
        }
        Hmm<ObservationInteger> hmm = new Hmm<>(5, new OpdfIntegerFactory(40));
        for (int i = 0; i < 5; ++i) {
            hmm.setPi(i, 0.2);
        }
        for (int i = 0; i < 5; ++i) {
            hmm.setOpdf(i, new OpdfInteger(40));
        }
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                hmm.setAij(i, j, 0.2);
            }
        }

        BaumWelchLearner baumWelchLearner = new BaumWelchLearner();

        for (int i = 0; i < 10; i++) {
            hmm = baumWelchLearner.iterate(hmm, sequences);
        }

        try {
            TextFile.writeFile(MainActivityCA.rootPath + "HMM.txt", hmm.toString(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void newNewTest() {

        String rawDict = "";
        try {
            rawDict = TextFile.readFile(MainActivityCA.rootPath + "group1mark.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] tmp = rawDict.split("\r");
        int[] dict = new int[tmp.length];
        for (int i = 0; i < dict.length; ++i) {
            dict[i] = Integer.parseInt(tmp[i]);
        }

        List<List<ObservationInteger>> sequences = new ArrayList<>();
        int k = 0;
        for (int i = 0; i < 100; ++i) {
            List<ObservationInteger> temp = new ArrayList<>();
            for (int j = 0; j < 200; ++j) {
                int w = (int) (Math.random() * 41);
                temp.add(new ObservationInteger(w));
                ++k;
            }
            sequences.add(temp);
        }

        Hmm<ObservationInteger> hmm = new Hmm<>(5, new OpdfIntegerFactory(41));
        for (int i = 0; i < 5; ++i) {
            hmm.setPi(i, 0.2);
        }

        for (int i = 0; i < 5; ++i) {
            hmm.setOpdf(i, new OpdfInteger(41));
        }

        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                hmm.setAij(i, j, 0.2);
            }
        }

        BaumWelchLearner baumWelchLearner = new BaumWelchLearner();

        for (int i = 0; i < 10; i++) {
            hmm = baumWelchLearner.iterate(hmm, sequences);
        }

        System.out.println("Resulting HMM:\n" + hmm);

    }

    public static void myTest(int[] dict) {
        learn(200, generateSequences(dict));
    }

    public static Hmm<ObservationInteger> learn(int n, List<List<ObservationInteger>> sequences) {

        BaumWelchLearner baumWelchLearner = new BaumWelchLearner();
        Hmm<ObservationInteger> learntHmm = buildInitHmm(n, 200);

        for (int i = 0; i < 10; i++) {
            learntHmm = baumWelchLearner.iterate(learntHmm, sequences);
        }

        System.out.println("Resulting HMM:\n" + learntHmm);

        return learntHmm;
    }

    private static Hmm<ObservationInteger> buildInitHmm(int n, int m) {
        double tempProbability = 1.0 / n;
        OpdfInteger opdfInteger = new OpdfInteger(m);

        Hmm<ObservationInteger> hmm = new Hmm<>(n, new OpdfIntegerFactory(n));

        for (int i = 0; i < n; ++i) {
            hmm.setPi(i, tempProbability);
            hmm.setOpdf(i, opdfInteger);
            for (int j = 0; j < n; ++j) {
                hmm.setAij(i, j, tempProbability);
            }
        }

        return hmm;
    }

    public static List<List<ObservationInteger>> generateSequences(int[] dict) {
        int M = dict.length / 50;
        List<List<ObservationInteger>> sequences = new ArrayList<>();

        int k = 0;
        for (int i = 0; i < 50; ++i) {
            List<ObservationInteger> tempList = new ArrayList<>();
            for (int j = 0; j < M; ++j) {
                tempList.add(new ObservationInteger(dict[k]));
                ++k;
            }
            sequences.add(tempList);
        }
        return sequences;
    }
}
