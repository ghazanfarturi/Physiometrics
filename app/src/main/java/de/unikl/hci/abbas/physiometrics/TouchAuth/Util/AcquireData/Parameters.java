package de.unikl.hci.abbas.physiometrics.TouchAuth.Util.AcquireData;

/**
 * Created by abbas on 2/16/18.
 */

import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.FileUtils;

public class Parameters {
    public static final int DATANUM = 30;
    public static final long RUNPERIOC = 1000;

    // Set the operating status parameters, start the time to run, or pause
    public static boolean runing_state = true;

    /* Set the feature vector state to be written,
    0 is to obtain the feature vector to be classified or add a positive eigenvector;
    1 is to add a negative feature vector */
    public static int Write_FeatureVector_State = 0;

    public static boolean enoughData(String filename) {
        int num = FileUtils.readFeatureNum(filename);
        return num >= Parameters.DATANUM;
    }

    public static int getDatanum(String filename) {
        return FileUtils.readFeatureNum(filename);
    }
}
