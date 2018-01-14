package de.unikl.hci.abbas.physiometrics.ContinuousAuth.Service;

/**
 * Created by abbas on 1/1/18.
 */

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.EOFException;
import java.text.DecimalFormat;
import java.util.Objects;

import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.TextFile;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.DataProcessing;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel.NGramModel;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.KMeans;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.Assessment;
import de.unikl.hci.abbas.physiometrics.Demo.MainActivityCA;

public class DataService extends IntentService implements SensorEventListener {

    public static int timePerStep = 10;
    Handler handler = new Handler();
    Handler handlerAuth = new Handler();
    private SensorManager sensorManager;
    private int remainTime = 0;
    private int initializingTime = 0;
    private int numberOfCentroids = 5;
    private int fileNameId = 0;
    private long startTime = 0;
    private double timePerWindow = 2;
    private double[] gravity;
    private boolean isTest;
    private boolean isAuth;
    private boolean isAuthTest;
    private String fileNameAccNow;
    private String fileNameOriNow;
    private String fileNameMagNow;
    private String fileNameGyrNow;
    private String fileNameFeatureVectors;
    private String fileNameCentroids;
    private String fileNameModels;
    private String rootPath;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (((initializingTime - remainTime) % timePerStep == 0
                    && initializingTime != remainTime) || remainTime == 0) {
                if (remainTime != 0) {
                    dataProcessing(fileNameAccNow, fileNameOriNow, fileNameMagNow,
                            fileNameGyrNow, timePerStep, timePerWindow);
                    ++fileNameId;
                    /*fileNameAccNow = rootPath + "Accelerometer/" + fileNameId + ".txt";
                    fileNameOriNow = rootPath + "Orientation/" + fileNameId + ".txt";
                    fileNameMagNow = rootPath + "Magnetic/" + fileNameId + ".txt";
                    fileNameGyrNow = rootPath + "Gyroscope/" + fileNameId + ".txt";*/

                    fileNameAccNow = rootPath + "Accelerometer/" + System.currentTimeMillis() + ".txt";
                    fileNameOriNow = rootPath + "Orientation/" + System.currentTimeMillis() + ".txt";
                    fileNameMagNow = rootPath + "Magnetic/" + System.currentTimeMillis() + ".txt";
                    fileNameGyrNow = rootPath + "Gyroscope/" + System.currentTimeMillis() + ".txt";

                } else {
                    int totalTime = initializingTime % timePerStep;
                    dataProcessing(fileNameAccNow, fileNameOriNow, fileNameMagNow,
                            fileNameGyrNow, totalTime, timePerWindow, numberOfCentroids);
                    Message message = new Message();
                    message.what = 1;
                    handlerStop.sendMessage(message);
                }
            }
            handler.postDelayed(this, 1000);
            --remainTime;
        }
    };
    Runnable runnableAuth = new Runnable() {
        @Override
        public void run() {
            boolean isNeedRun = true;
            try {
                isNeedRun = Boolean.parseBoolean(TextFile.readFile(rootPath + "isNeedRun"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((System.currentTimeMillis() - startTime) <= 86400000 && isNeedRun) {
                if ((System.currentTimeMillis() - startTime) % 10000 <= 10) {
                    dataProcessing(fileNameAccNow, fileNameOriNow, fileNameMagNow,
                            fileNameGyrNow, timePerStep, timePerWindow);
                    ++fileNameId;
                    /*fileNameAccNow = rootPath + "Accelerometer/" + fileNameId + ".txt";
                    fileNameOriNow = rootPath + "Orientation/" + fileNameId + ".txt";
                    fileNameMagNow = rootPath + "Magnetic/" + fileNameId + ".txt";
                    fileNameGyrNow = rootPath + "Gyroscope/" + fileNameId + ".txt";*/

                    fileNameAccNow = rootPath + "Accelerometer/" + System.currentTimeMillis() + ".txt";
                    fileNameOriNow = rootPath + "Orientation/" + System.currentTimeMillis() + ".txt";
                    fileNameMagNow = rootPath + "Magnetic/" + System.currentTimeMillis() + ".txt";
                    fileNameGyrNow = rootPath + "Gyroscope/" + System.currentTimeMillis() + ".txt";


                }
            } else {
                dataProcessing(numberOfCentroids);
                Message message = new Message();
                message.what = 2;
                handlerStop.sendMessage(message);
            }
            handlerAuth.postDelayed(this, 1);
        }
    };
    final Handler handlerStop = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                remainTime = 0;
                fileNameId = 0;
                handler.removeCallbacks(runnable);
                stopSensorListener();
            }
            if (msg.what == 2) {
                fileNameId = 0;
                handlerAuth.removeCallbacks(runnableAuth);
                stopSensorListener();
            }
            super.handleMessage(msg);
        }
    };

    public DataService() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String groupName;
        boolean isNewData;
        Bundle bundle = intent.getExtras();
        remainTime = Integer.parseInt(bundle.getString("totalTime"));
        timePerWindow = Double.parseDouble(bundle.getString("windowTime"));
        numberOfCentroids = Integer.parseInt(bundle.getString("centroids"));
        initializingTime = remainTime;
        groupName = bundle.getString("group");
        isNewData = Boolean.parseBoolean(bundle.getString("isNewData"));
        isTest = Boolean.parseBoolean(bundle.getString("isTest"));
        isAuth = Boolean.parseBoolean(bundle.getString("isAuth"));
        isAuthTest = Boolean.parseBoolean(bundle.getString("iaAuthTest"));
        startTime = Long.parseLong(bundle.getString("currentTime"));
        gravity = new double[3];
        gravity[0] = Double.parseDouble(bundle.getString("gravity0"));
        gravity[1] = Double.parseDouble(bundle.getString("gravity1"));
        gravity[2] = Double.parseDouble(bundle.getString("gravity2"));
        fileNameId = 0;
        if (!isAuth) {
            if (isNewData) {
                rootPath = MainActivityCA.rootPath + groupName + "/";
            } else {
                rootPath = MainActivityCA.rootPath;
            }
            TextFile.makeRootDirectory(MainActivityCA.rootPath);
            TextFile.makeRootDirectory(rootPath);
            TextFile.makeRootDirectory(rootPath + "Accelerometer/");
            TextFile.makeRootDirectory(rootPath + "Orientation/");
            TextFile.makeRootDirectory(rootPath + "Magnetic/");
            TextFile.makeRootDirectory(rootPath + "Gyroscope/");
            TextFile.makeRootDirectory(MainActivityCA.rootPath + "Models/");

            /*fileNameAccNow = rootPath + "Accelerometer/" + fileNameId + ".txt";
            fileNameOriNow = rootPath + "Orientation/" + fileNameId + ".txt";
            fileNameMagNow = rootPath + "Magnetic/" + fileNameId + ".txt";
            fileNameGyrNow = rootPath + "Gyroscope/" + fileNameId + ".txt";*/

            fileNameAccNow = rootPath + "Accelerometer/" + System.currentTimeMillis() + ".txt";
            fileNameOriNow = rootPath + "Orientation/" + System.currentTimeMillis() + ".txt";
            fileNameMagNow = rootPath + "Magnetic/" + System.currentTimeMillis() + ".txt";
            fileNameGyrNow = rootPath + "Gyroscope/" + System.currentTimeMillis() + ".txt";

            fileNameFeatureVectors = rootPath + "FeatureVectors.txt";
            fileNameCentroids = rootPath + "Centroids.txt";
            fileNameModels = MainActivityCA.rootPath + "/Models/" + groupName + ".txt";

            if (isNewData) {

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);

                handler.postDelayed(runnable, 1000);

            }
        } else {
            rootPath = MainActivityCA.rootPath + "Authentication/";
            TextFile.makeRootDirectory(MainActivityCA.rootPath);
            TextFile.makeRootDirectory(rootPath);
            TextFile.makeRootDirectory(rootPath + "Accelerometer/");
            TextFile.makeRootDirectory(rootPath + "Orientation/");
            TextFile.makeRootDirectory(rootPath + "Magnetic/");
            TextFile.makeRootDirectory(rootPath + "Gyroscope/");
            /*fileNameAccNow = rootPath + "Accelerometer/" + fileNameId + ".txt";
            fileNameOriNow = rootPath + "Orientation/" + fileNameId + ".txt";
            fileNameMagNow = rootPath + "Magnetic/" + fileNameId + ".txt";
            fileNameGyrNow = rootPath + "Gyroscope/" + fileNameId + ".txt";*/

            fileNameAccNow = rootPath + "Accelerometer/" + System.currentTimeMillis() + ".txt";
            fileNameOriNow = rootPath + "Orientation/" + System.currentTimeMillis() + ".txt";
            fileNameMagNow = rootPath + "Magnetic/" + System.currentTimeMillis() + ".txt";
            fileNameGyrNow = rootPath + "Gyroscope/" + System.currentTimeMillis() + ".txt";


            fileNameFeatureVectors = rootPath + "FeatureVectors.txt";
            fileNameCentroids = rootPath + "Centroids.txt";
            fileNameModels = rootPath + "model.txt";

            try {
                TextFile.writeFile(rootPath + "isNeedRun", "true", false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);

            handlerAuth.postDelayed(runnableAuth, 1);
        }
    }

    private void stopSensorListener() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Long currentTime = event.timestamp;
        String t = currentTime.toString();
        String x = String.valueOf(event.values[0]);
        String y = String.valueOf(event.values[1]);
        String z = String.valueOf(event.values[2]);
        String temp = x + "\t" + y + "\t" + z + "\t" + t + "\r";
        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    String a, b, c, s;
                    final double alpha = 0.8;
                    double[] linear_acceleration = new double[3];
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                    linear_acceleration[0] = event.values[0] - gravity[0];
                    linear_acceleration[1] = event.values[1] - gravity[1];
                    linear_acceleration[2] = event.values[2] - gravity[2];
                    a = String.valueOf(linear_acceleration[0]);
                    b = String.valueOf(linear_acceleration[1]);
                    c = String.valueOf(linear_acceleration[2]);
                    s = a + "\t" + b + "\t" + c + "\t" + t + "\r";
                    TextFile.writeFile(fileNameAccNow, s, true);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    TextFile.writeFile(fileNameOriNow, temp, true);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    TextFile.writeFile(fileNameMagNow, temp, true);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    TextFile.writeFile(fileNameGyrNow, temp, true);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dataProcessing(String fileNameAcc, String fileNameOri, String fileNameMag,
                                String fileNameGyr, int totalTime, double windowTime) {

        DataProcessing dataProcessing = new DataProcessing();
        dataProcessing.readData(fileNameAcc, fileNameOri, fileNameMag, fileNameGyr, totalTime);
        dataProcessing.getWindows(totalTime, windowTime);
        double[][] featureVectors = dataProcessing.getAllFeatureVectors();
        featureVectors = DataProcessing.clean(featureVectors);

        DecimalFormat decimalFormat = new DecimalFormat("######0.000");
        String stringFeatureVectors = "";
        for (double[] v : featureVectors) {
            for (double w : v) {
                stringFeatureVectors = stringFeatureVectors + decimalFormat.format(w) + "\t";
            }
            stringFeatureVectors = stringFeatureVectors + "\r";
        }
        try {
            TextFile.writeFile(fileNameFeatureVectors, stringFeatureVectors, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dataProcessing(String fileNameAcc, String fileNameOri, String fileNameMag,
                                String fileNameGyr, int totalTime, double windowTime, int numOfCentroids) {

        dataProcessing(fileNameAcc, fileNameOri, fileNameMag, fileNameGyr, totalTime, windowTime);
        dataProcessing(numOfCentroids);
    }

    private void dataProcessing(int numOfCentroids) {
        String rawString = "";
        try {
            rawString = TextFile.readFile(fileNameFeatureVectors);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] lines = rawString.split("\r");
        double[][] featureVectors = new double[lines.length][lines[0].split("\t").length];
        for (int i = 0; i < featureVectors.length; ++i) {
            String[] value = lines[i].split("\t");
            for (int j = 0; j < featureVectors[0].length; ++j) {
                if (!Objects.equals(value[j], "")) {
                    featureVectors[i][j] = Double.parseDouble(value[j]);
                }
            }
        }

        if (!isTest || (isAuth && !isAuthTest)) {
            Assessment assessment;
            assessment = KMeans.kMeansPlusPlus(featureVectors, numOfCentroids);
            //KMeansClustering kMeansClustering;
            //kMeansClustering = new KMeansClustering(featureVectors);
            //double[][] centroids = kMeansClustering.kMeansPlusPlus(numOfCentroids);
            NGramModel.rankLength(assessment.centroids, 0, assessment.centroids.length - 1);

            DecimalFormat decimalFormat = new DecimalFormat("######0.000");

            String stringCentroids = "";
            for (double[] v : assessment.centroids) {
                for (double w : v) {
                    stringCentroids = stringCentroids + decimalFormat.format(w) + "\t";
                }
                stringCentroids = stringCentroids + "\r";
            }
            try {
                TextFile.writeFile(fileNameCentroids, stringCentroids, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            NGramModel nGramModel = new NGramModel(5, assessment.centroids.length, assessment.clusterMark);
            double[][] model = nGramModel.getModel();
            String stringModel = "";
            for (double[] v : model) {
                for (double w : v) {
                    stringModel = stringModel + decimalFormat.format(w) + "\t";
                }
                stringModel = stringModel + "\r";
            }
            try {
                TextFile.writeFile(fileNameModels, stringModel, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do Nothing.
    }
}
