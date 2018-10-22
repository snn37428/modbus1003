package shop.excl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.TaskMapper;
import shop.domain.CellModel;
import shop.yun.dao.TaskYunMapper;

import java.util.ArrayList;
import java.util.List;

public class Work {

    @Autowired
    private ReadExcelService readExcelService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskYunMapper taskYunMapper;

    /**
     * 本地数据读失败 报警
     */
    private static int alarmReadException = 0;

    /**
     * 写云数据失败 报警
     */
    private static int alarmWriteYunException = 0;

    private static String[] sysCellList = {};


    public void dataTOYun() {
        List<CellModel> rs = null;
        try {
            rs = taskMapper.listCellModel();
        } catch (Exception e) {
            System.out.println("Work.dataTOYun，读取本地异常！！！" + e);
        }
        if (!CollectionUtils.isEmpty(rs)) {
            System.out.println("Work.dataTOYun，读取本地数据为空！！！");
        }
        try {
            taskYunMapper.insertList(rs);
            System.out.println("Work.dataTOYun，写入--云数据库成功");
        } catch (Exception e) {
            System.out.println("Work.dataTOYun，写入数据库异常！！！" + e);
        }
    }

    public void sysTOYun() {
        loadSysCell();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("Work.sysTOYun，InterruptedException" + e);
        }
        List<CellModel> listCellModel = new ArrayList<CellModel>();
        if (sysCellList.length == 0) {
            System.out.println("Work.sysTOYun，同步至云，初始化配置为空！");
        }
        try {
            for (int i = 0; i < sysCellList.length; i++){
                CellModel cellModel = new CellModel();
                cellModel.setModbusAddr(sysCellList[i]);
                List<CellModel> rs = taskMapper.selectById(cellModel);
                if (CollectionUtils.isEmpty(rs)) {
                    System.out.println("Work.sysTOYun, 读取数据失败！！！");
                }
                listCellModel.add(rs.get(0));
                Thread.sleep(50);
                System.out.println("=======" + JSONObject.toJSONString(rs.get(0)));
            }
        } catch (Exception e) {
            System.out.println("Work.sysTOYun, 读取数据异常！！！" + e);
            alarmReadException++;
        }
        if (CollectionUtils.isEmpty(listCellModel)) {
            alarmReadException++;
        }
        try {
            int rs = taskYunMapper.insertList(listCellModel);
            if (rs > 0) {
                System.out.println("Work.sysTOYun, 写云数据库成功");
            }
            alarmWriteYunException++;
        } catch (Exception e) {
            System.out.println("Work.sysTOYun, 写云数据库异常" + e);
            alarmWriteYunException++;
        }
        if (alarmReadException > 100 || alarmWriteYunException > 100) {
            System.out.println("Work.sysTOYun, 读取本地数据库异常报警");
        }
    }

    /**
     * 同步至云，初始化配置
     */
    private void loadSysCell() {
        if (sysCellList.length == 0) {
            setSysCellList(readExcelService.getSysCellList());
        }
    }

    public ReadExcelService getReadExcelService() {
        return readExcelService;
    }

    public void setReadExcelService(ReadExcelService readExcelService) {
        this.readExcelService = readExcelService;
    }

    public TaskMapper getTaskMapper() {
        return taskMapper;
    }

    public void setTaskMapper(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public TaskYunMapper getTaskYunMapper() {
        return taskYunMapper;
    }

    public void setTaskYunMapper(TaskYunMapper taskYunMapper) {
        this.taskYunMapper = taskYunMapper;
    }

    public static String[] getSysCellList() {
        return sysCellList;
    }

    public static void setSysCellList(String[] sysCellList) {
        Work.sysCellList = sysCellList;
    }
}
