package de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel;

/**
 * Created by abbas on 12/31/17.
 */

import java.util.concurrent.Callable;

public class TouchPostEventMethod implements Callable<Void> {
    protected TouchEvent event;
    protected StringBuilder sb;

    public void setParam(TouchEvent event, StringBuilder sb) {
        this.event = event;
        this.sb = sb;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
