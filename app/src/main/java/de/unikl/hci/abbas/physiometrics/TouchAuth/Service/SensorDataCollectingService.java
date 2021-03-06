package de.unikl.hci.abbas.physiometrics.TouchAuth.Service;

/**
 * Created by abbas on 12/31/17.
 */

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;

import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.SensorFeatureExtraction;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.FileUtils;
import de.unikl.hci.abbas.physiometrics.TouchAuth.Util.DataUtils;

import java.util.ArrayList;
import java.util.List;

public class SensorDataCollectingService extends Service implements SensorEventListener {
    public static double INTERVAL = 10;
    public static double WINDOW_INTERVAL = 2;
    private static int MAX_GROUP_COUNT = 4;
    public final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auth/Sensor/";
    public final String accDir = dir + "Acc/";
    public final String oriDir = dir + "Ori/";
    public final String magDir = dir + "Mag/";
    public final String gyrDir = dir + "Gyr/";
    public final String featureVectorsFilename = dir + "FeatureVectors.txt";

    private SensorManager sensorManager;
    private double[] gravity = {0, 0, 9.8};

    private List<List<Double>> accRawData = new ArrayList<>();
    private List<List<Double>> oriRawData = new ArrayList<>();
    private List<List<Double>> magRawData = new ArrayList<>();
    private List<List<Double>> gyrRawData = new ArrayList<>();

    private List<List<Double>> accTempData = new ArrayList<>();
    private List<List<Double>> oriTempData = new ArrayList<>();
    private List<List<Double>> magTempData = new ArrayList<>();
    private List<List<Double>> gyrTempData = new ArrayList<>();

    private int groupCount = 0;

    public SensorDataCollectingService() {
    }

    public static void collect() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        FileUtils.makeRootDirectory(accDir);
        FileUtils.makeRootDirectory(oriDir);
        FileUtils.makeRootDirectory(magDir);
        FileUtils.makeRootDirectory(gyrDir);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Long startTime = System.currentTimeMillis();
                groupCount = 0;
                try {
                    while (true) {
                        Long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > INTERVAL * 1000) {
                            startTime = currentTime;

                            List<List<Double>> accData = accRawData;
                            accRawData = new ArrayList<>();
                            List<List<Double>> oriData = oriRawData;
                            oriRawData = new ArrayList<>();
                            List<List<Double>> magData = magRawData;
                            magRawData = new ArrayList<>();
                            List<List<Double>> gyrData = gyrRawData;
                            gyrRawData = new ArrayList<>();

                            accTempData.addAll(accData);
                            oriTempData.addAll(oriData);
                            magTempData.addAll(magData);
                            gyrTempData.addAll(gyrData);
                            ++groupCount;

                            if (groupCount == MAX_GROUP_COUNT) {

                                groupCount = 0;

                                double[][] acc = DataUtils.listToArray(accTempData);
                                double[][] ori = DataUtils.listToArray(oriTempData);
                                double[][] mag = DataUtils.listToArray(magTempData);
                                double[][] gyr = DataUtils.listToArray(gyrTempData);

                                saveRawFile(currentTime, acc, ori, mag, gyr);

                                final double[][] featureVectors = SensorFeatureExtraction.extract(
                                        INTERVAL * MAX_GROUP_COUNT,
                                        2, acc, ori, mag, gyr);

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (double[] line : featureVectors) {
                                            FileUtils.writeFileFromNums(featureVectorsFilename, line, true, false, 1);
                                        }
                                    }
                                }).start();

                                accTempData = new ArrayList<>();
                                oriTempData = new ArrayList<>();
                                magTempData = new ArrayList<>();
                                gyrTempData = new ArrayList<>();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Long currentTime = event.timestamp;
        List<Double> data = new ArrayList<>();
        data.add(currentTime + .0);
        data.add(event.values[0] + .0);
        data.add(event.values[1] + .0);
        data.add(event.values[2] + .0);

        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    String a, b, c, s;
                    final double alpha = 0.8;
                    List<Double> linear_acceleration = new ArrayList<>();
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                    linear_acceleration.add(currentTime + .0);
                    linear_acceleration.add(event.values[0] - gravity[0]);
                    linear_acceleration.add(event.values[1] - gravity[1]);
                    linear_acceleration.add(event.values[2] - gravity[2]);
                    accRawData.add(linear_acceleration);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    oriRawData.add(data);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magRawData.add(data);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyrRawData.add(data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void saveRawFile(final Long currentTime,
                             final double[][] acc,
                             final double[][] ori,
                             final double[][] mag,
                             final double[][] gyr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String accFilename = accDir + currentTime + ".txt";
                String oriFilename = oriDir + currentTime + ".txt";
                String magFilename = magDir + currentTime + ".txt";
                String gyrFilename = gyrDir + currentTime + ".txt";
                for (double[] line : acc) {
                    FileUtils.writeFileFromNums(accFilename, line, true, false, 1);
                }
                for (double[] line : ori) {
                    FileUtils.writeFileFromNums(oriFilename, line, true, false, 1);
                }
                for (double[] line : mag) {
                    FileUtils.writeFileFromNums(magFilename, line, true, false, 1);
                }
                for (double[] line : gyr) {
                    FileUtils.writeFileFromNums(gyrFilename, line, true, false, 1);
                }
            }
        }).start();
    }

    private double mean(double[] vector) {
        double mean = 0;
        for (double value : vector) {
            mean += value;
        }
        mean /= vector.length;
        return mean;
    }

    private double var(double[] vector) {
        double var = 0;
        double mean = mean(vector);
        for (double value : vector) {
            var += Math.pow(value - mean, 2);
        }
        var /= vector.length;
        return var;
    }
}
