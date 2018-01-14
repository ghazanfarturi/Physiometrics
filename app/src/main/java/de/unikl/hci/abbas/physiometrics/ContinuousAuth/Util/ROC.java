package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util;

/**
 * Created by abbas on 1/1/18.
 */

import android.util.Log;

import de.unikl.hci.abbas.physiometrics.Demo.MainActivityCA;

public class ROC {

    public static void result() {
        for (int i = 0; i < 5; ++i) {
            double tmp = preProcessing(6, i);
            System.out.print(3 + " - " + i + " : " + tmp + "\n\n");
        }
    }

    public static double preProcessing(int sampleId, int windowId) {
        String stringPositive = "";
        String[] stringNegative = new String[MainActivityCA.MODEL_NUM - 1];
        try {
            stringPositive = TextFile.readFile(MainActivityCA.rootPath + sampleId + " - " + sampleId + ".txt");
            int k = 0;
            for (int i = 1; i <= MainActivityCA.MODEL_NUM; ++i) {
                if (i != sampleId) {
                    stringNegative[k] = TextFile.readFile(MainActivityCA.rootPath + sampleId + " - " + i + ".txt");
                    ++k;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sp = stringPositive.split("\r")[windowId];
        String sn = "";
        for (String s : stringNegative) {
            sn += s.split("\r")[windowId];
        }
        String[] spValues = sp.split("\t");
        String[] snValues = sn.split("\t");
        double[] positive = new double[spValues.length];
        double[] negative = new double[snValues.length];
        for (int i = 0; i < positive.length; ++i) {
            positive[i] = Double.parseDouble(spValues[i]);
        }
        for (int i = 0; i < negative.length; ++i) {
            negative[i] = Double.parseDouble(snValues[i]);
        }

        switch (windowId) {
            case 0:
                roc(positive, negative, 3);
                break;
            case 1:
                roc(positive, negative, 9);
                break;
            case 2:
                roc(positive, negative, 20);
                break;
            case 3:
                roc(positive, negative, 40);
                break;
            case 4:
                roc(positive, negative, 80);
                break;
        }

        return EER(positive, negative);
    }

    public static double EER(double[] positive, double[] negative) {
        double pt, a = 0, b = -1e10;
        pt = (a + b) / 2;
        double frr;
        double far;
        while (Math.abs(a - b) > 1e-4) {
            frr = FRR(positive, pt);
            far = FAR(negative, pt);
            System.out.print("FAR: " + far + "\t");
            System.out.print("FRR: " + frr + "\n");
            if (frr > far) {
                a = pt;
            } else {
                b = pt;
            }
            pt = (a + b) / 2;
        }
        return pt;
    }

    public static void roc(double[] positive, double[] negative, double initPt) {
        double step = initPt / 80;
        double pt = -initPt;
        for (; pt <= 0; pt += step) {
            //Log.i("FRR", "" + FRR(positive, pt));
            //Log.i("RAR", "" + FAR(negative, pt));
            try {
                TextFile.writeFile(MainActivityCA.rootPath + initPt + "FRR" + ".txt", "" + FRR(positive, pt) + "\t", true);
                TextFile.writeFile(MainActivityCA.rootPath + initPt + "FAR" + ".txt", "" + FAR(negative, pt) + "\t", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double FRR(double[] ps, double pt) {
        int n = 0;
        for (double p : ps) {
            if (p < pt) {
                ++n;
            }
        }
        return 1.0 * n / ps.length;
    }

    public static double FAR(double[] ps, double pt) {
        int n = 0;
        for (double p : ps) {
            if (p > pt) {
                ++n;
            }
        }
        return 1.0 * n / ps.length;
    }
}
