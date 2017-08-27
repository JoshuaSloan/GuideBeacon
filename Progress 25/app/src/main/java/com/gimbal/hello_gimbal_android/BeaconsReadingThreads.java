package com.gimbal.hello_gimbal_android;


import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gimbal.android.BeaconSighting;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;

import java.util.List;

public class BeaconsReadingThreads implements Runnable{


    private String BeaconsName;
    private String TAG = "Emad Service";
    Context mContext;
    private String message;
    private Handler mHandler;

    BeaconsReadingThreads(Context mContext, Handler mHandler){
        this.mContext = mContext;
        this.mHandler = mHandler;
    }


    public void run(){
        Gimbal.setApiKey((Application) mContext.getApplicationContext(), "1fc415f9-d2ca-49c1-96a6-65ce3dab0dfc" );
        PlaceEventListener placeEventListener = new PlaceEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
                super.onBeaconSighting(beaconSighting, list);
                message = beaconSighting.getBeacon().getName();
                Message msg = Message.obtain();
                msg.obj = message;
                msg.setTarget(mHandler);
                msg.sendToTarget();
            }
        };

        PlaceManager placeManager = PlaceManager.getInstance();
        placeManager.addListener(placeEventListener);
        placeManager.startMonitoring();

        CommunicationManager.getInstance().startReceivingCommunications();
    }
}
