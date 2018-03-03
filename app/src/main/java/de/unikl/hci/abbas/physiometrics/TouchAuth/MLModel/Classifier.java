package de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel;

/**
 * Created by abbas on 2/17/18.
 */

import java.util.List;

public abstract class Classifier {
    public abstract boolean train(List<FeatureVector> featureVectors);

    public abstract int classify(FeatureVector featureVector);
}
