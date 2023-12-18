package com.nexgo.apiv3demo;

import android.content.Context;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.oaf.apiv3.device.serialport.SerialCfgEntity;
import com.nexgo.oaf.apiv3.device.serialport.SerialPortDriver;
import com.xinguodu.ddiinterface.Ddi;
import com.xinguodu.ddiinterface.struct.StrComAttr;

public class SerialPortDriverImpl2 implements SerialPortDriver {
    int portNo;
    private final String TAG = "SerialPortService";
    private boolean isPortOpen = false;
    private final int COM_MAX_LEN = 2048;
    private Context mContext;

    SerialPortDriverImpl2(Context context, int port) {
        this.mContext = context;
        this.portNo = port;
    }

    public int connect(SerialCfgEntity entity) {
        try {
            if (entity == null) {
                return -2;
            } else {
                StrComAttr comCfg = new StrComAttr();
                int baud = entity.getBaudRate();
                if (baud != 110 && baud != 300 && baud != 600 && baud != 1200 && baud != 2400 && baud != 4800 && baud != 9600 && baud != 14400 && baud != 56000 && baud != 19200 && baud != 38400 && baud != 57600 && baud != 115200 && baud != 230400 && baud != 460800) {
                    return -4007;
                } else {
                    comCfg.setBaud(baud);
                    switch (entity.getParity()) {
                        case 'e':
                            comCfg.setParity(2);
                            break;
                        case 'n':
                            comCfg.setParity(0);
                            break;
                        case 'o':
                            comCfg.setParity(1);
                            break;
                        default:
                            return -4007;
                    }

                    int dataBits = entity.getDataBits();
                    if (dataBits != 5 && dataBits != 6 && dataBits != 7 && dataBits != 8) {
                        return -4007;
                    } else {
                        comCfg.setDatabits(dataBits);
                        int stopBits = entity.getStopBits();
                        if (stopBits != 1 && stopBits != 2) {
                            return -4007;
                        } else {
                            comCfg.setStopbits(stopBits);
                            LogUtils.debug("baud {},parity {},databits {},stopbits {}", new Object[]{comCfg.getBaud(), comCfg.getParity(), comCfg.getDatabits(), comCfg.getStopbits()});
                            Ddi.ddi_com_close(this.portNo);
                            int result = Ddi.ddi_com_open(this.portNo, comCfg);
                            LogUtils.debug("ddi_com_open portNo {},{}", new Object[]{this.portNo, result});
                            if (result == 0) {
                                this.isPortOpen = true;
                                return 0;
                            } else {
                                return -4001;
                            }
                        }
                    }
                }
            }
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var7) {
            var7.printStackTrace();
            return -4001;
        }
    }

    public int send(byte[] data, int dataLen) {
        try {
            if (data != null && data.length > 0 && data.length >= dataLen && dataLen > 0 && dataLen <= 2048) {
                if (!this.isPortOpen) {
                    return -4004;
                } else {
                    if (data.length > dataLen) {
                        byte[] tmp = new byte[dataLen];
                        System.arraycopy(data, 0, tmp, 0, dataLen);
                        data = tmp;
                    }

                    LogUtils.debug("send data {}", new Object[]{ByteUtils.byteArray2HexString(data)});
                    int result = Ddi.ddi_com_write(this.portNo, data, dataLen);
                    LogUtils.debug("ddi_com_write {}", new Object[]{result});
                    return result > 0 ? 0 : -4002;
                }
            } else {
                return -2;
            }
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var4) {
            var4.printStackTrace();
            return -4002;
        }
    }

    public int recv(byte[] buffer, int recvLen, long timeout) {
        try {
            if (buffer != null && buffer.length > 0 && recvLen > 0 && timeout >= 0L && buffer.length >= recvLen && recvLen <= 2048) {
                if (!this.isPortOpen) {
                    return -4004;
                } else {
                    long timer = System.currentTimeMillis();
                    int result = 0;
                    byte[] data = new byte[recvLen];
                    do {
                        if (timeout != 0L && System.currentTimeMillis() > timeout + timer) {
                            return -4006;
                        }

                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException var10) {
                            var10.printStackTrace();
                        }

                        result = Ddi.ddi_com_read(this.portNo, data, data.length);
                        LogUtils.debug("ddi_com_read {}", new Object[]{result});
                    } while(result == -1);

                    if (result > 0) {
                        System.arraycopy(data, 0, buffer, 0, result);
                        return result;
                    } else if (result == 0) {
                        Ddi.ddi_com_close(this.portNo);
                        return -4008;
                    } else {
                        return -4999;
                    }
                }
            } else {
                return -2;
            }
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var11) {
            var11.printStackTrace();
            return -4999;
        }
    }

    public int recv(byte[] buffer, int recvLen) {
        try {
            if (buffer != null && buffer.length > 0 && buffer.length >= recvLen && recvLen > 0 && recvLen <= 2048) {
                if (!this.isPortOpen) {
                    return -4004;
                } else {
                    int result = Ddi.ddi_com_read(this.portNo, buffer, recvLen);
                    LogUtils.debug("ddi_com_read {}", new Object[]{result});
                    if (result == -1) {
                        return 0;
                    } else {
                        return result <= 0 ? -4002 : result;
                    }
                }
            } else {
                return -2;
            }
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var4) {
            var4.printStackTrace();
            return -4002;
        }
    }

    public int disconnect() {
        try {
            if (!this.isPortOpen) {
                return -4004;
            } else {
                int result = Ddi.ddi_com_close(this.portNo);
                if (result == 0) {
                    this.isPortOpen = false;
                    return 0;
                } else {
                    return -4005;
                }
            }
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var2) {
            var2.printStackTrace();
            return -4005;
        }
    }

    public void clrBuffer() {
        try {
            Ddi.ddi_com_clear(this.portNo);
        } catch (NoSuchMethodError | UnsatisfiedLinkError | Exception var2) {
            var2.printStackTrace();
        }

    }
}
