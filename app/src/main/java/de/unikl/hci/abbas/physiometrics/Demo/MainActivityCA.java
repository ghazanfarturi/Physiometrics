package de.unikl.hci.abbas.physiometrics.Demo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import de.unikl.hci.abbas.physiometrics.R;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.TextFile;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel.LDA;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.MLModel.NGramModel;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Service.TempService;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Service.DataService;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.DataProcessing;
import de.unikl.hci.abbas.physiometrics.ContinuousAuth.Util.ROC;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.Assessment;
import de.unikl.hci.abbas.physiometrics.TouchAuth.MLModel.KMeans;

public class MainActivityCA extends Activity implements SensorEventListener {

    private static final int RATE = SensorManager.SENSOR_DELAY_FASTEST;
    public static int MODEL_NUM = 17;
    public static String rootPath;
    public static String fileNameAccelerometer;
    public static String fileNameOrientation;
    public static String fileNameMagnetic;
    public static String fileNameGyroscope;
    public static String fileNameWindows;
    public static String fileNameFeatureVectors;
    public static String fileNameCentroids;
    public static String fileNameModels;
    public static String fileNameService;
    Handler handlerBuild = new Handler();
    Handler handlerClaTest = new Handler();
    private SensorManager sensorManagerTemp;
    private LinearLayout layoutIdentification;
    private LinearLayout layoutBuildModels;
    private LinearLayout layoutTestModels;
    private LinearLayout layoutAuthentication;
    private EditText editBuildTotalTime;
    private EditText editBuildWindowTime;
    private EditText editBuildCentroids;
    private Button buttonStart;
    private Button buttonClaTestStart;
    private Button buttonAuthStart;
    private Button buttonAuthStop;
    private Switch switchAuthTest;
    private ImageView imageViewExperiment;
    private ImageView imageBuild;
    private ImageView imageAuthTraining;
    private Spinner spinnerBuildGroup;
    private CheckBox checkBuildNewData;
    private CheckBox checkTestNewData;
    private int timeRemain = 0;
    Runnable runnableBuild = new Runnable() {
        @Override
        public void run() {
            if (timeRemain == 0) {
                buttonStart.setClickable(true);
                Message message = new Message();
                message.what = 1;
                handlerStop.sendMessage(message);
            }
            buttonStart.setText("" + timeRemain);
            handlerBuild.postDelayed(this, 1000);
            --timeRemain;
        }
    };
    Runnable runnableClaTest = new Runnable() {
        @Override
        public void run() {
            if (timeRemain == 0) {
                buttonClaTestStart.setClickable(true);
                Message message = new Message();
                message.what = 2;
                handlerStop.sendMessage(message);
            }
            buttonClaTestStart.setText("" + timeRemain);
            handlerClaTest.postDelayed(this, 1000);
            --timeRemain;
        }
    };
    final Handler handlerStop = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                buttonStart.setText(R.string.buttonStart);
                timeRemain = 0;
                handlerBuild.removeCallbacks(runnableBuild);
            }
            if (msg.what == 2) {
                buttonClaTestStart.setText(R.string.buttonStart);
                timeRemain = 0;
                handlerClaTest.removeCallbacks(runnableClaTest);
                testIdentification();
            }
            super.handleMessage(msg);
        }
    };
    private double[] gravity = {0, 0, 9.8};
    private boolean isAuth = false;
    private LDA[] lda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ca);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Switch switchExperiment;
        Switch switchBuild;

        sensorManagerTemp = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManagerTemp.registerListener(this,
                sensorManagerTemp.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);

        rootPath = Environment.getExternalStorageDirectory().getPath() + "/SenSec/";
        TextFile.makeRootDirectory(rootPath);
        fileNameAccelerometer = rootPath + "AccelerometerData.txt";
        fileNameOrientation = rootPath + "OrientationData.txt";
        fileNameMagnetic = rootPath + "MagneticData.txt";
        fileNameGyroscope = rootPath + "GyroscopeData.txt";
        fileNameWindows = rootPath + "Windows.txt";
        fileNameFeatureVectors = rootPath + "FeatureVectors.txt";
        fileNameCentroids = rootPath + "Centroids.txt";
        fileNameModels = rootPath + "Models.txt";
        fileNameService = rootPath + "Service.txt";

        layoutIdentification = findViewById(R.id.layoutIdentification);
        layoutBuildModels = findViewById(R.id.layoutBuildModels);
        layoutTestModels = findViewById(R.id.layoutTestModels);
        layoutAuthentication = findViewById(R.id.layoutAuthentication);
        editBuildTotalTime = findViewById(R.id.editBuildTotalTime);
        editBuildWindowTime = findViewById(R.id.editBuildWindowTime);
        editBuildCentroids = findViewById(R.id.editBuildCentroids);
        buttonStart = findViewById(R.id.buttonStart);
        buttonClaTestStart = findViewById(R.id.buttonClaTestStart);
        buttonAuthStart = findViewById(R.id.buttonAuthStart);
        buttonAuthStop = findViewById(R.id.buttonAuthStop);
        imageViewExperiment = findViewById(R.id.imageViewExperiment);
        imageBuild = findViewById(R.id.imageBuild);
        imageAuthTraining = findViewById(R.id.imageAuthTest);
        switchExperiment = findViewById(R.id.switchExperiment);
        switchBuild = findViewById(R.id.switchBuild);
        switchAuthTest = findViewById(R.id.switchAuthTest);
        spinnerBuildGroup = findViewById(R.id.spinnerBuildGroup);
        checkBuildNewData = findViewById(R.id.checkBuildNewData);
        checkTestNewData = findViewById(R.id.checkTestNewData);

        switchExperiment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageViewExperiment.setImageResource(R.drawable.experimentnotice1);
                    layoutAuthentication.setVisibility(View.INVISIBLE);
                    layoutIdentification.setVisibility(View.VISIBLE);
                } else {
                    imageViewExperiment.setImageResource(R.drawable.experimentnotice2);
                    layoutIdentification.setVisibility(View.INVISIBLE);
                    layoutAuthentication.setVisibility(View.VISIBLE);
                }
            }
        });
        switchBuild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageBuild.setImageResource(R.drawable.trainmodels);
                    layoutTestModels.setVisibility(View.INVISIBLE);
                    layoutBuildModels.setVisibility(View.VISIBLE);
                } else {
                    imageBuild.setImageResource(R.drawable.testmodels);
                    layoutBuildModels.setVisibility(View.INVISIBLE);
                    layoutTestModels.setVisibility(View.VISIBLE);
                }
            }
        });
        switchAuthTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageAuthTraining.setImageResource(R.drawable.testmodel);
                } else {
                    imageAuthTraining.setImageResource(R.drawable.trainmodel);
                }
            }
        });

        for (int i = 1; i < 6; ++i) {
            String string = "";
            try {
                string = TextFile.readFile(rootPath + "/Models/group" + i + ".txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Objects.equals(string, "")) {
                try {
                    TextFile.writeFile(rootPath + "/Models/group" + i + ".txt", "", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //HMM.test();
        //TestClass.geodesicDistance();
        //sensorManagerTemp.unregisterListener(this);
        //HMM.newTest();
        //HMM.newNewTest();
        //ROC.result();
        //TestClass.tTest();
        //TestClass.tTest(1);
        //TestClass.tTest(2);
        //TestClass.tTest(3);
        //TestClass.pearson(1);
        //TestClass.pearson(2);
        //TestClass.pearson(3);
        //TestClass.trans();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double alpha = 0.8;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
    }

    public void onButtonStartClick(View view) {
        sensorManagerTemp.unregisterListener(this);

        if (checkBuildNewData.isChecked()) {

            Bundle bundle = new Bundle();
            bundle.putString("totalTime", editBuildTotalTime.getText().toString());
            bundle.putString("windowTime", editBuildWindowTime.getText().toString());
            bundle.putString("centroids", editBuildCentroids.getText().toString());
            bundle.putString("gravity0", "" + gravity[0]);
            bundle.putString("gravity1", "" + gravity[1]);
            bundle.putString("gravity2", "" + gravity[2]);
            bundle.putString("group", spinnerBuildGroup.getSelectedItem().toString());
            bundle.putString("isNewData", "true");
            bundle.putString("isTest", "false");
            bundle.putString("isAuth", "" + isAuth);
            bundle.putString("isAuthTest", "false");
            bundle.putString("currentTime", "0");
            Intent intent = new Intent();
            intent.setClass(MainActivityCA.this, DataService.class);
            intent.putExtras(bundle);
            startService(intent);

            timeRemain = Integer.parseInt(editBuildTotalTime.getText().toString());
            buttonStart.setClickable(false);
            handlerBuild.postDelayed(runnableBuild, 1000);
        } else {

            for (int n = 1; n <= MODEL_NUM; ++n) {
                String tempRootPath = rootPath + "/group" + n + "/";
                String[] fileNameAccList = TextFile.listFile(tempRootPath + "/Accelerometer");
                String[] fileNameOriList = TextFile.listFile(tempRootPath + "/Orientation");
                String[] fileNameMagList = TextFile.listFile(tempRootPath + "/Magnetic");
                String[] fileNameGyrList = TextFile.listFile(tempRootPath + "/Gyroscope");

                int minLength = min(fileNameAccList.length, fileNameOriList.length,
                        fileNameMagList.length, fileNameGyrList.length);
                int trainDataLength = 1200;

                for (int i = 0; i < minLength; ++i) {
                    DataProcessing dataProcessing = new DataProcessing();

                    dataProcessing.readData(fileNameAccList[i], fileNameOriList[i],
                            fileNameMagList[i], fileNameGyrList[i], 10);

                    dataProcessing.getWindows(10, 2);
                    double[][] featureVectors = dataProcessing.getAllFeatureVectors();

                    featureVectors = DataProcessing.clean(featureVectors);

                    String stringFeatureVectors = "";
                    for (double[] v : featureVectors) {
                        for (double w : v) {
                            stringFeatureVectors = stringFeatureVectors + w + "\t";
                        }
                        stringFeatureVectors = stringFeatureVectors + "\r";
                    }
                    try {
                        if (i < trainDataLength) {
                            TextFile.writeFile(tempRootPath + "/FeatureVectors.txt", stringFeatureVectors, true);
                        } else {
                            TextFile.writeFile(rootPath + "/new/group" + n + ".txt", stringFeatureVectors, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String rawFeatureVectors = "";
                try {
                    rawFeatureVectors = TextFile.readFile(tempRootPath + "/FeatureVectors.txt");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String[] lines = rawFeatureVectors.split("\r");
                double[][] featureVectors = new double[lines.length][lines[0].split("\t").length];
                for (int i = 0; i < lines.length; ++i) {
                    String[] values = lines[i].split("\t");
                    for (int j = 0; j < values.length; ++j) {
                        featureVectors[i][j] = Double.parseDouble(values[j]);
                    }
                }

                //////////////////////////////////////////////////////////////////////////////////////////
                //If Haven't Clustered

                Assessment assessment;
                assessment = KMeans.kMeansPlusPlus(featureVectors, 120);

                //If Haven't Clustered
                //////////////////////////////////////////////////////////////////////////////////////////

                //////////////////////////////////////////////////////////////////////////////////////////
                //If Have Clustered

                /*Assessment assessment = new Assessment();
                String rawCentroids = "";
                try {
                    rawCentroids = TextFile.readFile(tempRootPath + "/Centroids.txt");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String[] centroidsLines = rawCentroids.split("\r");
                assessment.centroids = new double[centroidsLines.length][centroidsLines[0].split("\t").length];
                for (int i = 0; i < centroidsLines.length; ++i) {
                    String[] values = centroidsLines[i].split("\t");
                    for (int j = 0; j < values.length; ++j) {
                        assessment.centroids[i][j] = Double.parseDouble(values[j]);
                    }
                }
                assessment.clusterMark = NGramModel.buildDictionary(assessment.centroids, featureVectors);*/

                //If Have Clustered
                //////////////////////////////////////////////////////////////////////////////////////////

                String stringCentroids = "";
                for (double[] v : assessment.centroids) {
                    for (double w : v) {
                        stringCentroids = stringCentroids + w + "\t";
                    }
                    stringCentroids = stringCentroids + "\r";
                }
                try {
                    TextFile.writeFile(tempRootPath + "Centroids.txt", stringCentroids, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                NGramModel nGramModel = new NGramModel(2, assessment.centroids, featureVectors);
                double[][] model = nGramModel.getModel();

                String stringModel = "";
                for (double[] v : model) {
                    for (double w : v) {
                        stringModel = stringModel + w + "\t";
                    }
                    stringModel = stringModel + "\r";
                }
                try {
                    TextFile.writeFile(rootPath + "/Models/group" + n + ".txt", stringModel, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            testIdentification();
        }
    }

    public void onButtonClaTestStartClick(View view) {
        sensorManagerTemp.unregisterListener(this);

        if (checkTestNewData.isChecked()) {

            String totalTime = ((EditText) findViewById(R.id.editTestTotalTime)).getText().toString();

            Bundle bundle = new Bundle();
            bundle.putString("totalTime", totalTime);
            bundle.putString("windowTime", "" + 2);
            bundle.putString("centroids", "" + 15);
            bundle.putString("gravity0", "" + gravity[0]);
            bundle.putString("gravity1", "" + gravity[1]);
            bundle.putString("gravity2", "" + gravity[2]);
            bundle.putString("group", "new");
            bundle.putString("isNewData", "true");
            bundle.putString("isTest", "true");
            bundle.putString("isAuth", "" + isAuth);
            bundle.putString("isAuthTest", "false");
            bundle.putString("currentTime", "0");
            Intent intent = new Intent();
            intent.setClass(MainActivityCA.this, DataService.class);
            intent.putExtras(bundle);
            startService(intent);

            buttonClaTestStart.setClickable(false);
            timeRemain = Integer.parseInt(totalTime);
            handlerClaTest.postDelayed(runnableClaTest, 1000);
        } else {

            /*String tempRootPath = rootPath + "/new/";
            String[] fileNameAccList = TextFile.listFile(tempRootPath + "/Accelerometer");
            String[] fileNameOriList = TextFile.listFile(tempRootPath + "/Orientation");
            String[] fileNameMagList = TextFile.listFile(tempRootPath + "/Magnetic");
            String[] fileNameGyrList = TextFile.listFile(tempRootPath + "/Gyroscope");
            int minLength = min(fileNameAccList.length, fileNameOriList.length,
                    fileNameMagList.length, fileNameGyrList.length);
            for (int i = 0; i < minLength; ++i) {
                DataProcessing dataProcessing = new DataProcessing();
                dataProcessing.readData(fileNameAccList[i], fileNameOriList[i],
                        fileNameMagList[i], fileNameGyrList[i], 10);
                dataProcessing.getWindows(10, Integer.parseInt(editBuildWindowTime.getText().toString()));
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
                    TextFile.writeFile(tempRootPath + "/FeatureVectors.txt", stringFeatureVectors, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

            testIdentification();
        }
    }

    private void testIdentification() {

        boolean[] isModelExist = new boolean[MODEL_NUM + 1];
        NGramModel[] models = new NGramModel[MODEL_NUM + 1];
        double[][][] centroids = new double[MODEL_NUM + 1][][];
        NGramModel.N = 2;

        for (int modelId = 1; modelId <= MODEL_NUM; ++modelId) {

            double[][] model;
            String rawModel = "", rawCentroids = "";
            try {
                rawModel = TextFile.readFile(rootPath + "/Models/group" + modelId + ".txt");
                rawCentroids = TextFile.readFile(rootPath + "/group" + modelId + "/Centroids.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!Objects.equals(rawModel, "")) {

                isModelExist[modelId] = true;
                String[] lines = rawModel.split("\r");
                model = new double[lines.length][lines[0].split("\t").length];
                for (int i = 0; i < lines.length; ++i) {
                    String[] value = lines[i].split("\t");
                    for (int j = 0; j < value.length; ++j) {
                        model[i][j] = Double.parseDouble(value[j]);
                    }
                }
                if (!Objects.equals(rawCentroids, "")) {
                    lines = rawCentroids.split("\r");
                    centroids[modelId] = new double[lines.length][lines[0].split("\t").length];
                    for (int i = 0; i < lines.length; ++i) {
                        String[] value = lines[i].split("\t");
                        for (int j = 0; j < value.length; ++j) {
                            centroids[modelId][i][j] = Double.parseDouble(value[j]);
                        }
                    }
                }
                models[modelId] = new NGramModel(model);

            } else {
                isModelExist[modelId] = false;
            }
        }

        String[][] result = new String[MODEL_NUM + 1][MODEL_NUM + 1];

        for (int sampleId = 1; sampleId <= MODEL_NUM; ++sampleId) {

            double[][] sample = new double[1][1];

            String rawSample = "";
            try {
                rawSample = TextFile.readFile(rootPath + "/new/group" + sampleId + ".txt");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!Objects.equals(rawSample, "")) {
                String[] lines = rawSample.split("\r");
                sample = new double[lines.length][lines[0].split("\t").length];
                for (int i = 0; i < sample.length; ++i) {
                    String[] value = lines[i].split("\t");
                    for (int j = 0; j < value.length; ++j) {
                        if (!Objects.equals(value[j], "")) {
                            sample[i][j] = Double.parseDouble(value[j]);
                        }
                    }
                }
            }


            for (int m = 2; m <= 512; m *= 2) {
                for (int w = 0; w < sample.length - m; w = w + m / 2) {
                    double[][] nowSample = new double[m][sample[0].length];
                    System.arraycopy(sample, w, nowSample, 0, m);
                    double[] probabilities = new double[MODEL_NUM + 1];
                    for (int modelId = 1; modelId <= MODEL_NUM; ++modelId) {
                        if (isModelExist[modelId]) {
                            //double[] tempProbabilities = models[k].getProbabilities(nowSample, lda[k]);
                            double[] tempProbabilities = models[modelId].getProbabilities(centroids[modelId], nowSample);
                            probabilities[modelId] = probabilityCalc(tempProbabilities);
                            if (result[sampleId][modelId] == null) {
                                result[sampleId][modelId] = probabilities[modelId] + "\t";
                            } else {
                                result[sampleId][modelId] = result[sampleId][modelId] + probabilities[modelId] + "\t";
                            }
                        } else {
                            probabilities[modelId] = -Double.MAX_VALUE;
                        }
                    }

                }
                for (int k = 1; k <= MODEL_NUM; ++k) {
                    result[sampleId][k] += "\r";
                }
            }
        }

        try {
            for (int i = 1; i < result.length; ++i) {
                for (int j = 1; j < result[i].length; ++j) {
                    TextFile.writeFile(rootPath + "/" + i + " - " + j + ".txt", result[i][j], false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ROC.result();
    }

    private double probabilityCalc(double[] probabilities) {
        double sum = 0;
        for (double probability : probabilities) {
            sum = sum + Math.log10(probability);
        }
        return sum;
    }

    public int maxIndex(double[] vector, int start, int end) {
        if (start >= 0 && end < vector.length) {
            double max = vector[start];
            int index = 1;
            for (int i = start; i <= end; ++i) {
                if (max <= vector[i]) {
                    max = vector[i];
                    index = i;
                }
            }
            return index;
        } else {
            return 0;
        }
    }

    public int max(double a, double b, double c) {
        double max = a;
        int index = 1;
        if (max < b) {
            max = b;
            index = 2;
        }
        if (max < c) {
            index = 3;
        }
        return index;
    }

    public int min(int a, int b, int c, int d) {
        int ans = a;
        if (ans > b) {
            ans = b;
        }
        if (ans > c) {
            ans = c;
        }
        if (ans > d) {
            ans = d;
        }
        return ans;
    }

    public double[][] add(double[][] a, double[][] b) {
        double[][] ans = new double[a.length + b.length][a[0].length];
        System.arraycopy(a, 0, ans, 0, a.length);
        System.arraycopy(b, 0, ans, a.length, b.length);
        return ans;
    }

    public void onButtonAuthStartClick(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long currentTime = System.currentTimeMillis();
        String time = sdf.format(currentTime);
        buttonAuthStart.setClickable(false);
        buttonAuthStop.setClickable(true);
        buttonAuthStart.setText(time);
        isAuth = true;

        TextFile.makeRootDirectory(rootPath);
        TextFile.makeRootDirectory(rootPath + "Authentication/");
        try {
            TextFile.writeFile(rootPath + "Authentication/isNeedRun", "true", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putString("totalTime", "" + 30);
        bundle.putString("windowTime", "" + 2);
        bundle.putString("centroids", "" + 15);
        bundle.putString("gravity0", "" + gravity[0]);
        bundle.putString("gravity1", "" + gravity[1]);
        bundle.putString("gravity2", "" + gravity[2]);
        bundle.putString("group", "new");
        bundle.putString("isNewData", "true");
        bundle.putString("isTest", "true");
        bundle.putString("isAuth", "" + isAuth);
        bundle.putString("isAuthTest", "" + switchAuthTest.isChecked());
        bundle.putString("currentTime", "" + currentTime);
        Intent intent = new Intent();
        intent.setClass(MainActivityCA.this, DataService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    public void onButtonAuthStopClick(View view) {
        buttonAuthStart.setClickable(true);
        buttonAuthStart.setText(R.string.buttonAuthStart);
        buttonAuthStop.setClickable(false);
        isAuth = true;
        TextFile.makeRootDirectory(rootPath);
        TextFile.makeRootDirectory(rootPath + "Authentication/");
        try {
            TextFile.writeFile(rootPath + "Authentication/isNeedRun", "false", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setClass(MainActivityCA.this, TempService.class);
        startService(intent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo =
                pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityInfo ai = homeInfo.activityInfo;
            Intent startIntent = new Intent(Intent.ACTION_MAIN);
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
            startActivitySafely(startIntent);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do Nothing.
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManagerTemp.unregisterListener(this);
    }

    public void onEditBuildTotalTimeClick(View view) {
        editBuildTotalTime.setSelectAllOnFocus(true);
    }

    public void onEditBuildWindowTimeClick(View view) {
        editBuildWindowTime.setSelectAllOnFocus(true);
    }

    public void onEditBuildCentroidsClick(View view) {
        editBuildCentroids.setSelectAllOnFocus(true);
    }
}
