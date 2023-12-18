package com.nexgo.apiv3demo;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.mdbDemo.R;
import com.nexgo.mdbservice.IPaymentCallback;
import com.nexgo.mdbservice.PaymentAppInfo;
import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.device.mdb.led.MdbLightModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.WorkKeyTypeEnum;
import com.nexgo.oaf.apiv3.emv.AidEntity;
import com.nexgo.oaf.apiv3.emv.CapkEntity;
import com.nexgo.oaf.apiv3.emv.EmvHandler2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.nexgo.mdbclient.ICompletionCallback;
import cn.nexgo.mdbclient.MdbServiceManager;
import cn.nexgo.mdbclient.constant.DeviceMode;
import cn.nexgo.mdbclient.constant.DeviceStatus;
import cn.nexgo.mdbclient.constant.LogLevel;
import cn.nexgo.mdbclient.constant.ReportKey;
import cn.nexgo.mdbclient.constant.ReportType;
import cn.nexgo.mdbclient.constant.TransParam;
import cn.nexgo.mdbclient.constant.VendParam;
import cn.nexgo.mdbclient.constant.VendResult;
import cn.nexgo.mdbclient.constant.VendType;


public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private DeviceEngine deviceEngine;

    public static final Logger mlog = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
    private EmvHandler2 emvHandler2;
    private EmvUtils emvUtils;
    private PinPad pinpad;
    private static final byte[] main_key_data = new byte[16];
    private static final byte[] work_key_data = new byte[16];
    private final int KEY_INDEX = 10;
    private PaymentAppInfo paymentAppInfo;

    private Button btnOpenMdb;
    private Button btnCloseMdb;
    private Button btnBeginSession;

    private Button btnCancelSession;
    private SensorManager mSensorManager;
    private Sensor mLight;

    private Spinner spFeatureLevel;
    private Spinner spDeviceMode;
    private static byte LEVEL_3 = 0x03;
    private static byte LEVEL_2 = 0x02;
    private static byte LEVEL_1 = 0x01;
    private static byte LEVEL_DEFAULT = 0x00;
    private static byte FEATURE_LEVEL = LEVEL_3;

    private ArrayList<MdbLightModeEnum> mdbLightModeEnumArrayList = new ArrayList<>();
    private void initKey() {
        Arrays.fill(main_key_data, (byte) 0xFF);
        System.arraycopy(ByteUtils.hexString2ByteArray("F616DD76F290635EF616DD76F290635E"), 0, work_key_data, 0, 16);

        int result = pinpad.writeMKey(KEY_INDEX, main_key_data, main_key_data.length);
        mlog.debug("writeMKey result:{}", result);
        mlog.debug("isKeyExist:{}", result);
        result = pinpad.writeWKey(KEY_INDEX, WorkKeyTypeEnum.MACKEY, work_key_data, work_key_data.length);
        mlog.debug("writeWKey MACKEY result:{}", result);
        result = pinpad.writeWKey(KEY_INDEX, WorkKeyTypeEnum.PINKEY, work_key_data, work_key_data.length);
        mlog.debug("writeWKey PINKEY result:{}", result);
        result = pinpad.writeWKey(KEY_INDEX, WorkKeyTypeEnum.TDKEY, work_key_data, work_key_data.length);
        mlog.debug("writeWKey TDKEY result:{}", result);
        result = pinpad.writeWKey(KEY_INDEX, WorkKeyTypeEnum.ENCRYPTIONKEY, work_key_data, work_key_data.length);
        mlog.debug("writeWKey ENCRYPTIONKEY result:{}", result);
    }
    private void initLed(){

        mdbLightModeEnumArrayList.add(MdbLightModeEnum.MAG_BLUE);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.MAG_RED);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.MAG_GREEN);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.IC_GREEN);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.IC_BLUE);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.IC_RED);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.RF_GREEN);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.RF_BLUE);
        mdbLightModeEnumArrayList.add(MdbLightModeEnum.RF_RED);
    }
    private Handler mMainHandler;
    private Handler mInitParamHandler;
    void initData(){

        PaymentAppInfo info = new PaymentAppInfo();
        info.setClientId("XGD");
        info.setModel(deviceEngine.getDeviceInfo().getModel());
        info.setCountryCode("0156");

/*
      ActualPrice = P * X * pow(10,-Y)  P is the price,X is the scaled factor,and y is number of decimal place
      If  P is 100, X(scale factor) is 1, and Y(decimal place) is 2,the actual price is 1.
*/

        info.setDecimalPlaces((byte) 0x02);
        info.setScaleFactor(1);

        info.setResponseTime((byte) 0x3C);
        //if release,set LogLevel.INFO as default;
        info.setLogLevel(LogLevel.INFO.ordinal());
        info.setEnableAlwaysIdle(false);
        info.setReaderFeatureLevel(FEATURE_LEVEL);

        //only level 3 support always idle mode and monetary format
        if(FEATURE_LEVEL==LEVEL_3){
            info.setEnableAlwaysIdle(true);
            //- 0 = 16 bit monetary format, 1 = 32 bit monetary format;  set 0 as default,didn't support 1
            info.setMonetaryFormat(0);
        }
        //add 1.8.0: when the cashless device receive the ReValue cmd, if setRevalueApproved to true, it will reply Revalue Approved, otherwise,reply Revalue Denied.
        info.setRevalueApproved(true);
        //add 1.8.0: set the Z8 bit0 value, the response of SETUP - Config Data(11 00) command,
        //if false,b0=0,do not request refunds, otherwise,may request the refunds.
        info.setRequestRefunds(false);
        //add 1.9.0: if set the mode to Device.TRANSMIT, the UN20 will be as man in middle role.
        info.setDeviceMode(DeviceMode.CASHLESS);
        //if set false, UN20 will not notify the status in the screen. default ture
        info.setNotifyStatus(false);
        //add 2.0.0: set false as the default value, if set true,the UN20 will ACK first,and respond command to VMC in next poll command.
        info.setRespondAckFirst(false);

        paymentAppInfo = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spFeatureLevel = findViewById(R.id.sp_featureLevel);
        spDeviceMode = findViewById(R.id.sp_deviceMode);

        btnOpenMdb = findViewById(R.id.open_mdb);
        btnCloseMdb = findViewById(R.id.close_mdb);
        btnBeginSession = findViewById(R.id.beginSession);
        btnCancelSession = findViewById(R.id.cancelSession);

        btnOpenMdb.setOnClickListener(this);
        btnCloseMdb.setOnClickListener(this);
        btnBeginSession.setOnClickListener(this);
        btnCancelSession.setOnClickListener(this);

        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        emvHandler2 = deviceEngine.getEmvHandler2("app2");
        emvUtils = new EmvUtils(MainActivity.this);
        pinpad = deviceEngine.getPinPad();
        pinpad.setAlgorithmMode(AlgorithmModeEnum.DES);
        pinpad.setPinKeyboardMode(PinKeyboardModeEnum.FIXED);


        HandlerThread initParamThread = new HandlerThread("initParamThread", -10);
        initParamThread.start();
        mMainHandler = new Handler(Looper.getMainLooper());
        mInitParamHandler = new Handler(initParamThread.getLooper());

        initData();
        initLed();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if(FEATURE_LEVEL==LEVEL_3&&paymentAppInfo.isEnableAlwaysIdle()){
            btnBeginSession.setVisibility(View.GONE);
            btnCancelSession.setVisibility(View.GONE);
        }else{
            btnBeginSession.setEnabled(false);
            btnBeginSession.setClickable(false);

            btnCancelSession.setEnabled(false);
            btnCancelSession.setClickable(false);
        }

        spFeatureLevel.setSelection(paymentAppInfo.getReaderFeatureLevel());
        spFeatureLevel.setClickable(false);
        spFeatureLevel.setEnabled(false);
        spFeatureLevel.setOnItemSelectedListener(this);


        spDeviceMode.setSelection(paymentAppInfo.getDeviceMode());
        spDeviceMode.setClickable(false);
        spDeviceMode.setEnabled(false);
        spDeviceMode.setOnItemSelectedListener(this);


        initMdbLed();
    }


    private void initMdbLed(){

        setMdbLed(false);
    }

    private void setMdbLed(boolean turnOn){
        for(int i=0;i<mdbLightModeEnumArrayList.size();i++){
            APIProxy.getDeviceEngine(getApplicationContext()).getMdbLEDDriver().setLed(mdbLightModeEnumArrayList.get(i), turnOn);
        }
    }
    private void postMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mlog.debug("call onResume() GData.getInstance().isInitSuccesss():{}",GData.getInstance().isInitSuccesss());
        LogUtils.setDebugEnable(true);
        if(!GData.getInstance().isInitSuccesss()){

            GData.getInstance().setInitSuccesss(true);
            mInitParamHandler.post(new Runnable() {
                @Override
                public void run() {
                    initEmvAid();
                    initEmvCapk();
                    initKey();

                    GData.getInstance().setTrading(false);
                    MdbServiceManager.getInstance().onStart(getApplicationContext(),
                            paymentAppInfo,
                            iPaymentCallback,
                            iCompletionCallback);
                }
            });
        }
        btnOpenMdb.setClickable(false);
        btnOpenMdb.setEnabled(false);
        mSensorManager.registerListener(this,mLight,SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        initMdbLed();
    }

    @Override
    protected void onDestroy() {
        mlog.debug("call onDestroy()");
        closeMdbService();
        super.onDestroy();

    }

    private void setSessionButtonStatus(boolean status){

        btnBeginSession.setVisibility(status?View.VISIBLE:View.GONE);
        btnBeginSession.setEnabled(status);
        btnBeginSession.setClickable(status);

        btnCancelSession.setVisibility(status?View.VISIBLE:View.GONE);
        btnCancelSession.setEnabled(status);
        btnCancelSession.setClickable(status);
    }
    private final ICompletionCallback iCompletionCallback = new ICompletionCallback() {
        @Override
        public void onSuccess() {
            mlog.debug("call mdbClient success!!");
        }

        @Override
        public void onFailure(int retCode) {
            mlog.debug("call mdbClient failure retCode:{}",retCode);
        }

        @Override
        public void onAppUpdate() {
            mlog.debug("call onAppUpdate() isTrading:{}",GData.getInstance().isTrading());
            if(!GData.getInstance().isTrading()){
                restartMdbService();
            }
        }
    };

    private final IPaymentCallback iPaymentCallback = new IPaymentCallback.Stub() {
        @Override
        public int onPay(final int tradeType, final String itemPrice, final String itemNumber) {
            mlog.debug("call onPay() tradeType:{} itemPrice:{} itemNumber:{} isTrading:{}",
                    tradeType, itemPrice, itemNumber,GData.getInstance().isTrading());

            if(GData.getInstance().isTrading()){
                return 0;
            }
            GData.getInstance().setTrading(true);
            postMainThread(new Runnable() {
                @Override
                public void run() {
                    if(tradeType == VendType.CASH_SALE.ordinal()){
                        Toast.makeText(MainActivity.this,
                                "Cash Sale" +
                                "\n" +
                                "itemPrice:"+itemPrice +
                                "\n" +
                                "itemNumber:"+itemNumber, Toast.LENGTH_SHORT).show();
                        GData.getInstance().setTrading(false);
                    }else{

                        if(itemPrice.contains("-")){
                            return;
                        }
                        if(Integer.parseInt(itemNumber)==0){
                            return;
                        }

                        Intent saleIntent = new Intent(MainActivity.this, EmvActivity2.class);
                        saleIntent.putExtra(TransParam.REQUEST_TYPE, tradeType);
                        saleIntent.putExtra(TransParam.ORDER_AMOUNT, itemPrice);
                        saleIntent.putExtra(TransParam.ORDER_NUMBER, itemNumber);
                        saleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(saleIntent);
                    }
                }
            });
            return 0;
        }

        @Override
        public void notifyMdbStatus(int mdbDeviceStatus) {
            mlog.debug("isTrading:{} mdbStatus:{}",GData.getInstance().isTrading(),DeviceStatus.values()[mdbDeviceStatus]);
            if(mdbDeviceStatus == DeviceStatus.SERVICE_ERR.ordinal()){
                restartMdbService();
            }
            boolean on = (mdbDeviceStatus >= DeviceStatus.ENABLE.ordinal() && (!GData.getInstance().isTrading()));

            APIProxy.getDeviceEngine(getApplicationContext()).getMdbLEDDriver().setLed(MdbLightModeEnum.MAG_RED, on);
            APIProxy.getDeviceEngine(getApplicationContext()).getMdbLEDDriver().setLed(MdbLightModeEnum.RF_RED, on);
            APIProxy.getDeviceEngine(getApplicationContext()).getMdbLEDDriver().setLed(MdbLightModeEnum.IC_RED, on);

            if(mdbDeviceStatus==DeviceStatus.ENABLE.ordinal()&&
                    (!GData.getInstance().isTrading())&&
                    (!paymentAppInfo.isEnableAlwaysIdle())){

                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            setSessionButtonStatus(true);
                        }
                    });
            }
        }


        @Override
        public void notifyVendResult(int result) {
            switch (VendResult.values()[result]){

                case VEND_SUCCESS:
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Vend Success", Toast.LENGTH_SHORT).show();
                        }
                    });
                    GData.getInstance().setTrading(false);
                    break;
                case VEND_FAILURE:
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Vend FAILURE", Toast.LENGTH_SHORT).show();
                        }
                    });
                    GData.getInstance().setTrading(false);
                    break;
                case VEND_END_SESSION:
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"End Session", Toast.LENGTH_SHORT).show();
                            ActivityCollector.finishAllActivity();
                        }
                    });
                    GData.getInstance().setTrading(false);
                    break;
                case VEND_CANCEL:
                    ActivityCollector.finishAllActivity();
                    break;
            }
        }

        @Override
        public void notifyVendParam(final int type, final byte[] data) {
            mlog.debug("type:{} data:{}", VendParam.values()[type],ByteUtils.byteArray2HexString(data));
            postMainThread(new Runnable() {
                @Override
                public void run() {
                    if(type== VendParam.IDLE_MODE.ordinal()){
                        switch (data[0]){
//                    0-idle 1-always idle
                            case 0:
                                setSessionButtonStatus(true);
                                paymentAppInfo.setEnableAlwaysIdle(false);
                                break;
                            case 1:
                            default:
                                setSessionButtonStatus(false);
                                paymentAppInfo.setEnableAlwaysIdle(true);
                                break;
                        }
                    }
                }
            });
        }

        @Override
        public void onPayResult(String payResult) {
            /* json string format
                {
                    "itemSlot": 1,
                    "itemInfo": [
                        {
                            "itemAmount": "100",
                            "paymentMethod": 2
                        }
                    ],
                    "vendStatus": true,
                    "machineStatus": 0
                }
            */

            try {
                JSONObject payRes = new JSONObject(payResult);
                mlog.info("payRes:{}", payRes);
                JSONArray itemInfo = payRes.optJSONArray("itemInfo");
                int totalAmt = 0;
                for(int i=0;i<itemInfo.length();i++){
                    JSONObject itemInfoObject = itemInfo.getJSONObject(i);
                    String itemAmount = itemInfoObject.getString("itemAmount");
                    int paymentMethod = itemInfoObject.getInt("paymentMethod");
                    totalAmt += Integer.parseInt(itemAmount);
                }
                mlog.info("itemSlot:{} totalAmt:{} vendStatus:{} machineStatus:{}",
                        payRes.opt("itemSlot"),
                        totalAmt,payRes.opt("vendStatus"),
                        payRes.opt("machineStatus"));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    };


    private void closeMdbService() {
        MdbServiceManager.getInstance().onClose();
    }

    private void restartMdbService() {
        mlog.debug("call restartMdbService()");
        MdbServiceManager.getInstance().onClose();
        MdbServiceManager.getInstance().onStart(getApplicationContext(),
                paymentAppInfo,
                iPaymentCallback,
                iCompletionCallback);
    }

    private void initEmvAid() {
        emvHandler2.delAllAid();
        if (emvHandler2.getAidListNum() <= 0) {
            List<AidEntity> aidEntityList = emvUtils.getAidList();
            if (aidEntityList == null) {
                mlog.debug("initAID failed");
                return;
            }
            int i = emvHandler2.setAidParaList(aidEntityList);
            mlog.debug("setAidParaList:{} ", i);
        } else {
            mlog.debug("setAidParaList " + "already load aid");
        }
    }


    private void initEmvCapk() {
        emvHandler2.delAllCapk();
        int capk_num = emvHandler2.getCapkListNum();
        mlog.debug("capk_num:{} ", capk_num);
        if (capk_num <= 0) {
            List<CapkEntity> capkEntityList = emvUtils.getCapkList();
            if (capkEntityList == null) {
                mlog.debug("initCAPK failed");
                return;
            }
            int j = emvHandler2.setCAPKList(capkEntityList);
            mlog.debug("setCAPKList:{} ", j);
        } else {
            mlog.debug("setCAPKList already load capk");
        }

    }

    private boolean mdbRunning = false;
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_mdb:
                mlog.debug("start to open mdb!!");
                mdbRunning = true;
                needTip = true;
                GData.getInstance().setTrading(false);
                MdbServiceManager.getInstance().onStart(getApplicationContext(),
                        paymentAppInfo,
                        iPaymentCallback,
                        iCompletionCallback);


                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        btnOpenMdb.setClickable(false);
                        btnOpenMdb.setEnabled(false);
                        spFeatureLevel.setClickable(false);
                        spFeatureLevel.setEnabled(false);
                        spDeviceMode.setClickable(false);
                        spDeviceMode.setEnabled(false);
                    }
                });

                break;
            case R.id.close_mdb:
                mlog.debug("close mdb!!");
                GData.getInstance().setTrading(false);
                closeMdbService();
                mdbRunning = false;
                initMdbLed();
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        btnOpenMdb.setClickable(true);
                        btnOpenMdb.setEnabled(true);
                        spFeatureLevel.setClickable(true);
                        spFeatureLevel.setEnabled(true);
                        spDeviceMode.setClickable(true);
                        spDeviceMode.setEnabled(true);

                        setSessionButtonStatus(true);
                    }
                });
                break;

            case R.id.beginSession:
                JSONObject report = new JSONObject();
                try {
                    report.put(ReportKey.TYPE, ReportType.BEGIN_SESSION.ordinal());
                    report.put(ReportKey.FUNDS,"FFFF");
                    MdbServiceManager.getInstance().reportToVMC(report.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        btnBeginSession.setClickable(false);
                        btnBeginSession.setEnabled(false);
                    }
                });
                break;

            case R.id.cancelSession:
                JSONObject cancelReport = new JSONObject();
                try {
                    cancelReport.put(ReportKey.TYPE, ReportType.CANCEL_SESSION_REQUEST.ordinal());
                    MdbServiceManager.getInstance().reportToVMC(cancelReport.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postMainThread(new Runnable() {
                    @Override
                    public void run() {
                        btnCancelSession.setClickable(false);
                        btnCancelSession.setEnabled(false);
                    }
                });
                break;
            default:
                break;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            mlog.debug("event.values[0]:{}",event.values[0]);
            if(event.values[0]==0.0){
                mlog.debug("Someone is approaching!!");
            }else if(event.values[0]==1.0){
                mlog.debug("Someone left!!");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private boolean needTip = true;
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {

        switch (adapterView.getId()){
            case R.id.sp_featureLevel:
                mlog.debug("call onItemSelected() index:{} level:{} needTip:{} ",index,paymentAppInfo.getReaderFeatureLevel(),needTip);

                byte featureLevel = paymentAppInfo.getReaderFeatureLevel();

                paymentAppInfo.setReaderFeatureLevel((byte)(index));
                if(paymentAppInfo.getReaderFeatureLevel()==LEVEL_3){
                    paymentAppInfo.setEnableAlwaysIdle(true);
                    //- 0 = 16 bit monetary format, 1 = 32 bit monetary format;  set 0 as default,didn't support 1
                    paymentAppInfo.setMonetaryFormat(0);
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            btnBeginSession.setVisibility(View.GONE);
                            btnCancelSession.setVisibility(View.GONE);
                        }
                    });
                }else{
                    paymentAppInfo.setEnableAlwaysIdle(false);
                    postMainThread(new Runnable() {
                        @Override
                        public void run() {
                            btnBeginSession.setVisibility(View.VISIBLE);
                            btnCancelSession.setVisibility(View.VISIBLE);
                        }
                    });
                }
                if((featureLevel!=(index))&&needTip){
                    Toast.makeText(this,"Need to wait for 2 mins to start mdb \nafter change the Feature Level", Toast.LENGTH_SHORT).show();
                    needTip = false;
                }else{
                    needTip = true;
                }
                break;

            case R.id.sp_deviceMode:
                mlog.debug("call onItemSelected() index:{} deviceMode:{} mdbRunning:{}",index,paymentAppInfo.getDeviceMode(),mdbRunning);

                if(!mdbRunning){
                    paymentAppInfo.setDeviceMode(index);
                }
                break;
            default:
                break;
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
