package com.gimbal.hello_gimbal_android;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity  implements SensorEventListener, AsyncResponse{
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
    private SensorManager sensorManager;
    boolean activityRunning = true,originalStepFlag = true;
    float stepCounter = 0, originalStepCounter =0;
    Long originalTime,endTime;
    TextView stepCounterView;
    boolean pathPreview;            //used to determine whether or not a path preview is provided
    boolean turnByTurnNav;          //used to determine if turn by turn navigation is provided
    boolean avoidStaircases;        //used to determine if alternate path mapping avoiding stairs is provided
    String lastInstruction = null;  //stores the last instruction so the users can hear it again if needed

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gimbal.setApiKey(this.getApplication(), "1fc415f9-d2ca-49c1-96a6-65ce3dab0dfc");

        stepCounterView = (TextView) findViewById(R.id.stepCounter);
        image = (ImageView) findViewById(R.id.imageViewCompass);// Compass image
        degreeView = (TextView) findViewById(R.id.degree);// Degree TextView
        currentLocationView = (TextView) findViewById(R.id.currentLocation);
        nextLocationView = (TextView) findViewById(R.id.nextLocation);
        pathView = (TextView) findViewById(R.id.pathFromSourceToDestination);
        timer = (TextView) findViewById(R.id.timerView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this,countSensor,sensorManager.SENSOR_DELAY_UI);


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        T2S = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    T2S.setLanguage(Locale.US);
                }
            }
        });

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        originalTime = System.currentTimeMillis();

        String listOfLocations= getIntent().getStringExtra("key");
        //String listOfLocations = "research Lab : 7////research Lab : 0/Beggs Hall Entrance : 7" +
                //"/NetApp : 1/Biomaterial and Bioengineering Lab : 4/power quality lab : 3" +
                //"/Sustainable System Lab : 2/Quantum Computing lab : 5/Stair : 6/Stair : 8list ";

        str = listOfLocations.split("////");//In this function str is used as a buffer for saving data
        //for temporary
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
            Log.d(DEBUG + " List of sensors & pos", i + "->" + listOfSensorsName[i]); //ignore, not a big deal

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

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lastInstruction.equals(null)){
                    T2S.speak(lastInstruction,TextToSpeech.QUEUE_FLUSH, null); //update deprecated method
                }
            }
        });

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
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        mSensorManager.unregisterListener(this);// to stop the listener and save battery
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if(originalStepFlag) {
                originalStepCounter = event.values[0];
                originalStepFlag = false;
            }
            if(activityRunning) {
                stepCounter=(int) event.values[0];
                stepCounterView.setText(String.valueOf(stepCounter));
            }
        }else if (sensor.getType() == Sensor.TYPE_ORIENTATION) {

            degree = Math.round(event.values[0]);// get the angle around the z-axis rotated
            degreeView.setText(Float.toString(degree) + " degrees");
            RotateAnimation ra = new RotateAnimation(// create a rotation animation (reverse turn degree degrees)
                    currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

            ra.setDuration(210);// how long the animation will take place
            ra.setFillAfter(true);// set the animation after the end of the reservation status
            image.startAnimation(ra);// Start the animation
            currentDegree = -degree;
//            if(Boo)
//                BooFlag=BG.booGuidance(degree, Integer.parseInt(separate[1]),BooFlag);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void processFinish(String result) {dataFromServer = result;
    }

    private class Thread1 extends Thread {
        @Override
        public void run() {
            super.run();
            setSettingsValues(); //this is where you get your info, maybe have to move this if it crashes
            while (!reachFlag) {
                if (!exceptionFlag) {
                    normalFunction();
                }
                else if (exceptionFlag) {
                    timerFunction();
                }
                speaking();

            }
            if(reachFlag && speakFlag){
                speaking();
                speakFlag = false;
            }
        }
    }

    private void timerFunction() {
        if (!beaconInformation.equals(beaconBuffer)) {
            beaconSeparation();
            if(currentBeaconNumber == next && (rssiThreshold(Integer.parseInt(separate[1]), 70))){
                fPath = routShortestPath.routing(currentBeaconNumber, destination,
                        sta.stringtoarrays(numberOfSensors, str), numberOfSensors);
                next = rf.routfinding(currentBeaconNumber, fPath, numberOfSensors, -1);
                speakFlag = true;
                current = currentBeaconNumber;
                exceptionFlag = false;
                firstTimeFlag = false;
            }else if ((timesOfSignalsReceiving[currentBeaconNumber]
                    - referenceTime) / 1000 < 3) {
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
                            T2S.speak("You are in the right track ", TextToSpeech.QUEUE_FLUSH, null); //need to update deprecated function
                            speakPeriod();
                            rightPathFlag = true;
                        }
                    }
                    if (!rightPathFlag) {
                        T2S.speak("Recalculating ", TextToSpeech.QUEUE_FLUSH, null); //need to update deprecated function
                        speakPeriod();
                    }
                    currentBeaconNumber = pos1;
                    commonFunction();
                    exceptionFlag = false;
                    firstTimeFlag = false;
                }
            }
        }
    }

    private void normalFunction() {

        if (!beaconInformation.equals(beaconBuffer)) {
            beaconSeparation();
            if (!dataFlag && rssiThreshold(sensorsRSSI[currentBeaconNumber], 100)) {
                downloading();
                if (dataFromServer.length() > 10) {
                    dataFlag = true;
                    path1 = databaseSeparation.seperation(dataFromServer, numberOfSensors);
                }
            } else if (dataFlag && rssiThreshold(sensorsRSSI[currentBeaconNumber], 67)) {
                if (currentBeaconNumber != current) {
                    if ((currentBeaconNumber == destination && next == destination) ||
                            (currentBeaconNumber == destination && next == -1)) {
                        current = currentBeaconNumber;
                        commonFunction();
                        if (!reachFlag) {
                            reachFlag = true;
                            speakFlag = true;
                        }
                        String jali = "Current step counter = "+stepCounter+" Original step counter = "+
                                originalStepCounter+"----------------";
                        stepCounter = stepCounter - originalStepCounter;
                        jali += "Total step counter = "+stepCounter+"----------------";
                        endTime = System.currentTimeMillis();
                        jali += "End time = "+endTime+" Original time = "+originalTime+"----------------";
                        originalTime = endTime - originalTime;
                        jali += "Total time = "+originalTime;
                        String myData = "Path Preview: "+ pathPreview + ", " + stepCounter + " steps in " + (originalTime/1000) + " seconds";
                        writeExternalStorage(myData);
                    }
                    if(!reachFlag) {
                        if (!firstTimeFlag) {//If this is the first time
                            current = currentBeaconNumber;
                            commonFunction();
                            completePath();
                            speakFlag = true; // perhaps as simple as removing this speak flag to remove turn by turn?
                        } else {//If it is not the first time
                            if (currentBeaconNumber == next) {
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
                                            listOfSensorsName[currentBeaconNumber] + " (" + String.valueOf(currentBeaconNumber) + ")");
                                }
                            });
                        }
                    }
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
            firstTimeFlag = true;
            separate = toSpeak.split("%p%");//This refers to which sensor it will reach
            lastInstruction = separate[0]; //copied String instruction from last event
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

    private void beaconSeparation() { //In this function Sensor's name, ID, RSSI and
        String[] separate = beaconInformation.split("&");//In this line Beacons information can be
                                                        // extracted like Name, RSSI, Time

        String[] separate1 = separate[0].split("/");//In this line Beacons name is separated for some
                                                    // information like sensors ID, Name, Place

        beaconBuffer = beaconInformation;           //Information to separate new beacons signal
                                                    // from old one

        currentBeaconNumber = Integer.parseInt(separate1[0]);//Beacons ID

        location = separate1[2];                    //Beacons location

        sensorsRSSI[currentBeaconNumber] = Integer.parseInt(separate[1]);// Save sensors RSSI
        timesOfSignalsReceiving[currentBeaconNumber] = Long.parseLong(separate[2]);//Save sensors time
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.add(String.format(listOfSensorsName[currentBeaconNumber] + ": " + sensorsRSSI[currentBeaconNumber]));
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    private void completePath() {
        overallGuidance og = new overallGuidance();
        String[] sString = fPath.split(",");
        String[] stepByStep = new String[sString.length - 1];
        String[] splitting;
        stepByStep[0] = og.directionGuide(sta.stringtoarrays(numberOfSensors, str), Integer.parseInt(sString[0]),
                Integer.parseInt(sString[1]), degree);
        Log.d(DEBUG,stepByStep[0]);
        for (int i = 1; i < sString.length-1; i++) {
            splitting = stepByStep[i - 1].split("//");
            stepByStep[i] = og.directionGuide(sta.stringtoarrays(numberOfSensors, str), Integer.parseInt(sString[i]),
                    Integer.parseInt(sString[i + 1]), Float.parseFloat(splitting[1]));
            Log.d(DEBUG,stepByStep[i]);
        }

        String[] nextDirection = new String[stepByStep.length];
        for (int i = 0; i < stepByStep.length; i++) {
            splitting = stepByStep[i].split("//");
            nextDirection[i] = splitting[0];
            Log.d(DEBUG, String.valueOf(nextDirection[i]));
        }

        if (sString.length == 3) {
            if (pathPreview) { //check for whether or not to execute this part of the code
                T2S.speak("Base on current information, you are at " +
                        listOfSensorsName[Integer.parseInt(sString[0])] +
                        " ,so in the first step you should " + nextDirection[0] + " to reach " +
                        listOfSensorsName[Integer.parseInt(sString[1])] +
                        ". Then to get to your destination at " +
                        listOfSensorsName[Integer.parseInt(sString[2])] +
                        " you need to " + nextDirection[1], TextToSpeech.QUEUE_FLUSH, null);
                speakPeriod();
            }
            speakFlag = false;

        } else if (sString.length > 3) {
            if(pathPreview) { //same as above, however, does not set speak flag to false (will this matter?)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    T2S.speak("Base on current information, you are at " + listOfSensorsName[Integer.parseInt(sString[0])] +
                            " ,so in the first step you should " + nextDirection[0] + " to reach " +
                            listOfSensorsName[Integer.parseInt(sString[1])], TextToSpeech.QUEUE_FLUSH, null);
                    speakPeriod();
                    speakFlag = false;

                    for (int i = 1; i < sString.length - 1; i++) {
                        T2S.speak(" Then you should " + nextDirection[i] + " to reach " +
                                listOfSensorsName[Integer.parseInt(sString[i + 1])], TextToSpeech.QUEUE_FLUSH, null);
                        speakPeriod();

                        speakFlag = false;

                    }
                }
            }

        }
    }

    public void writeExternalStorage(String string){

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> messagelist = sms.divideMessage(string);
        String phoneNum = "+13076315646"; //replace X's with your personal phone number
        sms.sendMultipartTextMessage(phoneNum, null, messagelist, null, null);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Data sent", Toast.LENGTH_SHORT).show();
            }
        });

        //uncomment below if you want to write to external storage instead of text data results, I just found a text message to be easier (requires additional permissions however)
        /*
        String state;
        state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath()+"/MyAppFile");
            if(!Dir.exists());
            Dir.mkdir();
            File file = new File(Dir,"WSUMapping.txt");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(string.getBytes());
                fileOutputStream.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Message saved!", Toast.LENGTH_SHORT).show();
                   }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "SDK card not found!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        */
    }

    //perhaps all file related functions should be moved to separate file specific java code
    public String read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    //sets the settings for the main activity, interpreted from sharedPreferences
    public void setSettingsValues() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        pathPreview = preferences.getBoolean("pref_key_path_preview", true);
        turnByTurnNav = preferences.getBoolean("pref_key_turn_by_turn", true);
        avoidStaircases = preferences.getBoolean("pref_key_avoid_stairs", false);

        //add new variables to this when implementing language, walk speed, etc.
    }

        //From old internal file setting storage ~ keep for check if it is the user's first time with the app
        /*
        if (settingsCode.startsWith("1")){
            pathPreview = true;
        }
        else {
            pathPreview = false;
        }
        if (settingsCode.substring(1).startsWith("1")){
            turnByTurnNav  = true;
        }
        else {
            turnByTurnNav = false;
        }
        if (settingsCode.substring(2).startsWith("1")){
            avoidStaircases = true;
        }
        else {
            avoidStaircases = false;
        }
        */

}
