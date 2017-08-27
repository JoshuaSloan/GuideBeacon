package com.gimbal.hello_gimbal_android;


import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TestBedA extends Activity implements SensorEventListener, AsyncResponse {

    TextToSpeech T2S;
    SensorManager mSensorManager;    // device sensor manager
    public TextView degreeView, currentLocationView, nextLocationView, pathView, timer;
    //    Chronometer chronometer;
    ImageView image;    // define the display assembly compass picture
    Separation databaseSeparation = new Separation();
    ArrayToString ats = new ArrayToString();
    Routing routShortestPath = new Routing();
    RoutFinding rf = new RoutFinding();
    StringToArray sta = new StringToArray();
    private ArrayAdapter<String> listAdapter;
    PlaceManager placeManager;
    PlaceEventListener placeEventListener;
    //--------------------------------------------------------------------------------------------------
    String beaconBuffer = " ", fPath = " ", location, dataFromServer = " ", toSpeak = "", doors,
            DEBUG = "Envision", beaconInformation = " ";
    int numberOfSensors, currentBeaconNumber, current = -1, next = -1, bufferRouCounter = 0, destination;
    boolean reachFlag = false, dataFlag = false, speakFlag = false,
            firstTimeFlag = false, exceptionFlag = false, rightPathFlag = false;
    //--------------------------------------------------------------------------------------------------
    float currentDegree = 0f, degree;    // record the compass picture angle turned
    Long referenceTime;
    //--------------------------------------------------------------------------------------------------
    String[] str = new String[2];
    Long[] timesOfSignalsReceiving;
    int[] sensorsRSSI, beaconWeight;
    double[][][] path1;
    String[] separate;
    int[] bufferRou;
    String[] listOfSensorsName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gimbal.setApiKey(this.getApplication(), "1fc415f9-d2ca-49c1-96a6-65ce3dab0dfc");

        image = (ImageView) findViewById(R.id.imageViewCompass);// Compass image
        degreeView = (TextView) findViewById(R.id.degree);// Degree TextView
        currentLocationView = (TextView) findViewById(R.id.currentLocation);
        nextLocationView = (TextView) findViewById(R.id.nextLocation);
        pathView = (TextView) findViewById(R.id.pathFromSourceToDestination);
        timer = (TextView) findViewById(R.id.timerView);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        T2S = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    T2S.setLanguage(Locale.UK);
                }
            }
        });

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

