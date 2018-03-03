package de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel;

/**
 * Created by abbas on 2/16/18.
 */

import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData.Dispatcher;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData.Parameters;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData.TempData;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.DataNormalization;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.FileUtils;

public class TouchAuth {
    public static boolean classifyFeatureVector = false;
    private static TouchAuth sTouchAuth = null;
    private static Classifier sClassifier = null;
    private static ArrayList<Integer> scoresList = new ArrayList<Integer>();
    String TAG = "TouchAuth";
    DecimalFormat df = new DecimalFormat("0.00");
    private Dispatcher mDispatcher;
    private boolean trainingFlag = false;

    public TouchAuth() {
        sTouchAuth = this;
    }

    public static TouchAuth getTouchAuth() {
        return sTouchAuth;
    }

    public Dispatcher getDispatcher() {
        return mDispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        mDispatcher = dispatcher;
    }

    public void useClassifier(Classifier classifier) {
        sClassifier = classifier;
    }

    public Classifier getClassifier() {
        return sClassifier;
    }

    public int getScores() {

        run();

        if (scoresList.size() >= 1) {

            return scoresList.remove(0);

        } else
            return 0;
    }

    public void run() {

        boolean isScored = true;

        while (Parameters.runing_state && isScored) {

            try {

                if (classifyFeatureVector) {

                    System.out.println("classify");
                    System.out.println("trainingFlag:" + trainingFlag);
                    int num = Parameters.getDatanum(FileUtils.FILE_FEATURE_NUM_NAME);

                    if (num == Parameters.DATANUM) {

                        if (!trainingFlag) {

                            List<FeatureVector> positive_listFv = FileUtils.readFeatureVectors(FileUtils.FILE_FEATUREVECTURE_NAME);
                            List<FeatureVector> negative_listFv = FileUtils.readFeatureVectors(FileUtils.FILE_NEGATIVE_FEATURE_NAME);

                            // Add the negative feature to the positive collection and merge all the features
                            positive_listFv.addAll(negative_listFv);
                            positive_listFv = DataNormalization.dataNormalization(positive_listFv);
                            sClassifier.train(positive_listFv);
                            System.out.println("a:" + positive_listFv.get(0).features.length);
                            trainingFlag = true;
                        }

                        FeatureVector fv = TempData.getFeature();

                        if (fv != null) {

                            fv = DataNormalization.fvNormalization(fv);
                            System.out.println("t:" + fv.features.length);
                            int classifyScore = sClassifier.classify(fv);
                            System.out.println("score:" + classifyScore);
                            scoresList.add(classifyScore);
                            Log.d(TAG, "DemoActivity Score: " + scoresList.toString());
                            isScored = false;
                        }
                    }

                    classifyFeatureVector = false;
                }

                // Thread.sleep(Parameters.RUNPERIOC);
                Thread.sleep(10);

            } catch (Exception e) {
                Log.e("DemoActivityException", "Exception");
                e.printStackTrace();
            }
        }
    }
}
