package shop.devs;

import modbus.Modbus;
import modbus.protocol.ModbusAnswer;
import modbus.protocol.ModbusProtocol;
import modbus.protocol.ModbusRequest;

public class DevicesManagement {
    public static final int MB_ERROR = -1;    //遇到错误
    public static final int MB_MAYBEWAIT = -2;    //可能需要等待一次
    public static final int MODSVR_NUM = 32;        //预定义可访问的Modbus Server最大数量
    public static final int SENDBUF_LEN = 261;        //Modbus网络发送缓冲区长度
    public static final int RECVBUF_LEN = 261;        //Modbus网络接收缓冲区长度


    /*
     * Error Code List
     */

    public static final int ERROR_TYPE_OPERATE = 0;
    public static final int ERROR_TYPE_FORMAT = ERROR_TYPE_OPERATE + 1;

    public static final int ERROR_NONE = ModbusProtocol.ERROR_NONE;

    public static final int ERROR_OPERATE_BEGIN = -2000;
    public static final int ERROR_ADD_FAILED = ERROR_OPERATE_BEGIN;
    public static final int ERROR_SERVERPOS = ERROR_ADD_FAILED - 1;
    public static final int ERROR_INVALID_SOCKET = ERROR_SERVERPOS - 1;
    public static final int ERROR_OPERATE_END = ERROR_INVALID_SOCKET;


    private boolean m_bDebug = true;

    private int m_nUsingDevices = 0;

    private Modbus m_modbusDevices[];
    private int m_nDevicesStatus[];

    public DevicesManagement(boolean debug) {
        m_bDebug = debug;

        initAll();
    }

    /**
     * initial the device List & status
     * however there is no device created
     * the device can be created only by adding method 'add()'
     */
    public void initAll() {

        m_modbusDevices = new Modbus[MODSVR_NUM];
        m_nDevicesStatus = new int[MODSVR_NUM];

        for (int i = 0; i < MODSVR_NUM; i++) {
            m_nDevicesStatus[i] = Modbus.MODBUS_STATUS_NULL;
        }
    }

    /**
     * Close All Connected Devices
     */
    public void closeAll() {
        for (int i = 0; i < MODSVR_NUM; i++) {
            if (m_nDevicesStatus[i] != Modbus.MODBUS_STATUS_NULL) {
                m_modbusDevices[i].close();
                m_nDevicesStatus[i] = Modbus.MODBUS_STATUS_CLOSED;
            }
        }
    }