//        String listOfLocations= getIntent().getStringExtra("key");
        String listOfLocations = "Beggs Hall Entrance : 7////Wines Lab : 0/Beggs Hall Entrance : 7" +
                "/NetApp : 1/Biomaterial and Bioengineering Lab : 4/power quality lab : 3" +
                "/Sustainable System Lab : 2/Quantum Computing lab : 5/Stair : 6/Stair : 8list ";

        str = listOfLocations.split("////");//In this function str is used as a buffer for saving data
        String[] str1 = str[0].split(":");
        destination = Integer.parseInt(str1[1].trim());
        str1 = str[1].split("/");
        numberOfSensors = str1.length;
        listOfSensorsName = new String[numberOfSensors];//This will save list of sensors name that exist
        String[] str2;
        for (int i = 0; i < numberOfSensors; i++) {
            str2 = str1[i].split(":");
            if (i == numberOfSensors - 1) {
                String[] str3 = str2[1].split("list");
                str2[1] = str3[0];
            }
            listOfSensorsName[Integer.parseInt(str2[1].trim())] = str2[0];
        }

        timesOfSignalsReceiving = new Long[numberOfSensors];//This is used to save sensors received times
        sensorsRSSI = new int[numberOfSensors];//This is used to save signals RSSI values
        bufferRou = new int[numberOfSensors]; //This is used as an array to save the path in int format
        beaconWeight = new int[numberOfSensors]; //This is used to give the weight to received signals
        //in order to choose the best signal which repeat more than the rest

        for (int i = 0; i < numberOfSensors; i++)
            Log.d(DEBUG + " List of sensors & pos", i + "->" + listOfSensorsName[i]);

        placeEventListener = new PlaceEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
                super.onBeaconSighting(beaconSighting, list);
                beaconInformation = beaconSighting.getBeacon().getName() + "&" +
                        Integer.toString(beaconSighting.getRSSI()) + "&" +
                        Long.toString(beaconSighting.getTimeInMillis());
                Log.d(DEBUG, beaconInformation);

            }
        };


        placeManager = PlaceManager.getInstance();
        placeManager.addListener(placeEventListener);
        placeManager.startMonitoring();

        CommunicationManager.getInstance().startReceivingCommunications();
        Thread1 th = new Thread1();
        th.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);// for the system's orientation sensor registered listeners
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);// to stop the listener and save battery
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]);// get the angle around the z-axis rotated
        degreeView.setText(String.valueOf(degree));
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);// create a rotation animation (reverse turn degree degrees)
        ra.setDuration(210);// how long the animation will take place
        ra.setFillAfter(true);// set the animation after the end of the reservation status
        image.startAnimation(ra);// Start the animation
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void processFinish(String result) {
        dataFromServer = result;
    }

    private class Thread1 extends Thread {
        @Override
        public void run() {
            super.run();
            while (!reachFlag) {
                if (!beaconInformation.equals(beaconBuffer)) {
                    beaconSeparation();
                    if (!dataFlag && rssiThreshold(sensorsRSSI[currentBeaconNumber], 100)) {
                        downloading();
                        if (dataFromServer.length() > 10) {
                            dataFlag = true;
                            path1 = databaseSeparation.seperation(dataFromServer, numberOfSensors);
                        }
                    }
                    if (!exceptionFlag && dataFlag) {
                        normalFunction();
                    } else if (exceptionFlag && dataFlag) {
                        timerFunction();
                    }
                    speaking();
                }
            }
        }
    }

    private void timerFunction() {
        if ((timesOfSignalsReceiving[currentBeaconNumber] - referenceTime) / 1000 < 3) {
            if (rssiThreshold(Integer.parseInt(separate[1]), 68)) {
                beaconWeight[currentBeaconNumber]++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.setText(String.valueOf((float) (timesOfSignalsReceiving[currentBeaconNumber]
                                - referenceTime) / 1000));
                    }
                });
            }
        } else {
            if (beaconWeight[next] > 0) {
                currentBeaconNumber = next;
                commonFunction();
                exceptionFlag = false;
            } else {
                int greatBuf = 0, pos1 = 0;
                for (int i = 0; i < numberOfSensors; i++) {
                    if (beaconWeight[i] > greatBuf) {
                        pos1 = i;
                        greatBuf = beaconWeight[i];
                    }
                }
                for (int i = 0; i < bufferRouCounter; i++) {
                    if (bufferRou[i] == pos1) {
                        T2S.speak("You are in the right track ", TextToSpeech.QUEUE_FLUSH, null);
                        speakPeriod();
                        rightPathFlag = true;
                    }
                }
                if (!rightPathFlag) {
                    T2S.speak("You get wrong path.", TextToSpeech.QUEUE_FLUSH, null);
                    speakPeriod();
                }
                currentBeaconNumber = pos1;
                commonFunction();
                exceptionFlag = false;
                firstTimeFlag = false;
            }
        }
    }

    private void normalFunction() {

        if (dataFlag && rssiThreshold(sensorsRSSI[currentBeaconNumber], 68)) {
            if (currentBeaconNumber != current) {
                if (!firstTimeFlag) {//If this is the first time
                    current = currentBeaconNumber;
                    commonFunction();
                    firstTimeFlag = true;
                    speakFlag = true;
                } else {//If it is not the first time
                    if (currentBeaconNumber == destination && next == destination) {
                        current = currentBeaconNumber;
                        commonFunction();
                        if (!reachFlag) {
                            reachFlag = true;
                            speakFlag = true;
                        }
                    } else if (currentBeaconNumber == next) {
                        fPath = routShortestPath.routing(currentBeaconNumber, destination,
                                sta.stringtoarrays(numberOfSensors, str), numberOfSensors);
                        next = rf.routfinding(currentBeaconNumber, fPath, numberOfSensors, -1);
                        speakFlag = true;
                        current = currentBeaconNumber;
                    } else {
                        String[] str9 = fPath.split(",");
                        bufferRouCounter = 0;
                        for (int i = 0; i < str9.length; i++) {
                            bufferRou[bufferRouCounter] = Integer.parseInt(str9[bufferRouCounter]);
                            bufferRouCounter++;
                        }
                        exceptionFlag = true;
                        int referenceBeaconRssiBuffer;
                        referenceTime = timesOfSignalsReceiving[currentBeaconNumber];
                        referenceBeaconRssiBuffer = sensorsRSSI[currentBeaconNumber];
                        sensorsRSSI = new int[numberOfSensors];
                        timesOfSignalsReceiving = new Long[numberOfSensors];
                        sensorsRSSI[currentBeaconNumber] = referenceBeaconRssiBuffer;
                        timesOfSignalsReceiving[currentBeaconNumber] = referenceTime;
                        for (int i = 0; i < numberOfSensors; i++) beaconWeight[i] = 0;
                        beaconBuffer = " ";
                        rightPathFlag = false;
                    }
                }
                if (!exceptionFlag) {
                    final String[] pathVariable = {String.valueOf(bufferRou[0])};
                    for (int i = 1; i < bufferRouCounter; i++) {
                        pathVariable[0] = "->" + String.valueOf(bufferRou[i]);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pathView.setText("Path: " + fPath);
                            nextLocationView.setText("Next Location: " +
                                    listOfSensorsName[next] + " (" + String.valueOf(next) + ")");
                            currentLocationView.setText("Current Location: " +
                                    listOfSensorsName[currentBeaconNumber] + " (" +
                                    String.valueOf(currentBeaconNumber) + ")");
                        }
                    });
                }
            }
        }
    }

    private void speaking() {
        if (speakFlag) {
            AudioGuidance AG = new AudioGuidance();
            toSpeak = AG.guidance(sta.stringtoarrays(numberOfSensors, str),
                    numberOfSensors, doors, reachFlag, destination,
                    currentBeaconNumber, next, degree,
                    listOfSensorsName[currentBeaconNumber],firstTimeFlag);
            separate = toSpeak.split("%p%");//This refers to which sensor it will reach
            T2S.speak(separate[0], TextToSpeech.QUEUE_FLUSH, null);
            speakPeriod();
            speakFlag = false;
        }
    }

    private void speakPeriod() {
        while (!T2S.isSpeaking()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (T2S.isSpeaking())
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    private boolean rssiThreshold(int rssi, int th) {
        if (rssi >= -th && rssi != 0) {
            return true;
        } else
            return false;
    }

    void connectivity() throws ExecutionException, InterruptedException {
        BackgroundTask asyncTask = new BackgroundTask(this);
        asyncTask.delegate = this;
        asyncTask.execute("login", "map", location).get();
    }

    private void downloading() {
        try {
            connectivity();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void commonFunction() {
        fPath = routShortestPath.routing(currentBeaconNumber, destination,
                path1, numberOfSensors);
        next = rf.routfinding(currentBeaconNumber, fPath, numberOfSensors, next);
        str = ats.arraystostring(path1, numberOfSensors);
        String[] separation = dataFromServer.split("&");//Separate unused part
        doors = separation[1];
    }

    private void beaconSeparation() {
        String[] separate = beaconInformation.split("&");//Separate unused part
        String[] separate1 = separate[0].split("/");
        beaconBuffer = beaconInformation;
        currentBeaconNumber = Integer.parseInt(separate1[0]);
        location = separate1[2];
//        if(!exceptionFlag)
        sensorsRSSI[currentBeaconNumber] = Integer.parseInt(separate[1]);
        timesOfSignalsReceiving[currentBeaconNumber] = Long.parseLong(separate[2]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.add(String.format(listOfSensorsName[currentBeaconNumber] + ": " + sensorsRSSI[currentBeaconNumber]));
                listAdapter.notifyDataSetChanged();
            }
        });
    }
}
