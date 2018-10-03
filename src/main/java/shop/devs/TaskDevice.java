package shop.devs;

import modbus.protocol.ModbusAnswer;
import modbus.protocol.ModbusProtocol;
import modbus.protocol.ModbusRequest;

import java.util.ArrayList;
import java.util.List;

public class TaskDevice {

    private static String ip;
    private static String port;
    private static String deviceId;

    private static boolean bDebug = true;
    private static management.DevicesManagement manager = new management.DevicesManagement(bDebug);
    private int nServerListPos = manager.add(ip, Integer.valueOf(port));
    private static ModbusRequest req = new ModbusRequest();
    private static ModbusAnswer ans = new ModbusAnswer();

    public List<Float> readDevicesTask4(int nFrom, int nNum) {
        List<Float> responseDate = new ArrayList<Float>();
        if (nServerListPos != -1) {
            int nServerDataType = ModbusProtocol.DATATYPE_INT32;
            int nConvMode = ModbusProtocol.CONVMOD_0123_0123;
            int nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;

            int nError;
            //0.1 发送设置
            req.setServerDataType(nServerDataType);
            //设置为需要检查所有反馈信息
            req.setCheckAnswer(ModbusProtocol.CHKASK_ALL);
            //必须设置的内容
            ans.setServerDataType(nServerDataType);
            ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);
            ans.setConvertMode(ModbusProtocol.DATATYPE_INT32, ModbusProtocol.CONVMOD_0123_3210,
                    ModbusProtocol.DATATYPE_JAVA_FLOAT32);
            //1.设置发送指令参数
            nError = req.sendReadHoldingRegister(Integer.parseInt(deviceId), nFrom, nNum);
            if (nError == ModbusProtocol.ERROR_NONE) {
//                System.out.println("--send-start");
            } else {
                System.out.println(ModbusProtocol.getErrorMessage(nError));
            }
            //2.发送指令
            nError = manager.write(nServerListPos, req);
            if (nError == ModbusProtocol.ERROR_NONE) {
//                System.out.println("--send-success");
            } else {
                System.out.println(ModbusProtocol.getErrorMessage(nError));
            }
            //3.接收数据
            nError = manager.read(nServerListPos, ans);
            if (nError == ModbusProtocol.ERROR_NONE) {
//                System.out.println("--accept-success");
            } else {
                System.out.println(ModbusProtocol.getErrorMessage(nError));
            }
            //4.接收数据后，通过该方法读取相应数据
            if (nError == ModbusProtocol.ERROR_NONE) {
                for (int i = nFrom; i < nFrom + nNum; i++) {
                    //选择方法与Java端数据类型有关
                    //int data = ans.getIntByIndex(i);
                    float data = ans.getFloatByIndex(i - nFrom);
                    responseDate.add(data);
                }
            }
        }
        return responseDate;
    }

    /**
     * 读取静态数据
     */
    private void loadIp() {
//        setIp(res.getIp());
//        setPort(res.getPort());
//        setDeviceId(res.getDeviceId());
//        System.out.println("ip: " + getIp() + ", port: " + getPort() + ",deviceId: " + getDeviceId());
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        TaskDevice.ip = ip;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        TaskDevice.port = port;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        TaskDevice.deviceId = deviceId;
    }
}
