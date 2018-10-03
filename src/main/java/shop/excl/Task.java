package shop.excl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.TaskMapper;
import shop.devs.TaskDevice;
import shop.domain.AddrSpotModel;
import shop.domain.CellModel;

import java.util.ArrayList;
import java.util.List;

public class Task {


    @Autowired
    private ReadExcelService readExcelService;

    @Autowired
    private TaskDevice taskDevices;

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 功能码4
     */
    private static final int model4 = 4;

    /**
     * 功能码3
     */
    private static final int model3 = 3;

    /**
     * 功能码1
     */
    private static final int model1 = 1;

    /**
     * 任务4
     */
    private static List<AddrSpotModel> taskList4;

    /**
     * 任务3
     */
    private static List<AddrSpotModel> taskList3;

    /**
     * 任务1
     */
    private static List<AddrSpotModel> taskList1;


    /**
     * 任务4
     */
    public void readTaskList4() {

        if (taskList4 == null) {
            taskList4 = readExcelService.getTaskList4();
            System.out.println("--Task,4,首次加载,完成");
        }

        if (CollectionUtils.isEmpty(taskList4)) {
            System.out.println("Task.taskList4，任务内存为空！！！");
        }
        for (AddrSpotModel addrSpotModel : taskList4) {
            if (addrSpotModel == null) {
                System.out.println("PLC 读取数据时，解析taskList4，对象存在空值!");
                continue;
            }
            if (addrSpotModel.getnFrom() == 0 || addrSpotModel.getAddNum() == 0) {
                System.out.println("PLC 读取数据时，解析taskList4，起始位，或步长为空！ object :" + JSONObject.toJSONString(addrSpotModel));
                continue;
            }
            int nfrom = addrSpotModel.getnFrom();
            int nNum = addrSpotModel.getAddNum();
            List<String> rs = taskDevices.readDevicesTask4(nfrom, nNum);
//                System.out.println("rs" + rs);
            if (CollectionUtils.isEmpty(rs) || rs.size() != addrSpotModel.getAddNum()) {
                System.out.println("Task.readTaskList4 单组读取为空！,分组号:" + addrSpotModel.getGroupCode() + "起始位：" + nfrom);
            }
            addrSpotModel.setPlcValue(rs);

            buildCellModel(addrSpotModel, model4);
        }

    }

    /**
     * 任务3
     */
    public void readTaskList3() {
        if (taskList3 == null) {
            taskList3 = readExcelService.getTaskList3();
            System.out.println("--Task,3,首次加载,完成");
        }
//        test();
        if (CollectionUtils.isEmpty(taskList3)) {
            System.out.println("Task.taskList3，任务内存为空！！！");
        }
        if (!CollectionUtils.isEmpty(taskList3)) {
            for (AddrSpotModel addrSpotModel : taskList3) {
                if (addrSpotModel == null) {
                    System.out.println("PLC 读取数据时，解析taskList3，对象存在空值!");
                    continue;
                }
                if (addrSpotModel.getnFrom() == 0 || addrSpotModel.getAddNum() == 0) {
                    System.out.println("PLC 读取数据时，解析taskList3，起始位，或步长为空！ object :" + JSONObject.toJSONString(addrSpotModel));
                    continue;
                }
                int nfrom = addrSpotModel.getnFrom();
                int nNum = addrSpotModel.getAddNum();
                List<String> rs = taskDevices.readDevicesTask3(nfrom, nNum);
                //System.out.println("rs" + rs);
                if (CollectionUtils.isEmpty(rs) || rs.size() != addrSpotModel.getAddNum()) {
                    System.out.println("Task.readTaskList3 单组读取为空！,分组号:" + addrSpotModel.getGroupCode() + "起始位：" + nfrom);
                }
                addrSpotModel.setPlcValue(rs);

                buildCellModel(addrSpotModel, model3);
            }
        }
    }

