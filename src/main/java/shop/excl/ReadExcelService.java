package shop.excl;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import shop.domain.AddrSpotModel;
import shop.domain.CellModel;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ReadExcelService {

    private static Properties globalConfig;

    private String ip;
    private String port;
    private String deviceId;

    private static List<CellModel> listCellModel = new ArrayList<CellModel>();

    /**
     * 功能码 3
     */
    private static List<CellModel> listModel_3 = new ArrayList<CellModel>();

    /**
     * 功能码 4
     */
    private static List<CellModel> listModel_4 = new ArrayList<CellModel>();

    /**
     * 功能码 2
     */
    private static List<CellModel> listModel_2 = new ArrayList<CellModel>();

    /**
     * 功能码 1
     */
    private static List<CellModel> listModel_1 = new ArrayList<CellModel>();

    /**
     * 任务1
     */
    private List<AddrSpotModel> taskList1;

    /**
     * 任务2
     */
    private List<AddrSpotModel> taskList4;

    /**
     * 任务3
     */
    private List<AddrSpotModel> taskList3;

    private int initFlag = 0;

    /**
     * 容器初始化方法（加载Excel配置文件）
     */
    public void init() {
        try {

            readExcel();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            if (CollectionUtils.isEmpty(listCellModel)) {
                System.out.println("--初始化--读取Excel文件，失败!!!");
                throw new RuntimeException();
            } else {
                System.out.println("--初始化--读取Excel文件，成功!");
            }
            loadModel3();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            loadModel4();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            loadModel1();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            listCellModel.clear();
            System.out.println("--主内存数据清除!");
            System.out.println("");
            handldModel();
        } catch (Exception e) {
            System.out.println("--初始化-- 异常 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + e);
        }
        loadIp();
    }

    /**
     * 组装工作任务
     */
    private void handldModel() {
        try {
            taskList1 = splitByGroupCode(listModel_1);
            System.out.println("--TaskList1，加载完毕！，长度为：" + taskList1.size());
            taskList4 = splitByGroupCode(listModel_4);
            System.out.println("--TaskList4，加载完毕！，长度为：" + taskList4.size());
            taskList3 = splitByGroupCode(listModel_3);
            System.out.println("--TaskList３，加载完毕！，长度为：" + taskList3.size());
//            System.out.println("-" + JSONObject.toJSONString(taskList4));
            this.setInitFlag(1);
        } catch (Exception e) {
            System.out.println("--初始化-- handldModel，异常!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + e);
        }
    }

    /**
     * 读取配置文件Excel
     */
    public static void readExcel() {
        System.out.println("--初始化--读取Excel文件，开始!");
        String excelPath = globalConfig.get("path").toString();

        try {
            //String encoding = "GBK";
            File excel = new File(excelPath);
            if (excel.isFile() && excel.exists()) {   //判断文件是否存在
                String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
                Workbook wb;
                //根据文件后缀（xls/xlsx）进行判断
                if ("xls".equals(split[1])) {
                    FileInputStream fis = new FileInputStream(excel);   //文件流对象
                    wb = new HSSFWorkbook(fis);
                } else if ("xlsx".equals(split[1])) {
                    wb = new XSSFWorkbook(excel);
                } else {
                    System.out.println("--读取Excel文件，文件类型错误!");
                    return;
                }
                //开始解析
                Sheet sheet = wb.getSheetAt(0);     //读取sheet 0

                int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
                int lastRowIndex = sheet.getLastRowNum();
                CellModel cellModel = null;
                for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
                    Row row = sheet.getRow(rIndex);
                    if (row != null) {
                        cellModel = new CellModel();
                        cellModel.setId(getCellValue(row.getCell(0)));
                        cellModel.setName(getCellValue(row.getCell(1)));
                        cellModel.setType(getCellValue(row.getCell(2)));
                        cellModel.setDesc(getCellValue(row.getCell(3)));
                        cellModel.setModel(getCellValue(row.getCell(4)));
                        cellModel.setPlcAdd(getCellValue(row.getCell(5)));
                        cellModel.setCyc(getCellValue(row.getCell(6)));
                        cellModel.setGroupCode(getCellValue(row.getCell(7)));
                        listCellModel.add(cellModel);
                    }
                }
            } else {
                System.out.println("--读取Excel文件，找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("--读取Excel文件，异常" + e);
        }
    }

    /**
     * 根据groupCode分割 功能Map
     *
     * @param listModel
     */
    private List<AddrSpotModel> splitByGroupCode(List<CellModel> listModel) {

        List<AddrSpotModel> listAddrSpotModel = new ArrayList<AddrSpotModel>();

        if (CollectionUtils.isEmpty(listModel)) {
            System.out.println("--listModel, is null");
            return listAddrSpotModel;
        }

        try {
            int j = 0;
            for (int i = 0; i < listModel.size() - 1; i++) {

                if (listModel.get(i) == null || StringUtils.isEmpty(listModel.get(i).getModel())) {
                    continue;
                }
                List<CellModel> newList;
                if (!listModel.get(i).getGroupCode().equals(listModel.get(i + 1).getGroupCode())) {
                    AddrSpotModel addrSpotModel = new AddrSpotModel();
                    // 设置Id
                    addrSpotModel.setModelCode(listModel.get(i).getId());
                    // 设置起始点
                    addrSpotModel.setnFrom(Integer.parseInt(this.formatStringToInt(listModel.get(i - j).getPlcAdd())));
                    // 设置组 功能码
                    addrSpotModel.setModelCode(this.formatStringToInt(listModel.get(i - j).getModel()));
                    // 设置类型 int
                    addrSpotModel.setType(listModel.get(i - j).getType());
                    // 设置周期
                    addrSpotModel.setCyc(listModel.get(i - j).getCyc());
                    // 设置结束点
                    addrSpotModel.setnEnd(this.formatStringToInt(listModel.get(i).getPlcAdd()));
                    newList = listModel.subList(i - j, i + 1);
                    // 设置步长
                    addrSpotModel.setAddNum(newList.size());
                    // 分组号
                    addrSpotModel.setGroupCode(listModel.get(i).getGroupCode());
                    List<Integer> listId = new ArrayList<Integer>();
                    List<Integer> listSpot = new ArrayList<Integer>();
                    List<String> listSpotDesc = new ArrayList<String>();
                    List<String> listSpotDescCN = new ArrayList<String>();
                    for (CellModel cellModel : newList) {
                        listId.add(Integer.parseInt(this.formatStringToInt(cellModel.getId())));
                        listSpot.add(Integer.parseInt(this.formatStringToInt(cellModel.getPlcAdd())));
                        listSpotDesc.add(cellModel.getName());
                        listSpotDescCN.add(cellModel.getDesc());

                    }
                    addrSpotModel.setId(listId);
                    addrSpotModel.setListSpot(listSpot);
                    addrSpotModel.setSpotDesc(listSpotDesc);
                    addrSpotModel.setSpotDescCN(listSpotDescCN);
                    listAddrSpotModel.add(addrSpotModel);
//                    newList.clear();
                    j = 0;
                    continue;
                }
                j++;
            }
        } catch (Exception e) {
            System.out.println("--根据groupCode分割 功能Map, 异常！！！" + e);
        }
        return listAddrSpotModel;
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

    /**
     * 转换公式的值
     *
     * @param cell
     * @return
     */
    public static String getCellValue(Cell cell) {
        int cellType = cell.getCellType();
        String cellValue = "";
        switch (cellType) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;

            case HSSFCell.CELL_TYPE_FORMULA:
                try {
                    cellValue = cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }
                break;

            default:
                cellValue = cell.getStringCellValue();
        }

        return cellValue.trim();
    }


    /**
     * 加载功能码list 3
     */
    private static void loadModel3() {
        System.out.println("--功能码3，加载开始!");
        for (CellModel cellModel : listCellModel) {
            if (cellModel == null) {
                continue;
            }
            if (3.0 == (Double.valueOf(cellModel.getModel()))) {
                listModel_3.add(cellModel);
            }
        }
        if (CollectionUtils.isEmpty(listModel_3)) {
            System.out.println("--功能码3，加载失败!!!");
        } else {
            System.out.println("--功能码3，加载成功!" + "数据长度为:" + listModel_3.size());
            System.out.println();
        }
    }

    /**
     * 加载功能码list 4
     */
    private static void loadModel4() {
        System.out.println("--功能码4，加载开始!");
        for (CellModel cellModel : listCellModel) {
            if (cellModel == null) {
                continue;
            }
            if (4.0 == (Double.valueOf(cellModel.getModel()))) {
                listModel_4.add(cellModel);
            }
        }
        if (CollectionUtils.isEmpty(listModel_4)) {
            System.out.println("--功能码4，加载失败!!!");
        } else {
            System.out.println("--功能码4，加载成功!" + "数据长度为:" + listModel_4.size());
            System.out.println();
        }
    }

    /**
     * 加载功能码list 1
     */
    private static void loadModel1() {
        System.out.println("--功能码1，加载开始!");
        for (CellModel cellModel : listCellModel) {
            if (cellModel == null) {
                continue;
            }
            if (1.0 == (Double.valueOf(cellModel.getModel()))) {
                listModel_1.add(cellModel);
            }
        }
        if (CollectionUtils.isEmpty(listModel_1)) {
            System.out.println("--功能码1，数据长度为 0 !!!");
        } else {
            System.out.println("--功能码1，加载成功!" + "数据长度为:" + listModel_1.size());
            System.out.println();
        }
    }

    /**
     * 加载IP
     */
    private void loadIp() {
        if (StringUtils.isEmpty(ip)) {
            setIp(globalConfig.get("ip").toString());
            setPort(globalConfig.get("port").toString());
            setDeviceId(globalConfig.get("deviceId").toString());
        }

    }

    public List<AddrSpotModel> getTaskList1() {
        return taskList1;
    }

    public void setTaskList1(List<AddrSpotModel> taskList1) {
        this.taskList1 = taskList1;
    }

    public List<AddrSpotModel> getTaskList4() {
        return taskList4;
    }

    public void setTaskList4(List<AddrSpotModel> taskList4) {
        this.taskList4 = taskList4;
    }

    public List<AddrSpotModel> getTaskList3() {
        return taskList3;
    }

    public void setTaskList3(List<AddrSpotModel> taskList3) {
        this.taskList3 = taskList3;
    }

    public int getInitFlag() {
        return initFlag;
    }

    public void setInitFlag(int initFlag) {
        this.initFlag = initFlag;
    }

    public Properties getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(Properties globalConfig) {
        this.globalConfig = globalConfig;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}


