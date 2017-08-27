package com.gimbal.hello_gimbal_android;

public class Routing {
    public static String routing (int source, int destination,double [][][] path,int sensors){
        String [] smallestRoute=new String[sensors];
        double inf = Double.POSITIVE_INFINITY;
        double [] smallest=new double[sensors];
        double overallPath=0,buffer=inf;

        for(int j=0;j<sensors;j++){smallest[j]=inf;}

        boolean reached=false,flag=false;

        int current;
        int [] visited=new int[sensors];
        int [] frontier=new int[sensors];
        int buf=0;
        int frontierCounter=0;
        int visitedCounter=0,infInt=10000;
        for(int j=0;j<sensors;j++){frontier[j]=infInt;visited[j]=infInt;}
        for(int j=0;j<sensors;j++){smallestRoute[j]="";}
        current=source;
        visited[visitedCounter]=current;
        smallest[current]=0;
        smallestRoute[current]=Integer.toString(source)+',';

        visitedCounter++;


        if(path[source][destination][1]>0){

            smallest[destination]=path[current][destination][0];
            smallestRoute[current]=smallestRoute[current]+Integer.toString(destination)+',';
            frontier[0]=destination;
            visited[0]=destination;
            smallestRoute[destination]=Integer.toString(source)+','+Integer.toString(destination)+',';
        }else{
            while(!reached){
                buffer=inf;
                overallPath=smallest[visited[(visitedCounter-1)]];
                for(int i=0;i<sensors;i++){
                    flag=false;
                    if(path[current][i][1]>0){
                        for(int k=0;k<sensors;k++){//This for loop is to see if visiting happened
                            if((visited[k]==i)){
                                flag=true;
                            }
                        }
                        if(!flag){
                            for(int k=0;k<sensors;k++){//This for loop is to see if the node has been frontier
                                if(frontier[k]==i){
                                    if((overallPath+path[current][i][0]<smallest[frontier[k]])){
                                        smallest[frontier[k]]=overallPath+path[current][i][0];
                                        smallestRoute[frontier[k]]=smallestRoute[current]+Integer.toString(i)+',';
                                    }
                                    flag=true;
                                }
                            }
                        }
                        if(!flag){
                            if((overallPath+path[current][i][0]<smallest[i])){
                                smallest[i]=overallPath+path[current][i][0];
                                smallestRoute[i]=smallestRoute[current]+Integer.toString(i)+',';
                            }
                            frontier[frontierCounter]=i;
                            frontierCounter++;
                        }
                    }
                }
                frontierCounter--;
                if(frontierCounter==0){
                    visited[visitedCounter]=frontier[0];
                    current=visited[visitedCounter];
                    visitedCounter++;
                    frontier[0]=infInt;
                }else{
                    for(int i=0;i<sensors;i++){
                        if(frontier[i]<sensors){
                            if((smallest[frontier[i]]<buffer)){
                                visited[visitedCounter]=frontier[i];
                                buffer=smallest[frontier[i]];
                                buf=i;
                            }
                        }
                    }
                    current=visited[visitedCounter];
                    visitedCounter++;
                    for(int i=buf;i<sensors;i++){
                        if((i+1)<sensors)
                            frontier[i]=frontier[i+1];
                        else
                            frontier[i]=infInt;
                    }
                }
                if((smallest[destination]<inf)&&frontierCounter==0)
                    reached=true;
            }
        }
        return smallestRoute[destination];
    }
}
