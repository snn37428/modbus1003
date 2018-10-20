package shop.serviceImpl;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.SpotMapper;
import shop.pojo.Spot;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明
 *
 * @author: songningning
 * @date: created in 2018/3/18 19:25
 * @modified: by
 */
public class Test {
//    @Autowired
//    private SpotMapper orderPOMapper;
    private String ip = "192.168.1.111";
    private int port = 502;
//单个信息写入
    public void test1 () {
        Spot se = new Spot();
        se.setPlcId(25);
        se.setSpotName("gagse");
        se.setTableIndex("int_0");
//        orderPOMapper.insert(se);
        System.out.println("8");
    }
//多个信息入库
    public void test2(){
        try {
            List<Spot> l = new ArrayList<Spot>();
            Spot se = new Spot();
            se.setPlcId(25);
            se.setSpotName("gag");
            se.setTableIndex("int_0");
            l.add(se);
            Spot s2 = new Spot();
            s2.setPlcId(27);
            s2.setSpotName("gaerww");
            s2.setTableIndex("int_0");
            l.add(s2);
//            orderPOMapper.insertSpotInt(l);
            System.out.println("test===========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //readInputRegister 3000 地址+1 例如：30004 传入 3
    public void test3() {
        int dd =4;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            TCPMasterConnection con = new TCPMasterConnection(addr);
            //Modbus.DEFAULT_PORT;
            con.setPort(port);
            con.connect();
            //这里重点说明下，这个地址和数量一定要对应起来
            ReadInputRegistersRequest req = new ReadInputRegistersRequest(dd, 1);
            //这个SlaveId一定要正确
            req.setUnitID(1);
            ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
            trans.setRequest(req);
            trans.execute();
            ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
            int data = res.getRegisterValue(0);
            System.out.println("--------------------点地址："+ dd + "   |读出结果：" + data);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //readInputRegister 4000
    public void test4() {
        int dd = 4;
        int data = 2002;
        int ks =2002;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            TCPMasterConnection con = new TCPMasterConnection(addr);
            con.setPort(port);
            con.connect();
            ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(dd, 1);
            req.setUnitID(1);
            ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
            trans.setRequest(req);
            trans.execute();
            ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
//            int qq = res.getRegisters()[0].toUnsignedShort();
//            ks = res.getRegisterValue(1);
            data = res.getRegisterValue(0);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------点地址：ks" + "   |读出结果：" + ks);
        System.out.println("--------------------点地址："+ dd + "   |读出结果：" + data);
    }
    //readInputRegister 1000
    public void test5(){
        int data = 0;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            TCPMasterConnection con = new TCPMasterConnection(addr);
            System.out.println("");
            con.setPort(port);
            con.connect();
            ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(3, 1);
            req.setUnitID(1);
            ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
            trans.setRequest(req);
            trans.execute();
            ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) trans.getResponse();
            if(res.getDiscretes().getBit(0)){
                data = 1;
            }
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--------------------data" + data);
    }
    //批量操作3000
    public void testList1() {
        List<Spot> ListInt = LoadConfigService.getSpotListInt();
        if (CollectionUtils.isEmpty(ListInt)) {
            System.out.println("程序初始化开始，加载Int配置.....长度为零");
        }
        try {
            InetAddress addr = InetAddress.getByName(LoadConfigService.getIp());
            TCPMasterConnection con = new TCPMasterConnection(addr);
            con.setPort(Integer.parseInt(LoadConfigService.getPort()));
            con.connect();
            System.out.println("");

            for(Spot spt : ListInt) {

                ReadInputRegistersRequest req = new ReadInputRegistersRequest(spt.getModbusAddress(), 1);
                req.setUnitID(1);
                ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
                trans.setRequest(req);
                trans.execute();
                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
                int data = res.getRegisterValue(0);
                spt.setValue(String.valueOf(0));
                System.out.println("==============data：" + data);
            }
            con.close();
        } catch (Exception e) {
            System.out.println("=====readInputRegister，3000地址，批量读取PLC数值异常%%%%%");
        }
    }

    //批量操作1000
    public void testList2() {
        List<Spot> ListB = LoadConfigService.getSpotListB();
        if (CollectionUtils.isEmpty(ListB)) {
            System.out.println("程序初始化开始，加载Int配置.....长度为零");
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            TCPMasterConnection con = new TCPMasterConnection(addr);
            System.out.println("");
            con.setPort(port);
            con.connect();
            for(Spot spt : ListB) {
                int data = 0;
                ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(spt.getModbusAddress(), 1);
                req.setUnitID(1);
                ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
                trans.setRequest(req);
                trans.execute();
                ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) trans.getResponse();
                if(res.getDiscretes().getBit(0)){
                    data = 1;
                    spt.setValue(String.valueOf(1));
                } else {
                    spt.setValue(String.valueOf(0));
                }
                System.out.println("=============================" +data);
                System.out.println("=============================" +spt.getSpotDesc());
            }
            con.close();
        } catch (Exception e) {
            System.out.println("=====readInputRegister，1000地址，批量读取PLC数值异常%%%%%");
        }

        try {
//            orderPOMapper.insertSpotBool(ListB);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        Test ts = new Test();
        ts.test4();
    }

}
