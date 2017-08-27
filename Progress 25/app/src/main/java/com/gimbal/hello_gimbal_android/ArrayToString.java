package com.gimbal.hello_gimbal_android;


public class ArrayToString {
    public String [] arraystostring(double [][][] path,int locations){
        String string="",string1="",string2="";
        String [] strings = new String[3];
        for(int i=0;i<locations;i++){
            for(int j=0;j<locations;j++){
                string+=String.valueOf((int)path[i][j][0])+",";
                string1+=String.valueOf((int)path[i][j][1])+",";
                string2+=String.valueOf((int)path[i][j][2])+",";
            }
        }
        strings[0]=string;
        strings[1]=string1;
        strings[2]=string2;
        return strings;
    }
}
