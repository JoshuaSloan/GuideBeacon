package com.gimbal.hello_gimbal_android;


public class StringToArray {
    public double [][][] stringtoarrays(int locations,String [] str){
        double[][][] path = new double[locations][locations][2];
        String[] separat = str[0].split(",");//Separate unused part
        int k=0;

        for(int i=0;i<locations;i++){
            for(int j=0;j<locations;j++){
                path[i][j][0]=Double.parseDouble(separat[k]);
                k++;
            }
        }

        String[] separat1 = str[1].split(",");//Separate unused part
        k=0;

        for(int i=0;i<locations;i++){
            for(int j=0;j<locations;j++){
                path[i][j][1]=Double.parseDouble(separat1[k]);
                k++;
            }
        }
        return path;
    }
}