package com.nexgo.apiv3demo;

import android.app.Application;

import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;

/**
 * Created by xiaox on 16/4/28.
 */
public class NexgoApplication extends Application {
    public DeviceEngine deviceEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceEngine = APIProxy.getDeviceEngine(this);
		deviceEngine.getEmvHandler2("app2");
    }
}
