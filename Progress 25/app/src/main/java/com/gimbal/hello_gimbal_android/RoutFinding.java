package com.gimbal.hello_gimbal_android;

public class RoutFinding {
    public int routfinding(int beaconNumber,String fPath,int sensors,int next){
        int routingCounter=0;
        int[] bufferRou = new int[sensors];
        routingCounter=0;
        String[] nodes;
        if ((beaconNumber > -1)) {
            if(fPath.length()!=0) {
                nodes = fPath.split(",");//separate sensors from each other
                for (int i = 0; i < nodes.length; i++) {
                    bufferRou[i] = Integer.parseInt(nodes[i]);
                    routingCounter++;
                }
                routingCounter = routingCounter - 1;
            }
            for (int i=0;i<(routingCounter);i++){
                bufferRou[i]=bufferRou[i+1];
            }
            next=bufferRou[0];
        }
        return next;
    }
}
