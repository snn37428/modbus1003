package shop.devs;

import modbus.protocol.ModbusAnswer;
import modbus.protocol.ModbusProtocol;
import modbus.protocol.ModbusRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import shop.excl.ReadExcelService;

import java.util.ArrayList;
import java.util.List;

public class TaskDevice {

    @Autowired
    private ReadExcelService rexs;

    /**
     * ip
     */
    private static String ip;

    /**
     * 端口
     */
    private static String port;

    /**
     * 设备号
     */
    private static String deviceId;

    /**
     * model4 客户端
     */
    private management.DevicesManagement manager = null;

    /**
     * model4 客户端pos
     */
    private int nServerListPos;

    /**
     * model4请求
     */
    private ModbusRequest req = new ModbusRequest();

    private static String configSwtich = "";
    private static int nServerDataType;
    private static int nConvMode;
    private static int nJavaDataType;


    /**
     * 读取model3数据方法
     *
     * @param nFrom
     * @param nNum
     * @return
     */
    public List<String> readDevicesTask3(int nFrom, int nNum, int dataType) {
        loadIp();
        // 结果返回体
        List<String> responseDate = new ArrayList<String>();
        try {
            if (manager == null) {
                manager = new management.DevicesManagement(true);
                nServerListPos = manager.add(ip, Integer.valueOf(port));
            }
            ModbusAnswer ans = new ModbusAnswer();
            if (nServerListPos != -1) {
                //必须设置的内容
                loadConfigSwtich();
                ans.setServerDataType(nServerDataType);
                ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);

                int nError;
                ans.setConvertMode(dataType, ModbusProtocol.CONVMOD_0123_3210,
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
                        responseDate.add(String.valueOf(data));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Task.readTaskList4，读取时异常!!!" + e);
        }
        return responseDate;
    }

    /**
     * 读取model4数据方法
     *
     * @param nAddressFrom
     * @param nDataNum
     * @return
     */
    public List<String> readDevicesTask4(int nAddressFrom, int nDataNum, int dataType) {
        loadIp();
        // 结果返回体
        List<String> responseDate = new ArrayList<String>();
        if (manager == null) {
            manager = new management.DevicesManagement(true);
            nServerListPos = manager.add(ip, Integer.valueOf(port));
        }
        ModbusAnswer ans = new ModbusAnswer();
        try {
            if (nServerListPos != -1) {
                //必须设置的内容
                loadConfigSwtich();
                ans.setServerDataType(nServerDataType);
                ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);

                int nError;
                ans.setConvertMode(dataType, ModbusProtocol.CONVMOD_0123_3210,
                        ModbusProtocol.DATATYPE_JAVA_INT32);

                nError = req.sendReadInputRegister(Integer.parseInt(deviceId), nAddressFrom, nDataNum);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    // System.out.println("sendReadInputRegister-参数设置有效");
                } else {
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //2.发送指令
                nError = manager.write(nServerListPos, req);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    //System.out.println("sendReadInputRegister-发送有效");
                } else {
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //3.接收数据
                nError = manager.read(nServerListPos, ans);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    // System.out.println("sendReadInputRegister-接收有效");
                }

                //4.接收数据成功，则通过该方法读取相应数据
                if (nError == ModbusProtocol.ERROR_NONE) {
                    //注：i的值为第几个数据,因此起点为0,而不是字节数的起点也与nAddressFrom不同
                    int nDataFrom = 0;
                    for (int i = nDataFrom; i < nDataNum; i++) {
                        //选择的数据类型，与setConvertMode方法中设置的Java端数据类型有关

                        if (dataType == 105) {
                            int data = ans.getIntByIndex(i);
                            responseDate.add(String.valueOf(data));
                        } else if (dataType == 107) {
                            float data = ans.getFloatByIndex(i);
                            responseDate.add(String.valueOf(data));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Task.readTaskList3，读取时异常!!!" + e);
        }
        return responseDate;
    }

    /**
     * 读取model1数据方法
     *
     * @param nCoilStart
     * @param nCoilNum
     * @return
     */
    public List<String> readDevicesTask1(int nCoilStart, int nCoilNum) {
        loadIp();
        // 结果返回体
        List<String> responseDate = new ArrayList<String>();
        if (manager == null) {
            manager = new management.DevicesManagement(true);
            nServerListPos = manager.add(ip, Integer.valueOf(port));
        }
        ModbusAnswer ans = new ModbusAnswer();
        try {
            if (nServerListPos != -1) {

                //必须设置的内容
                loadConfigSwtich();
                ans.setServerDataType(nServerDataType);
                ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);
                int nError;
                nError = req.sendReadCoil(Integer.parseInt(deviceId), nCoilStart, nCoilNum);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    //System.out.println("sendReadCoil-参数设置有效");
                } else {
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //2.发送指令
                nError = manager.write(nServerListPos, req);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    //System.out.println("sendReadCoil-发送成功");
                } else {
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //3.接收数据
                nError = manager.read(nServerListPos, ans);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    //System.out.println("sendReadCoil-接收成功");
                } else {
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }
                //4.接收数据后，通过该方法读取相应数据
                if (nError == ModbusProtocol.ERROR_NONE) {
                    for (int i = nCoilStart; i < nCoilStart + nCoilNum; i++) {
                        int nCoilStatus = ans.getBitByIndex(i);
                        responseDate.add(String.valueOf(nCoilStatus));
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Task.readTaskList1，读取时异常!!!" + e);
        }
        return responseDate;
    }

    /**
     * 加载Plc读取客户端公用参数
     */

    private void loadConfigSwtich() {
        if (StringUtils.isBlank(configSwtich)) {
            nServerDataType = ModbusProtocol.DATATYPE_INT32;
            nConvMode = ModbusProtocol.CONVMOD_0123_3210;
            nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;
            //0.1 发送设置
            req.setServerDataType(nServerDataType);
            //设置为需要检查所有反馈信息
            req.setCheckAnswer(ModbusProtocol.CHKASK_ALL);
            setConfigSwtich("1");
        }
    }

    /**
     * 读取静态ip数据
     */
    private void loadIp() {
        if (StringUtils.isBlank(ip)) {
            setIp(rexs.getIp());
            setPort(rexs.getPort());
            setDeviceId(rexs.getDeviceId());
            System.out.println("ip: " + getIp() + ", port: " + getPort() + ",deviceId: " + getDeviceId());
        }
    }

    /**
     * 格式化小数点
     *
     * @param s
     * @return
     */
    private String formatStringToInt(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }
        return s;
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

    public ReadExcelService getRexs() {
        return rexs;
    }

    public void setRexs(ReadExcelService rexs) {
        this.rexs = rexs;
    }

    public static String getConfigSwtich() {
        return configSwtich;
    }

    public static void setConfigSwtich(String configSwtich) {
        TaskDevice.configSwtich = configSwtich;
    }
}
