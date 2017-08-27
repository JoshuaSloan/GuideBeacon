package com.gimbal.hello_gimbal_android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
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


public class SearchActivity extends AppCompatActivity implements AsyncResponse, SettingsFragment.OnFragmentInteractionListener {

    private PlaceManager placeManager;
    TextToSpeech tts;
    BluetoothAdapter bluetoothAdapter;
    String userCommand = null, message = null, dataFromServer, buffer;
    String[] options, s1;
    int resultCounter = 0,counter=0;
    boolean dataFlag = false,noFlag = true;
    TextView txtView;
    ProgressBar spinner;
    boolean settingsCatch;                  //voice settings error detection
    boolean includePathPreview = true;      //used to determine whether or not a path preview is provided
    boolean turnByTurnNavigation = true;    //used to determine whether or not turn by turn navigation is provided
    boolean avoidStaircases = false;        //used to determine whether or not alternate paths are provided

    String filename = "GuideBeaconSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        txtView = (TextView)findViewById(R.id.txtView);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);

        spinner.setVisibility(View.GONE);
        //sets the settings based of the current internal file information (OLD METHOD)
        /*
        if (fileExistance(filename)) {
            String checkSettings = read_file(this, filename);

            if (checkSettings.startsWith("1")){
                includePathPreview = true;
            }
            else
            {
                includePathPreview = false;
            }

            if (checkSettings.substring(1).startsWith("1")){
                turnByTurnNavigation  = true;
            }
            else
            {
                turnByTurnNavigation = false;
            }

            if (checkSettings.substring(2).startsWith("1")){
                avoidStaircases = true;
            }
            else
            {
                avoidStaircases = false;
            }
        }
        */

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    //Locale loc = new Locale ("es", "ES"); //something like this will make it Spanish for future reference
                    //tts.setLanguage(loc);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //drop down items in the action bar, only settings currently, but more can easily be added (ReadMe?)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .addToBackStack("settings")
                    .commit();
            //frame_container is the id of the container for the fragment
            /*
            //maybe there is a better way but for now this works for testing
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Settings");
            // These are grouped separately to easily format the settings window, adding another is now very easy and concise
            LinearLayout settingsLayout = new LinearLayout(this);
            settingsLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout pathPreviewLayout = new LinearLayout(this);
            pathPreviewLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout turnByTurnLayout = new LinearLayout(this);
            turnByTurnLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout avoidStairsLayout = new LinearLayout(this);
            avoidStairsLayout.setOrientation(LinearLayout.HORIZONTAL);

            final boolean tempPathPreview, tempTurnByTurn, tempAvoidStairs;
            final Switch pathPreview = new Switch(this);
            final Switch turnByTurn = new Switch(this);
            final Switch avoidStairs = new Switch(this);
            final TextView txtPathPreview = new TextView(this);
            final TextView txtTurnByTurn = new TextView(this);
            final TextView txtAvoidStairs = new TextView(this);
            //Do we want to include multi-language support?
            //Also will need new TextView labels and a lot of translations for sighted users
            /*
            final Spinner languageSelect = new Spinner(this);

            languageSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getApplicationContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //nothing
                }
            });
            */
            /*
            txtPathPreview.setText("Path Preview");
            txtTurnByTurn.setText("Turn by turn navigation");
            txtAvoidStairs.setText("Avoid paths using stairs");

            if (includePathPreview)
            {
                tempPathPreview = true;
                pathPreview.setChecked(true);
            }
            else
            {
                tempPathPreview = false;
                pathPreview.setChecked(false);
            }

            if (turnByTurnNavigation)
            {
                tempTurnByTurn = true;
                turnByTurn.setChecked(true);
            }
            else
            {
                tempTurnByTurn = false;
                turnByTurn.setChecked(false);
            }

            if (avoidStaircases)
            {
                tempAvoidStairs = true;
                avoidStairs.setChecked(true);
            }
            else
            {
                tempAvoidStairs = false;
                avoidStairs.setChecked(false);
            }

            //grouping of path preview prompt and switch
            pathPreviewLayout.addView(txtPathPreview);
            pathPreviewLayout.addView(pathPreview);

            //grouping of turn by turn directionality prompt and switch
            turnByTurnLayout.addView(txtTurnByTurn);
            turnByTurnLayout.addView(turnByTurn);

            //grouping of alternate paths avoiding staircases prompt and switch
            avoidStairsLayout.addView(txtAvoidStairs);
            avoidStairsLayout.addView(avoidStairs);

            //throw all of them into the settings layout
            settingsLayout.addView(pathPreviewLayout);
            settingsLayout.addView(turnByTurnLayout);
            settingsLayout.addView(avoidStairsLayout);
            //settingsLayout.addView(languageSelect); //uncomment when spinner is created for Language Selection
            builder.setView(settingsLayout);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //check values and save setting (clean this up)
                    String settings;
                    String clear = "";
                    if (pathPreview.isChecked())
                    {
                        includePathPreview = true;
                        settings = "1";
                    }
                    else
                    {
                        includePathPreview = false;
                        settings = "0";
                    }

                    if (turnByTurn.isChecked())
                    {
                        turnByTurnNavigation = true;
                        settings = settings + "1";
                    }
                    else
                    {
                        turnByTurnNavigation = false;
                        settings = settings + "0";
                    }

                    if (avoidStairs.isChecked())
                    {
                        avoidStaircases = true;
                        settings = settings + "1";
                    }
                    else
                    {
                        avoidStaircases = false;
                        settings = settings + "0";
                    }

                    FileOutputStream outputStream;
                    if (settings.startsWith("00")) //need a better check and additional responsiveness! Perhaps as they are moving them (this would require listeners for the switches).
                    {
                        Toast.makeText(getApplicationContext(), "Cannot disable both Path Preview and Turn-by-Turn!", Toast.LENGTH_LONG).show();
                        includePathPreview = tempPathPreview;
                        turnByTurnNavigation = tempTurnByTurn;
                        avoidStaircases = tempAvoidStairs;
                    }
                    else {
                        if(fileExistance(filename)) {
                            deleteFile(filename);
                            try {
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                outputStream.write(settings.getBytes());
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                    else {
                            try {
                                new File(getApplicationContext().getFilesDir(), filename);
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                outputStream.write(settings.getBytes());
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
                    }
                }
            });

            builder.show();
            */
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                userCommand = null; message = null; dataFromServer = null; buffer = null;
                resultCounter = 0; counter=0;
                dataFlag = false; noFlag = true;
                speaking("Say your destination please.", 100);
                return true;
            }
        };
        final GestureDetector detector = new GestureDetector(this, listener);
        detector.setOnDoubleTapListener(listener);
        detector.setIsLongpressEnabled(true);
        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
    }

    public void gimbal(){

        Gimbal.setApiKey(this.getApplication(), "1fc415f9-d2ca-49c1-96a6-65ce3dab0dfc");

        PlaceEventListener placeEventListener = new PlaceEventListener() {

            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
                message = beaconSighting.getBeacon().getName();
                Log.d("emad",message);
                if (message != null) {
                    String[] separated = message.split("/");
                    Log.d("emad",separated[2]);
                    buffer = separated[2];
                    try {
                        connectivity();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    spinner.setVisibility(View.GONE);
                    if (dataFromServer != null && dataFromServer.length() > 2 && !dataFlag) {
                        Log.d("emad",dataFromServer);
                        String[] strbuf, strbuf1;
                        dataFlag = true;
                        strbuf = dataFromServer.split("/");
                        Log.d("emad",strbuf[1]);
                        options = new String[strbuf.length];
                        s1 = new String[strbuf.length];
                        strbuf1 = strbuf[strbuf.length - 1].split("list");
                        strbuf[strbuf.length - 1] = strbuf1[0];
                        for (String aStrbuf : strbuf) {
                            strbuf1 = aStrbuf.split(":");
                            options[Integer.parseInt(strbuf1[1].trim())] = strbuf1[0];
                        }

                        Log.d("emad",options[0]);
                        for (int i = 0; i < options.length; i++) {
                            if (options[i].toLowerCase().contains(userCommand)) {
                                s1[resultCounter] = String.valueOf(i);
                                resultCounter++;
                            }
                        }

                        Log.d("emad",Integer.toString(resultCounter));
                        if (resultCounter == 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tts.speak("Nothing is found! Please try again.", TextToSpeech.QUEUE_FLUSH, null, "TTS");
                                noFlag = false;
                            }
                        } else {
                            if (resultCounter > 1)
                                speaking(resultCounter + "locations are found. " + "Are you Looking for " + options[Integer.parseInt(s1[counter])] + "?", 300);
                            else
                                speaking(resultCounter + "location is found. " + "Are you Looking for " + options[Integer.parseInt(s1[counter])] + "?", 300);

                            counter++;
                        }
                    }
                }
            }
        };

        if(!dataFlag) {
            placeManager = PlaceManager.getInstance();
            placeManager.addListener(placeEventListener);
            placeManager.startMonitoring();
            CommunicationManager.getInstance().startReceivingCommunications();
        }else
            placeManager.stopMonitoring();
    }

    private void speaking(final String toSpeak, final int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "TTS");
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(String s) {
            }

            @Override
            public void onDone(String s) {
                if (noFlag) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    try {
                        startActivityForResult(intent, requestCode);
                    } catch (ActivityNotFoundException ignored) {
                    }
                }
            }

            @Override
            public void onError(String s) {
            }
        });
    }

    //***************************************************************************************************************
    //This is what I need to fix to get it to stop crashing on speech to text
    //Basically it does not know what to do for a "case x00" scenario
    //Work backwards and perhaps try to return a value in speaking() such that you do not have to recursively call it
    //***************************************************************************************************************
    public void onActivityResult(int request_code, int result_code, Intent intent) {

        switch (request_code) {
            case 100:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    userCommand = result.get(0);
                    txtView.setText(userCommand);
                    if (userCommand.equals("user settings")) {
                        speaking("Would you like to change the current settings", 400);
                    } else {
                        speaking("Are you looking for " + userCommand + "? Please say yes or no.", 200);
                    }
                }
                break;
            case 200:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        bluetoothAdapter.enable();
                        Log.d("emad", "Yes");
                        spinner.setVisibility(View.VISIBLE);
                        gimbal();
                    } else {
                        speaking("Please say your destination again.", 100);
                    }
                }
                break;
            case 300:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        transfer();
                    } else {
                        if (resultCounter == 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tts.speak("Nothing is found! Please try again.", TextToSpeech.QUEUE_FLUSH, null, "TTS");
                                noFlag = false;
                            }
                        } else if (resultCounter > 1 && counter < resultCounter) {
                            speaking("Are you Looking for " + options[Integer.parseInt(s1[counter])] + "?", 300);
                            counter++;
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                tts.speak("Nothing is found! Please try again.", TextToSpeech.QUEUE_FLUSH, null, "TTS");
                                noFlag = false;
                            }
                        }
                    }
                }
                break;
            case 400:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // need separate request codes for all settings or need to implement a loop somehow... wait on this

                            if (includePathPreview) {
                                speaking("Path preview is currently enabled, would you like to disable it?.", 500);
                            } else {
                                speaking("Path preview is currently disabled, would you like to enable it?.", 500);
                            }
                        }
                    }
                } else {
                    speaking("Returning to main menu. Please say your destination.", 100);
                }
                break;
            case 500:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        includePathPreview = !includePathPreview;
                        if (!includePathPreview) {
                            settingsCatch = true;
                        }
                        if (turnByTurnNavigation) {
                            speaking("Turn by turn navigation is currently enabled, would you like to disable it?.", 510);
                        } else {
                            speaking("Turn by turn navigation is currently disabled, would you like to enable it?.", 510);
                        }
                    } else if (result.get(0).equals("no")) {
                        if (turnByTurnNavigation) {
                            speaking("Turn by turn navigation is currently enabled, would you like to disable it?.", 510);
                        } else {
                            speaking("Turn by turn navigation is currently disabled, would you like to enable it?.", 510);
                        }

                    } else {
                        speaking("Returning to main menu. Please say your destination.", 100);
                    }
                }
                break;
            case 510:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        turnByTurnNavigation = !turnByTurnNavigation;
                        if (!turnByTurnNavigation && settingsCatch) {
                            turnByTurnNavigation = true;
                            speaking("Cannot disable both Path Preview and Turn by Turn Navigation, setting reverted," +
                                    " returning to main menu, please say your destination.",520); // this is not catching, although the variable is reverted?... is this a problem?
                        }
                        if (avoidStaircases) {
                            speaking("Avoid staircases is currently enabled, would you like to disable it?.", 520);
                        } else {
                            speaking("Avoid staircases is currently disabled, would you like to enable it?.", 520);
                        }
                    } else if (result.get(0).equals("no")) {
                        if (avoidStaircases) {
                            speaking("Avoid paths with staircases is currently enabled, would you like to disable it?.", 520);
                        } else {
                            speaking("Avoid paths with staircases is currently disabled, would you like to enable it?.", 520);
                        }

                    } else {
                        speaking("Returning to main menu. Please say your destination.", 100);
                    }
                }
                break;
            case 520:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).equals("yes")) {
                        avoidStaircases = !avoidStaircases;
                        saveSettings(); //needs to do some sort of fail safe check for double disabled TbT and PP
                        speaking("Settings saved, returning to main menu. Please say your destination.", 100);
                    }
                    else {
                        saveSettings(); //needs to do some sort of fail safe check for double disabled TbT and PP
                        speaking("Settings saved, returning to main menu. Please say your destination.", 100);
                    }
                    break;

                    //will this alone fix the crashing problem elegantly? additionally does not solve recursion problem
           /*     //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            default:
                if (intent != null)
                    //something needs to be done here so that there is a fail safe to get you back to where you need to go
                    //this may include, sorry I didn't catch that, and then return to a case value of 100 for instance

                    //this may not even fix the problem, it will require testing
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak("I'm sorry something went wrong, please try again.", TextToSpeech.QUEUE_FLUSH, null, "TTS");

                        //copied section from earlier ***
                        userCommand = null; message = null; dataFromServer = null; buffer = null;
                        resultCounter = 0; counter=0;
                        dataFlag = false; noFlag = true;
                        //copied section from earlier ***

                        speaking("Say your destination please.", 100); //NOTE: THIS DOES NOT FIX THE RECURSION PROBLEM!!!
                    }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                */
                }
        }
    }
    public void transfer() {
        spinner.setVisibility(View.VISIBLE);
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.putExtra("key",  options[Integer.parseInt(s1[counter-1])] + " : "+ s1[counter-1] +"////" + dataFromServer); //Optional parameters
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(myIntent);
        finish();
    }

    void connectivity() throws ExecutionException, InterruptedException {
        BackgroundTask asyncTask = new BackgroundTask(this);
        asyncTask.delegate = this;
        asyncTask.execute("login", buffer, "list").get();
    }

    @Override
    public void processFinish(String result) {
        dataFromServer = result;
    }

    //checks internal storage associated with the app to see if the file with the filename string exists
    public boolean fileExistance(String filename){
        File file = getBaseContext().getFileStreamPath(filename);
        return file.exists();
    }

    //reads the content of a file after formatting it into UTF-8 format
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

    //need to consolidate the save from options menu and this into one executable function that does not conflict with variable integrity in the options menu
    //(i.e. allow this to work without extending the scope of the option menu variables)
    public void saveSettings()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (includePathPreview) {

            editor.putBoolean("pref_key_path_preview", true);
            editor.commit();
        }
        else {
            editor.putBoolean("pref_key_path_preview", false);
            editor.commit();
        }

        if (turnByTurnNavigation) {
            editor.putBoolean("pref_key_turn_by_turn", true);
            editor.commit();
        }
        else {
            editor.putBoolean("pref_key_turn_by_turn", false);
            editor.commit();
        }

        if (avoidStaircases) {
            editor.putBoolean("pref_key_avoid_stairs", true);
            editor.commit();
        }
        else {
            editor.putBoolean("pref_key_avoid_stairs", false);
            editor.commit();
        }
        //old check for valid settings configuration, keeping for reference in case I use internal file storage later for other information
        /*
        FileOutputStream outputStream;
        if (settings.startsWith("00")) //need a better check! and response, perhaps as they are moving them. (this would require listeners for the switches)
        {
            Toast.makeText(getApplicationContext(), "Cannot disable both Path Preview and Turn-by-Turn!", Toast.LENGTH_LONG).show();
        }
        else {
            if(fileExistance(filename)) {
                deleteFile(filename);
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(settings.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    new File(getApplicationContext().getFilesDir(), filename);
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(settings.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        */
            Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
    }

    //checks if the settings fragment is open or not. If so will return to search activity and save settings, if not will close the app. (Clean explanation)
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            //I don't remember why the last parameters are the way that they are, go back and explain.
            includePathPreview = sharedPref.getBoolean("pref_key_path_preview", true);
            turnByTurnNavigation = sharedPref.getBoolean("pref_key_turn_by_turn", true);
            avoidStaircases = sharedPref.getBoolean("pref_key_avoid_stairs", false);

            Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
        }
        else super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