    /**
     * 任务1
     */
    public void readTaskList1() {
        if (taskList1 == null) {
            taskList1 = readExcelService.getTaskList1();
            System.out.println("--Task,3,首次加载,完成");
        }
//      test();
        if (CollectionUtils.isEmpty(taskList1)) {
            System.out.println("Task.taskList1，任务内存为空！！！");
        }
        if (!CollectionUtils.isEmpty(taskList1)) {
            for (AddrSpotModel addrSpotModel : taskList1) {
                if (addrSpotModel == null) {
                    System.out.println("PLC 读取数据时，解析taskList1，对象存在空值!");
                    continue;
                }
                if (addrSpotModel.getnFrom() == 0 || addrSpotModel.getAddNum() == 0) {
                    System.out.println("PLC 读取数据时，解析taskList1，起始位，或步长为空！ object :" + JSONObject.toJSONString(addrSpotModel));
                    continue;
                }
                int nfrom = addrSpotModel.getnFrom();
                int nNum = addrSpotModel.getAddNum();
                List<String> rs = taskDevices.readDevicesTask1(nfrom, nNum);
                // System.out.println("rs" + rs);
                if (CollectionUtils.isEmpty(rs) || rs.size() != addrSpotModel.getAddNum()) {
                    System.out.println("Task.readTaskList1 单组读取为空！,分组号:" + addrSpotModel.getGroupCode() + "起始位：" + nfrom);
                }
                addrSpotModel.setPlcValue(rs);
                buildCellModel(addrSpotModel, model1);
            }
        }
    }

    /**
     * 组建入库模型cellModel
     */
    private void buildCellModel(AddrSpotModel addrSpotModel, int model) {

        if (addrSpotModel == null) {
            System.out.println("组建入库模型cellModel addrSpotModel is null");
        }

        List<String> listSpot = addrSpotModel.getPlcValue();

        if (CollectionUtils.isEmpty(listSpot)) {
            System.out.println("组建入库模型cellModel listSpot is null");
        }

        List<CellModel> listCellModel = new ArrayList<CellModel>();

        try {
            for (int i = 0; i < listSpot.size(); i++) {
                CellModel cellModel = new CellModel();
                cellModel.setId(String.valueOf(addrSpotModel.getId().get(i)));
                cellModel.setModel(addrSpotModel.getModelCode());
                cellModel.setCyc(addrSpotModel.getCyc());
                cellModel.setGroupCode(addrSpotModel.getGroupCode());
                cellModel.setType(addrSpotModel.getType());
                cellModel.setDesc(addrSpotModel.getSpotDesc().get(i));
                cellModel.setModbusAddr(String.valueOf(addrSpotModel.getListSpot().get(i)));
                cellModel.setValue(String.valueOf(addrSpotModel.getPlcValue().get(i)));
                cellModel.setName(addrSpotModel.getSpotDescCN().get(i));
                listCellModel.add(cellModel);
            }
        } catch (Exception e) {
            System.out.println("组建入库模型cellModel Exception " + e);
        }
        try {
            taskMapper.insertList(listCellModel);
            System.out.println("功能码，" + model + "，写入数据成功！！！");
        } catch (Exception e) {
            System.out.println("插入数据，异常：" + e);
        }
    }


    /**
     * 测试
     */
    private void test() {

        List<Integer> listPot = new ArrayList<Integer>();
        listPot.add(0);
        listPot.add(4);
        listPot.add(6);

        List<String> listPotName = new ArrayList<String>();
        listPotName.add("iWTm0202");
        listPotName.add("iWTm0203");
        listPotName.add("iWTm0204");

        List<String> listPotDesc = new ArrayList<String>();
        listPotDesc.add("浇水时间0202");
        listPotDesc.add("浇水时间0203");
        listPotDesc.add("浇水时间0203");


        AddrSpotModel addrSpotModel = new AddrSpotModel();
        addrSpotModel.setListSpot(listPot);
        addrSpotModel.setnFrom(2);
        addrSpotModel.setAddNum(3);
        addrSpotModel.setSpotDescCN(listPotDesc);
        addrSpotModel.setSpotDesc(listPotName);


        List<String> listPotName2 = new ArrayList<String>();
        listPotName2.add("iWTm0202");
        listPotName2.add("iWTm0203");
        listPotName2.add("iWTm0204");

        List<String> listPotDesc2 = new ArrayList<String>();
        listPotDesc2.add("浇水时间0202");
        listPotDesc2.add("浇水时间0203");
        listPotDesc2.add("浇水时间0203");


        List<Integer> listPot2 = new ArrayList<Integer>();
        listPot2.add(8);
        listPot2.add(10);
        listPot2.add(12);

        AddrSpotModel addrSpotModel2 = new AddrSpotModel();
        addrSpotModel2.setListSpot(listPot2);
        addrSpotModel2.setnFrom(8);
        addrSpotModel2.setAddNum(3);
        addrSpotModel2.setSpotDescCN(listPotDesc2);
        addrSpotModel2.setSpotDesc(listPotName2);

        List<AddrSpotModel> taskListTest = new ArrayList<AddrSpotModel>();

        taskListTest.add(addrSpotModel);
        taskListTest.add(addrSpotModel2);

        taskList1.clear();
        taskList1 = taskListTest;

    }
}
