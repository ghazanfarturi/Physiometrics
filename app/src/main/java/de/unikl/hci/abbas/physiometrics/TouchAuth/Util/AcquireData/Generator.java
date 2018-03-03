package de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData;

/**
 * Created by abbas on 2/16/18.
 */

import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.FeatureVector;

public abstract class Generator {
    protected FeatureVector fv;

    public abstract boolean process(Object ev);

    public FeatureVector getFeatureVector() {
        return fv;
    }
}
