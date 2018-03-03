package de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData;

/**
 * Created by abbas on 2/16/18.
 */

import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.FeatureVector;

import java.util.LinkedList;
import java.util.Queue;

public class TempData {
    public static Queue<FeatureVector> featureVectors = new LinkedList<>();

    public static FeatureVector getFeature() {
        return featureVectors.remove();
    }

    public static void clear() {
        featureVectors.clear();
    }
}
