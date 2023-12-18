package com.nexgo.apiv3demo;

import com.xinguodu.ddiinterface.Ddi;

import java.util.Arrays;

/**
 * Created by blatand on 2023-1-31 11:32
 */
public class GpioUtils {

    private final static int CMD_SET = 0;
    private final static int CMD_GET = 1;
    private final static int MGR_MDB_GPIO_CTRL = 0x6C76;

    /**
     * set gpio P1.0 ~ P1.5 val
     *
     * @which which gpio (0 1 2 3 4 5)
     * @val gpio value (1 for high, 0 for low)
     * eg :
     * setGpioVal(0, 1) for P1.0 set high
     * setGpioVal(3, 0) for P1.3 set low
     */
    public static void setGpioVal(int which, int val) {
        int nCmd;
        int wlen;
        byte[] wbuf = new byte[1];
        int[] rLen = new int[1];
        byte[] rbuf = new byte[1];
        int[] rStatus = new int[1];

        nCmd = MGR_MDB_GPIO_CTRL;
        wlen = 1;
        wbuf[0] = (byte) ((which << 2) | (val << 1) | CMD_SET);

        // rLen[0] = 1; rbuf[0] = 0; rStatus[0] = ret;
        Ddi.ddi_general_interface(nCmd, wlen, wbuf, rLen, rbuf, rStatus);
    }

    /**
     * get gpio P0.0 ~ P0.5 val
     *
     * @return 0 or 1
     * eg :
     * getGpioVal(0) for get P0.0 val
     * @which which gpio (0 1 2 3 4 5)
     */
    public static int getGpioVal(int which) {
        int val = 0;
        int nCmd;
        int wlen;
        byte[] wbuf = new byte[1];
        int[] rLen = new int[1];
        byte[] rbuf = new byte[1];
        int[] rStatus = new int[1];

        nCmd = MGR_MDB_GPIO_CTRL;
        wlen = 1;
        wbuf[0] = (byte) ((which << 2) | CMD_GET);

        // rLen[0] = 1; rbuf[0] = val; rStatus[0] = ret;
        Ddi.ddi_general_interface(nCmd, wlen, wbuf, rLen, rbuf, rStatus);
        val = rbuf[0];

        return val;
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException pE) {
            pE.printStackTrace();
        }
    }
    private char[] mResult = new char[8];
    private boolean mAlwaysRun = true;
    private void testGpios() {
        int i = 0;
        int val = 0;
        char ok = 1;
        int[] setGpios = {0, 3, 4, 5};
        int[] getGpios = {2, 3, 4, 5};
        int[] saveVal = new int[4];

        mResult[0] = '0';
        mResult[1] = '0';

        for (i = 0; i < 2; i++) {
            if (getGpioVal(i) == 0) {
                mResult[i + 2] = '1';
            } else {
                mResult[i + 2] = '0';
            }
        }

        // save
        for (i = 0; i < 4; i++) {
            saveVal[i] = getGpioVal(setGpios[i]);
        }
        for (i = 0; i < 4; i++) {
            ok = 1;
            setGpioVal(setGpios[i], 1);
            delay(5);
            val = getGpioVal(getGpios[i]);
            ok &= (0 == val ? 1 : 0);

            setGpioVal(setGpios[i], 0);
            delay(5);
            val = getGpioVal(getGpios[i]);
            ok &= (1 == val ? 1 : 0);

            setGpioVal(setGpios[i], 1);
            delay(5);
            val = getGpioVal(getGpios[i]);
            ok &= (0 == val ? 1 : 0);

            mResult[i + 4] = (ok == 1 ? '1' : '0');
        }
        // restore
        for (i = 0; i < 4; i++) {
            setGpioVal(setGpios[i], saveVal[i]);
        }
//        Log.d(TAG, "testGpios: " + Arrays.toString(mResult));

        new Thread(() -> {
            while (mAlwaysRun) {
                setGpioVal(1, 1);
                setGpioVal(2, 1);
                delay(100);
                setGpioVal(1, 0);
                setGpioVal(2, 0);
                delay(100);
            }
        }).start();
    }
}
