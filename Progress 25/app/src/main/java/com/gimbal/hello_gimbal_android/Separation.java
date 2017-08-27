package com.gimbal.hello_gimbal_android;



public class Separation {

    public String [] listSeparation(String dataList,String symbol){
        String [] list=dataList.split(symbol);
        return list;
    }


    public static double [][][] seperation(String result,int sensors){
        double [][][] path=new double[sensors][sensors][3];
        int [] doors=new int[sensors];


        String[] doorsDir1;
        String[] separat = result.split("&");//Separate unused part
        String[] doorsDir = separat[1].split("z");//separate sensors from each other
        String[] separated = separat[0].split("z");//separate sensors from each other

        for(int i=0;(i<doorsDir.length)&&(i!=sensors);i++){
            doorsDir1 = doorsDir[i].split("d");//This will separate and
            doors[Integer.parseInt(doorsDir1[0])]=Integer.parseInt(doorsDir1[1]);
        }

        for(int i=0;(i<separated.length)&&(i!=sensors);i++){
            String[] separated1 = separated[i].split("a");//This will separate and
            for(int j=0;j<separated1.length;j++){

                String[] separated2 = separated1[j].split("p");//This refers to Price
                int price= Integer.parseInt(separated2[0]);
                String[] separated3 = separated2[1].split("d");//This refers to direction
                int direction= Integer.parseInt(separated3[0]);
                String[] separated4 = separated3[1].split("r");//This refers to which sensor it will reach
                int firstReach= Integer.parseInt(separated4[0]);
                String[] separated5 = separated4[1].split("s");
                int secondReach= Integer.parseInt(separated5[0]);
                int steps= Integer.parseInt(separated5[1]);

                path[price][secondReach][1]=firstReach;//This will show the direction between sensors
                path[price][secondReach][0]=direction;//This will save the weight between sensors

                path[price][secondReach][2]=steps;//This will save the weight between sensors


            }
        }

        return path;
    }
}

