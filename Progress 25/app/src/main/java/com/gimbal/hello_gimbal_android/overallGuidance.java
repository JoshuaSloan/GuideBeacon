package com.gimbal.hello_gimbal_android;


public class overallGuidance {

    public String directionGuide(double[][][] path, int beaconNumber, int next, float degree) {

        int options;
        String direction="null";
        String pD = "";

        if (String.valueOf(path[beaconNumber][next][1]).length() > 3) {
            options = 0;
        } else {
            options = Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1])
                    .charAt(0)));
        }
        switch (options) {
            case 1:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North

                    pD = "Go straight.";

                } else if ((degree > 30 && degree <= 150)) {//.......................................East

                    pD = "Turn left and then go straight.";

                } else if ((degree > 150 && degree < 210)) {//........................................South

                    pD = "Turn around and then go straight.";

                } else if ((degree > 210 && degree < 330)) {//........................................South West

                    pD = "Turn right and then go straight.";

                }
                direction = "15";
                break;
            case 2:
                if ((degree >= 0 && degree <= 30) || (degree >= 330 && degree <= 360)) {//..........North

                    pD = "Turn around and then go straight";

                } else if ((degree > 30 && degree < 150)) {//..........................................North East

                    pD = "Turn right and then go straight.";


                } else if ((degree > 150 && degree < 210)) {//........................................South

                    pD = "Go straight.";

                } else if ((degree > 210 && degree < 330)) {//........................................South West

                    pD = "Turn left and then go straight.";

                }
                direction = "175";
                break;
            case 3:
                if ((degree >= 0 && degree <= 60) || (degree > 300 && degree <= 360)) {//..........North

                    pD = "Turn Right and then go straight";

                } else if ((degree >= 60 && degree <= 120)) {//.......................................East

                    pD = "Go straight";

                } else if ((degree > 120 && degree <= 240)) {//.......................................South East

                    pD = "Turn left and then go straight";

                } else if ((degree >= 240 && degree <= 300)) {//......................................West

                    pD = "Turn around and then go straight.";

                }
                direction = "90";
                break;
            case 4:
                if ((degree >= 0 && degree < 60) || (degree > 300 && degree <= 360)) {//..........North

                    pD = "Turn left and then go straight.";

                } else if ((degree >= 60 && degree <= 120)) {//.......................................East

                    pD = "Turn around and then go straight.";

                } else if ((degree > 120 && degree < 240)) {//.......................................South East

                    pD = "Turn Right and then go straight.";

                } else if ((degree >= 240 && degree <= 300)) {//......................................West

                    pD = "Go straight.";
                }
                direction = "275";
                break;
            case 0:
                if(Integer.parseInt(String.valueOf(String.valueOf(path[beaconNumber][next][1]).charAt(1)))==1){
                    pD = " use the stairs to reach ground floor. ";
                    direction = "100";
                }else{
                    pD = " use the stairs to reach floor one ";
                    direction = "175";
                }
                break;

        }


        return pD+"//"+direction;
    }
}
