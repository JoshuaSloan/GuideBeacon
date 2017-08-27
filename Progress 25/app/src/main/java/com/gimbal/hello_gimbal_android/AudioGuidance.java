package com.gimbal.hello_gimbal_android;


import android.util.Log;

public class AudioGuidance {


    public String guidance(double[][][] path,int locations,String doors,boolean reachFlag,int destination,int beaconNumber
            ,int next,float degree,String beaconName,boolean firstTimeFlag) {

        int options;
        String pD = "";
        int [] door=new int[locations];
        String[] doorsDir1;
        String[] doorsDir = doors.split("z");//separate sensors from each other


        for(int i=0;(i<doorsDir.length)&&(i!=locations);i++){
            doorsDir1 = doorsDir[i].split("d");//This will separate and
            door[Integer.parseInt(doorsDir1[0])]=Integer.parseInt(doorsDir1[1]);
        }

        if (reachFlag) {
            options=door[destination];
        }else{
            if (String.valueOf(path[beaconNumber][next][1]).length() > 3) {
                options = 0;
            }else{
                options = Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1])
                        .charAt(0)));
            }
        }
        switch (options) {
            case 1:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in front of you.";
                    else
                        pD = "Go straight.";

                }else if ((degree > 30 && degree < 60)) {//..........................................North East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn a little left and then go straight.";

                }else if ((degree >= 60 && degree <= 120)) {//.......................................East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight.";

                }else if ((degree > 120 && degree <= 150)) {//.......................................South East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight.";

                }else if ((degree > 150 && degree < 210)) {//........................................South
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere behind you.";
                    else
                        pD = "Turn around and then go straight.";

                }else if ((degree > 210 && degree < 240)) {//........................................South West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn right and then go straight.";

                }else if ((degree >= 240 && degree <= 300)) {//......................................West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn right and then go straight.";

                }else if ((degree > 300 && degree < 330)) {//........................................North West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn a little right and then go straight.";

                }
                break;
            case 2:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere behind you.";
                    else
                        pD = "Turn around and then go straight";

                }else if ((degree > 30 && degree < 60)) {//..........................................North East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn right and then go straight.";

                }else if ((degree >= 60 && degree <= 120)) {//.......................................East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn Right and then go straight.";

                }else if ((degree > 120 && degree <= 150)) {//.......................................South East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn a little right and then go straight.";

                }else if ((degree > 150 && degree < 210)) {//........................................South
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in front of you.";
                    else
                        pD = "Go straight.";

                }else if ((degree > 210 && degree < 240)) {//........................................South West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn a little left and then go straight.";

                }else if ((degree >= 240 && degree <= 300)) {//......................................West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight.";

                }else if ((degree > 300 && degree < 330)) {//........................................North West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight";

                }
                break;
            case 3:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn Right and then go straight";

                }else if ((degree > 30 && degree < 60)) {//..........................................North East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn a little Right and then go straight";

                }else if ((degree >= 60 && degree <= 120)) {//.......................................East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in front of you.";
                    else
                        pD = "Go straight";

                }else if ((degree > 120 && degree <= 150)) {//.......................................South East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn a little left and then go straight";

                }else if ((degree > 150 && degree < 210)) {//........................................South
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight";

                }else if ((degree > 210 && degree < 240)) {//........................................South West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight";

                }else if ((degree >= 240 && degree <= 300)) {//......................................West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere behind you.";
                    else
                        pD = "Turn around and then go straight.";

                }else if ((degree > 300 && degree < 330)) {//........................................North West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn Right and then go straight.";

                }
                break;
            case 4:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight.";

                }else if ((degree > 30 && degree < 60)) {//..........................................North East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your left.";
                    else
                        pD = "Turn left and then go straight.";

                }else if ((degree >= 60 && degree <= 120)) {//.......................................East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere behind you.";
                    else
                        pD = "Turn around and then go straight.";

                }else if ((degree > 120 && degree <= 150)) {//.......................................South East
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn Right and then go straight.";

                }else if ((degree > 150 && degree < 210)) {//........................................South
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn Right and then go straight.";

                }else if ((degree > 210 && degree < 240)) {//........................................South West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in your right.";
                    else
                        pD = "Turn a little right and then go straight.";

                }else if ((degree >= 240 && degree <= 300)) {//......................................West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in front of you.";
                    else
                        pD = "Go straight.";

                }else if ((degree > 300 && degree < 330)) {//........................................North West
                    if (reachFlag)
                        pD = "You are in destination proximity. Destination should be somewhere in front of you.";
                    else
                        pD = "Turn a little left and then go straight.";

                }
                break;
            case 0:
//                switch (Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1]).charAt(0)))){
//                    case 1:
//                }
                pD = " use the stairs to go from floor " +
                        Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1]).charAt(0)))  + " to floor " +
                        Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1]).charAt(1)));
                Log.d("emad",pD);

                break;

        }

        if (!reachFlag) {
            if(firstTimeFlag)
                return  "You are approaching " + beaconName + ". You should" + pD + "%p%" + options;
            else
                return  "You are at " + beaconName + ", so you should" + pD + "%p%" + options;
        } else {
            return  pD + "%p%" + options;
        }
    }
}
