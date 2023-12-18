package com.nexgo.apiv3demo;

import cn.nexgo.mdbclient.MdbService;


/***************************************************************************************************
 *                                  Copyright (C), Nexgo Inc.                                      *
 *                                    http://www.nexgo.cn                                          *
 ***************************************************************************************************
 * usage           : 
 * Version         : 1
 * Author          : Truth
 * Date            : 2018/1/2
 * Modify          : create file
 **************************************************************************************************/
public class GData {
    private static volatile GData INSTANCE;



    public static GData getInstance(){
        if(INSTANCE == null){
            synchronized (GData.class){
                if (INSTANCE == null){
                    INSTANCE = new GData();
                }
            }
        }
        return INSTANCE;
    }
    private boolean initSuccesss;

    private int mdbDeviceStatus;

    private boolean trading;

    public boolean isInitSuccesss() {
        return initSuccesss;
    }

    public void setInitSuccesss(boolean initSuccesss) {
        this.initSuccesss = initSuccesss;
    }

    public boolean isTrading() {
        return trading;
    }

    public void setTrading(boolean trading) {
        this.trading = trading;
    }

    public int getMdbDeviceStatus() {
        return mdbDeviceStatus;
    }

    public void setMdbDeviceStatus(int mdbDeviceStatus) {
        this.mdbDeviceStatus = mdbDeviceStatus;
    }
}