    /**
     * add a device and connect the new device to the server
     *
     * @param strIPAddress
     * @param nPort
     * @return succeed:Index of the device, failed: -1
     */
    public int add(String strIPAddress, int nPort) {

        int nAddIndex = -1;

        //Check whether exists the same connnection
        for (int i = 0; i < MODSVR_NUM; i++) {
            if (m_nDevicesStatus[i] != Modbus.MODBUS_STATUS_NULL) {
                String address = m_modbusDevices[i].getIPAddress();
                if (strIPAddress.equals(address)) {
                    showErrorMessage(ERROR_ADD_FAILED);
                    break;
                }
            }
        }

        if (nAddIndex == -1 && m_nUsingDevices < MODSVR_NUM) {

            nAddIndex = getUnusedIndex();

            // add a new modbus device to the list
            m_modbusDevices[nAddIndex] = new Modbus(strIPAddress, nPort);
            m_nDevicesStatus[nAddIndex] = Modbus.MODBUS_STATUS_SETUP;

            try {
                m_modbusDevices[nAddIndex].connect();
                m_nDevicesStatus[nAddIndex] = Modbus.MODBUS_STATUS_CONNECTED;

                if (m_bDebug) //System.out.println("Adding succeed, Modbus: " + m_modbusDevices[nAddIndex]);

                    m_nUsingDevices++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return nAddIndex;
    }


    /**
     * Get the minimal index of one unused list number
     *
     * @return
     */
    private int getUnusedIndex() {

        int nIndex = 0;

        for (int i = 0; i < MODSVR_NUM; i++) {
            if (m_nDevicesStatus[i] == Modbus.MODBUS_STATUS_NULL) {
                nIndex = i;
                break;
            }
        }

        return nIndex;
    }

    /**
     * close the Modbus Device, directed by index
     *
     * @param nIndex
     * @return succeed 1, Failed 0
     */
    public int close(int nIndex) {
        int index = -1;
        if (nIndex >= 0 && nIndex < MODSVR_NUM) {

            if (m_nDevicesStatus[index] != Modbus.MODBUS_STATUS_NULL) {
                m_modbusDevices[nIndex].close();
                m_nDevicesStatus[index] = Modbus.MODBUS_STATUS_CLOSED;

                if (m_bDebug) System.out.println("Remove modbus device:" + m_modbusDevices[nIndex]);
            }
        }
        return index;
    }


    /**
     *
     * @param nMbServerListPos    本次调用在最多32个PLC列表中的位置(使用AddServer得到的返回)
    //	 * @param nDeviceId    		Modbus协议中Slave(1~32)
    //	 * @param nFnCode			Modbus协议中FunCode(1\2\3\4\5\6\15\16)
    //	 * @param nProtAddress		Modbus协议地址(0起始的寄存器地址)
    //	 * @param nDataCount		JAVA业务级数据的数量
    //	 * @param nServerDataType	存放在服务器端的数据类型(PLC侧) LONG32/SHORT16/FLOAT32
     * @param nClientDataType    返回给调用者的数据类型(JAVA侧) INT32/FLOAT32
     * @param nConvMode            字节转换模式
     * @return
     */
    /**
     * @param nMbServerListPos 本次调用在最多32个PLC列表中的位置(使用AddServer得到的返回)
     * @param ans              所有与响应有关参数在Answer里设置和读取
     * @return
     */
    public int read(int nMbServerListPos, ModbusAnswer ans) {

        // Check the validation of the socket
        int nChecked = checkSocket(nMbServerListPos);

        if (nChecked != ERROR_NONE) {
            return nChecked;
        }

        // Receive message
        nChecked = m_modbusDevices[nMbServerListPos].read(ans);

        if (m_bDebug && nChecked != ModbusProtocol.ERROR_NONE) {
            System.out.println(ModbusProtocol.getErrorMessage(nChecked));
        }

        return nChecked;

    }


    /**
     *
     * @param nMbServerListPos    本次调用在最多32个PLC列表中的位置(使用AddServer得到的返回)
     * @param nDeviceId            Modbus协议中Slave(1~32)
     * @param nFnCode            Modbus协议中FunCode(1|2|3|4|5|6|15|16)
     * @param nProtAddress        Modbus协议地址(0起始的Modbus地址  由功能码决定为s 点位/寄存器)
     * @param nDataCount        JAVA业务级数据的数量
     * @param nServerDataType    存放在服务器端的数据类型(PLC侧) LONG32 | SHORT16 | FLOAT32
     * @param nChkAns            是否检查Fn5\Fn15\Fn6\Fn16的通信应答
     * @return
     */
    /**
     * @param nMbServerListPos 本次调用在最多32个PLC列表中的位置(使用AddServer得到的返回)
     * @param req              所有与发送相关参数在request里设置
     * @return
     */
    public int write(int nMbServerListPos, ModbusRequest req) {

        // Check the Validation of the Socket
        int nChecked = checkSocket(nMbServerListPos);

        // Send Message
        m_modbusDevices[nMbServerListPos].write(req);

        return nChecked;
    }

    /**
     * Check the Validation of the General Parameters
     * including:
     *
     * @param nMbServerListPos
     * @return
     */
    private int checkSocket(int nMbServerListPos) {
        //Check the validation of the device position in the list according nMbServerListPos
        if (nMbServerListPos < 0 || nMbServerListPos >= MODSVR_NUM) {
            showErrorMessage(ERROR_SERVERPOS, nMbServerListPos);
            return -1;
        }

        // check whether the device is existed and
        if (m_modbusDevices[nMbServerListPos] == null ||
                !m_modbusDevices[nMbServerListPos].isConnected()) {
            showErrorMessage(ERROR_INVALID_SOCKET, nMbServerListPos);
            return -1;
        }


        return ERROR_NONE;
    }

    private void showErrorMessage(int nError) {
        showErrorMessage(nError, 0);
    }

    private void showErrorMessage(int nError, int nParam) {
        String message = null;
        int nErrorType = -1;
        int language = 0;  //0-中文   1-英文

        switch (nError) {
            case ERROR_ADD_FAILED:
                nErrorType = ERROR_TYPE_OPERATE;
                if (language == 0) message = "添加失败, 已经存在到相同连接";
                else message = "adding failed, there has been the same slave.";
                break;
            case ERROR_INVALID_SOCKET:
                nErrorType = ERROR_TYPE_OPERATE;
                if (language == 0) message = "Socket无效或者未打开";
                else message = "invalid or unopened socket.";
                break;
        }

        String strErrorType = "";
        switch (nErrorType) {
            case ERROR_TYPE_FORMAT:
                if (language == 0) strErrorType = "FORMAT";
                else strErrorType = "格式错误";
                break;
            case ERROR_TYPE_OPERATE:
                if (language == 0) strErrorType = "OPERATE";
                else strErrorType = "操作错误";
                break;
        }

        if (message != null) {
            System.out.println(strErrorType + "[" + nError + "]:" + message);
        }
    }


    public static void main(String[] args) {

        boolean bDebug = true;

        management.DevicesManagement manager = new management.DevicesManagement(bDebug);

        /*
         * STEP1: 添加新的PLC设备
         */
        int nServerListPos = manager.add("192.168.1.111", Modbus.DEFAULT_PORT);

        //int nget = Float.floatToIntBits((float) 1.00);
        //System.out.println("1.00: " + Integer.toHexString(nget));
        //nget = Float.floatToIntBits((float) 4.16);
        //System.out.println("4.16: " + Integer.toHexString(nget));


        /*
         * 设置接收数据对象ModbusAnswer
         * 如果不是多线程可以共用一个ModbusAnswer, 节省空间和时间
         * 如果多线程，可以为每一个连接线程 设计一个ModbusAnswer对象
         */
        //byte[] byteRecSour = new byte[ModbusProtocol.MAX_BYTE_SIZE];
        //byte[] byteRecDesc = new byte[ModbusProtocol.MAX_BYTE_SIZE];
        //int int32 [] = new int[ModbusProtocol.MAX_BYTE_SIZE / 2];
        //float float32[] = new float[ModbusProtocol.MAX_BYTE_SIZE / 4];


        ModbusRequest req = new ModbusRequest();
        ModbusAnswer ans = new ModbusAnswer();

        while (true) {

            if (nServerListPos != -1) {

                int nDeviceId = 1;                    //设备ID

                int nServerDataType = ModbusProtocol.DATATYPE_INT32;
                int nConvMode = ModbusProtocol.CONVMOD_0123_0123;
                int nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;

                int nError;

                //0.初始化设置发送对象和接收对象的参数
                //0.1 发送设置
                req.setServerDataType(nServerDataType);
                req.setCheckAnswer(ModbusProtocol.CHKASK_ALL);    //设置为需要检查所有反馈信息

                //0.1 接收设置
                ans.setServerDataType(nServerDataType);            //必须设置的内容
                ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);

	    		/*
	    		//----------功能码01--------------------
	    		// -- tested --
	    		//1.设置发送指令参数
	    		int nCoilStart = 2;
	    		int nCoilNum = 8;
	    		nError = req.sendReadCoil(nDeviceId, nCoilStart, nCoilNum);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadCoil-参数设置有效");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//2.发送指令
    			nError = manager.write(nServerListPos, req);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadCoil-发送成功");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//3.接收数据
    			nError =manager.read(nServerListPos, ans);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadCoil-接收成功");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//4.接收数据后，通过该方法读取相应数据
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			for(int i = nCoilStart; i< nCoilStart + nCoilNum; i++){
	    				int nCoilStatus = ans.getBitByIndex(i);
	    				System.out.println(i+":"+nCoilStatus);
	    			}
    			}
    			*/

    			/*
	    		//----------功能码02--------------------
	    		// -- tested --
	    		//1.设置发送指令参数
	    		int nCoilStart = 3;
	    		int nCorlNum = 10;
	    		nError = req.sendReadInputStates(nDeviceId, nCoilStart, nCorlNum);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadInputStates-参数设置有效");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//2.发送指令
    			manager.write(nServerListPos, req);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadInputStates-发送成功");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//3.接收数据
    			manager.read(nServerListPos, ans);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendReadInputStates-接收成功");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//4.接收数据后，通过该方法读取相应数据
    			for(int i = nCoilStart; i< nCoilStart + nCorlNum; i++){
    				int nStatus = ans.getBitByIndex(i);
    				System.out.println(i+":"+nStatus);
    			}
    			*/



                //----------功能码03--------------------
                // -- tested --
                //0.设置系统参数
                ans.setConvertMode(ModbusProtocol.DATATYPE_INT32, ModbusProtocol.CONVMOD_0123_3210,
                        ModbusProtocol.DATATYPE_JAVA_FLOAT32);

                //1.设置发送指令参数
                int nFrom = 0;
                int nNum = 14;
                nError = req.sendReadHoldingRegister(nDeviceId, nFrom, nNum);
                if (nError == ModbusProtocol.ERROR_NONE ){
                    System.out.println("sendReadHoldingRegister-参数设置有效");
                }else{
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //2.发送指令
                nError = manager.write(nServerListPos, req);
                if (nError == ModbusProtocol.ERROR_NONE ){
                    System.out.println("sendReadHoldingRegister-发送成功");
                }else{
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //3.接收数据
                nError = manager.read(nServerListPos, ans);
                if (nError == ModbusProtocol.ERROR_NONE ){
                    System.out.println("sendReadHoldingRegister-接收成功");
                }else{
                    System.out.println(ModbusProtocol.getErrorMessage(nError));
                }

                //4.接收数据后，通过该方法读取相应数据
                if (nError == ModbusProtocol.ERROR_NONE){
                    for(int i=nFrom; i<nFrom + nNum; i++){
                        //选择方法与Java端数据类型有关
                        //int data = ans.getIntByIndex(i);
                        float data = ans.getFloatByIndex(i-nFrom);
                        System.out.println(i+":" + data);
                    }
                }


//
                //----------功能码04--------------------
                // -- tested --
                //1.设置发送指令参数
//    			int nAddressFrom = 2;
//    			int nDataNum = 3;
//	    		ans.setConvertMode(ModbusProtocol.DATATYPE_INT32, ModbusProtocol.CONVMOD_0123_0123,
//	    				ModbusProtocol.DATATYPE_JAVA_INT32);
//
//	    		nError = req.sendReadInputRegister(nDeviceId, nAddressFrom, nDataNum);
//    			if (nError == ModbusProtocol.ERROR_NONE ){
//	    			System.out.println("sendReadInputRegister-参数设置有效");
//    			}else{
//	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
//    			}
//
//    			//2.发送指令
//    			nError = manager.write(nServerListPos, req);
//    			if (nError == ModbusProtocol.ERROR_NONE ){
//	    			System.out.println("sendReadInputRegister-发送有效");
//    			}else{
//	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
//    			}
//
//    			//3.接收数据
//    			nError = manager.read(nServerListPos, ans);
//     			if (nError == ModbusProtocol.ERROR_NONE ){
//	    			System.out.println("sendReadInputRegister-接收有效");
//    			}
//
//    			//4.接收数据成功，则通过该方法读取相应数据
//    			if (nError == ModbusProtocol.ERROR_NONE){
//    				//注：i的值为第几个数据,因此起点为0,而不是字节数的起点也与nAddressFrom不同
//	    			int nDataFrom = 0;
//    				for(int i = nDataFrom; i< nDataNum; i++){
//	    				//选择的数据类型，与setConvertMode方法中设置的Java端数据类型有关
//	    				//int data = ans.getIntByIndex(i);
//	    				float data = ans.getFloatByIndex(i);
//	    				System.out.println(i+":" + data);
//	    			}
//    			}
//


				/*
	    		//----------功能码05--------------------
	    		// -- tested --
	    		//0.初始化设置发送对象和接收对象的参数
	    		req.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
	    		ans.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
	    		ans.setJavaDataType(ModbusProtocol.DATATYPE_JAVA_INT32);

	    		//1.设置发送指令参数
	    		nError = req.sendWriteCoil(nDeviceId, 5 , true);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteCoil-参数设置有效");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//2.发送指令
    			nError = manager.write(nServerListPos, req);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteCoil-发送成功");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//3.接收数据
    			nError = manager.read(nServerListPos, ans);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteCoil-接收成功");
    			}
	    		*/

	    		/*
	    		//----------功能码06--------------------
	    		// -- tested --
    			//1.设置发送指令参数
    			nError = req.sendWriteSingleRegister(nDeviceId, 4, 5);//设置参数
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteSingleRegister-参数设置有效！");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//2.发送指令
    			nError = manager.write(nServerListPos, req);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteSingleRegister-发送成功！");
    			}else{
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));
    			}

    			//3.接收数据
    			nError = manager.read(nServerListPos, ans);
    			if (nError == ModbusProtocol.ERROR_NONE ){
	    			System.out.println("sendWriteSingleRegister-接收成功！");
    			}
    			*/

//
//                //设置发送指令——功能码15
//                //0.初始化设置发送对象和接收对象的参数
//                req.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
//                ans.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
//                ans.setJavaDataType(ModbusProtocol.DATATYPE_JAVA_INT32);
//
//                //1.设置发送指令参数
//                int nCoilAddr = 1;
//                int nCoilNum = 7;
//                int nCoilValue = 0x0106;
//                //????这个地方很奇怪，这几个参数的设置不同可能有些能正确反馈，有些不能
//
//                nError = req.sendWriteMultipleCoils(nDeviceId, nCoilAddr, nCoilNum, nCoilValue);
//                if (nError == ModbusProtocol.ERROR_NONE) {
//                    System.out.println("sendWriteMultipleCoils-参数设置正确！");
//                } else {
//                    System.out.println(ModbusProtocol.getErrorMessage(nError));
//                }
//
//                //2.发送指令
//                nError = manager.write(nServerListPos, req);
//                if (nError == ModbusProtocol.ERROR_NONE) {
//                    System.out.println("sendWriteMultipleCoils--发送成功！");
//                } else {
//                    System.out.println(ModbusProtocol.getErrorMessage(nError));
//                }
//
//                //3.接收数据
//                nError = manager.read(nServerListPos, ans);
//                System.out.println("返回错误码 ： " + nError);
//                if (nError == ModbusProtocol.ERROR_NONE) {
//                    System.out.println("sendWriteMultipleCoils-接收成功！");
//                }


	    		/*
    			//设置发送指令——功能码16
    			// -- tested --
	    		//0.设置系统参数
	    		nError = ans.setConvertMode(ModbusProtocol.DATATYPE_INT32,
	    				ModbusProtocol.CONVMOD_0123_3210,
	    				ModbusProtocol.DATATYPE_JAVA_INT32);

	    		//1.设置指令参数
   				int nRegNum = 5;
    			short nRegValues[] = new short[nRegNum];
    			for(short i=0 ;i<nRegNum; i++){
    				nRegValues[i] = (short)(i * 2);
    			}
				nError = req.sendWriteMultipleRegister(nDeviceId, 2, nRegNum, nRegValues);

				//2.发送指令
				nError = manager.write(nServerListPos, req);
	    		if (nError == ModbusProtocol.ERROR_NONE)
	    			System.out.println("sendWriteMultipleRegister-发送成功！");
	    		else
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));

	    		//3.接收指令
    			nError = manager.read (nServerListPos, ans);
	    		if (nError == ModbusProtocol.ERROR_NONE)
	    			System.out.println("sendWriteMultipleRegister-接收成功！");
	    		else
	    			System.out.println(ModbusProtocol.getErrorMessage(nError));

	    		*/
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
    }
}
