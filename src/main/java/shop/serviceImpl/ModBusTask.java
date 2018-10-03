package shop.serviceImpl;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.SpotMapper;
import shop.pojo.Spot;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author: songningning
 * @date: created in 2018/3/18 21:19
 * @modified: by
 */
public class ModBusTask {

    private Map switchMap;
    private Map writeDataMap;
    private Map tableIndexMap;

    @Autowired
    private SpotMapper orderPOMapper;

    //readInputRegister 3000 地址+1 例如：30004 传入 3
    public void readInputRegister3(){

        if ("0".equals(switchMap.get("inputRegister3"))) {
            System.out.println("=====readInputRegister3，3000地址，任务关闭");
        }
        List<Spot> ListInt = LoadConfigService.getSpotListInt();
        if (CollectionUtils.isEmpty(LoadConfigService.getSpotListInt())) {
            System.out.println("程序初始化开始，加载Int配置.....长度为零");
        }
        try {
            InetAddress addr = InetAddress.getByName(LoadConfigService.getIp());
            TCPMasterConnection con = new TCPMasterConnection(addr);
            con.setPort(Integer.parseInt(LoadConfigService.getPort()));
            con.connect();
            for(Spot spt : ListInt) {
                ReadInputRegistersRequest req = new ReadInputRegistersRequest(Integer.valueOf(spt.getModbusAddress()), 1);
                req.setUnitID(1);
                ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
                trans.setRequest(req);
                trans.execute();
                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
                int data = res.getRegisterValue(0);
                spt.setValue(String.valueOf(0));
            }
            con.close();
        } catch (Exception e) {
            System.out.println("=====readInputRegister，3000地址，批量读取PLC数值异常**********************" + e);
        }

        if ("0".equals(writeDataMap.get("inputRegister3"))) {
            System.out.println("=====readInputRegister，3000，禁止写库");
        }

        try {
            orderPOMapper.insertSpotInt(ListInt);
            System.out.println("=====readInputRegister，3000地址，读取数据写入数据成功");
        } catch (Exception e) {
            System.out.println("=====readInputRegister，3000地址，批量写库异常**********************" + e);
        }

    }
    //readInputRegister 4000 地址+1 例如：30004 传入 3
    public void readInputRegister4() {

        if ("0".equals(switchMap.get("inputRegister4"))) {
            System.out.println("=====readInputRegister，4000地址，任务关闭");
        }
        List<Spot> ListReal = LoadConfigService.getSpotListReal();
        if (CollectionUtils.isEmpty(LoadConfigService.getSpotListInt())) {
            System.out.println("程序初始化开始，加载Int配置.....长度为零");
        }
        try {
            InetAddress addr = InetAddress.getByName(LoadConfigService.getIp());
            TCPMasterConnection con = new TCPMasterConnection(addr);
            con.setPort(Integer.parseInt(LoadConfigService.getPort()));
            con.connect();
            for(Spot spt : ListReal) {
                ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(Integer.valueOf(spt.getModbusAddress()), 1);
                req.setUnitID(1);
                ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
                trans.setRequest(req);
                trans.execute();
                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
                int data = res.getRegisterValue(0);
                spt.setValue(String.valueOf(0));
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("0".equals(writeDataMap.get("inputRegister4"))) {
            System.out.println("=====readInputRegister，4000，禁止写库");
        }
        try {
            orderPOMapper.insertSpotInt(ListReal);
        } catch (Exception e) {
            System.out.println("=====readInputRegister，4000地址，批量写库异常**********************");
        }
        System.out.println("=====readInputRegister，4000地址，读取数据写入数据成功");
    }

    //readInputRegister 1000 地址+1 例如：30004 传入 3
    public void readInputRegister1() {

        if ("0".equals(switchMap.get("inputRegister1"))) {
            System.out.println("=====readInputRegister，1000地址，任务关闭");
        }
        List<Spot> ListB = LoadConfigService.getSpotListB();
        if (CollectionUtils.isEmpty(ListB)) {
            System.out.println("程序初始化开始，加载bool配置.....长度为零");
        }
        try {
            InetAddress addr = InetAddress.getByName(LoadConfigService.getIp());
            TCPMasterConnection con = new TCPMasterConnection(addr);
            System.out.println("");
            con.setPort(Integer.parseInt(LoadConfigService.getPort()));
            con.connect();
            System.out.println("");
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
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("0".equals(writeDataMap.get("inputRegister1"))) {
            System.out.println("1000，禁止写库");
        }
        try {
            orderPOMapper.insertSpotBool(ListB);
        } catch (Exception e) {
            System.out.println("1000地址，批量写库异常**********************"+e);
        }
        System.out.println("1000地址，读取数据写入数据成功");
    }


    public Map getSwitchMap() {
        return switchMap;
    }

    public void setSwitchMap(Map switchMap) {
        this.switchMap = switchMap;
    }

    public Map getWriteDataMap() {
        return writeDataMap;
    }

    public void setWriteDataMap(Map writeDataMap) {
        this.writeDataMap = writeDataMap;
    }

    public Map getTableIndexMap() {
        return tableIndexMap;
    }

    public void setTableIndexMap(Map tableIndexMap) {
        this.tableIndexMap = tableIndexMap;
    }
}
