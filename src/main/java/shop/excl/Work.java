package shop.excl;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.TaskMapper;
import shop.domain.CellModel;
import shop.yun.dao.TaskYunMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Work {

    @Autowired
    private ReadExcelService readExcelService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskYunMapper taskYunMapper;


    /**
     * 定时任务work更新配置的初始化状态
     * true：更更新云库
     * false：更新云库
     */
    private static boolean cronConfigSwtich = true;

    /**
     * 云库报警配置更新
     */
    private static List<CellModel> yunUpadeListCellModel = new ArrayList<CellModel>();

    /**
     * 总开关
     */
    private static String mainWitch = "0";

    /**
     * 读取本地数据异常报警开关 >100
     */
    private static String readWitch = "1";

    /**
     * 写入云端数据库异常报警开关
     */
    private static String writeYunSwitch = "1";

    /**
     * 钉钉报警开关
     */
    private static String dingDingAlarmSwtich = "0";

    /**
     * 钉钉报警信息
     */
    private static String dingDingAlarmMsg = "默认信息";

    /**
     * 钉钉@手机号码数组
     */
    private static List<String> listMobies = new ArrayList<String>();

    /**
     * 远程控制点名
     */
    private static String[] sysCellList = {};

    /**
     * 读本地数据异常 计数器
     */
    private static int alarmReadException;

    /**
     * 写云库数据异常 计数器
     */
    private static int alarmWriteYunException;

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
            for (int i = 0; i < sysCellList.length; i++) {
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
        if (alarmReadException > 10 && "1".equals(mainWitch) && "1".equals(readWitch)) {
            System.out.println("Work.sysTOYun, 读取本地数据库异常报警");
        }
        if (alarmWriteYunException > 10 && "1".equals(mainWitch) && "1".equals(writeYunSwitch)) {
            System.out.println("Work.sysTOYun, 写云数据库异常");
        }
    }


    /**
     * 发送消息
     *
     * @param msg
     * @param list
     */
    public static void sendQuantiyAlarmInfo(String msg, List<String> list) {

        if ("0".equals(mainWitch) || "0".equals(dingDingAlarmSwtich)) {
            return;
        }

        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(
                    "https://oapi.dingtalk.com/robot/send?access_token=cfd308e57ae0dac4df80d85cb13d2d8d9324a718db31e53369fcd75349ba2534");
            httppost.addHeader("Content-Type", "application/json; charset=utf-8");
            //String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"测试消息类型？\"}}";
            // 内容
            Map<String, Object> contentMap = new HashMap<String, Object>();
            contentMap.put("content", msg);
            // at
            Map<String, Object> atMap = new HashMap<String, Object>();
            atMap.put("atMobiles", listMobies);
            // 主体
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("msgtype", "text");
            map.put("text", JSONObject.toJSONString(contentMap));
            map.put("at", atMap);
            StringEntity strEnt = new StringEntity(JSONObject.toJSONString(map), "utf-8");
            httppost.setEntity(strEnt);
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(result);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public static void main(String[] args) {
        List<String> se = new ArrayList<String>();
//        se.add("13810653015");
        sendQuantiyAlarmInfo(dingDingAlarmMsg, listMobies);
    }


    /**
     * 远程同步数据work 每小时
     */
    private static int yunUpdateListCellSum = 0;

    /**
     * 告警次数
     */

    int sum = 0;

    public void sysWork() {

        if (!cronConfigSwtich) {
            System.out.println("9999999999999");
            return;
        }

        try {
            yunUpadeListCellModel = taskYunMapper.selectMainSwitch();
            if (yunUpadeListCellModel == null) {
                yunUpdateListCellSum++;
            }
            if (yunUpadeListCellModel != null && yunUpdateListCellSum > 5) {
                yunUpdateListCellSum = 0;
            }
        } catch (Exception e) {
            System.out.println("Work.sysWork2, 更新云库配置表失败");
            yunUpdateListCellSum++;
        }
        if (yunUpdateListCellSum > 5 && yunUpdateListCellSum < 10) {
            sum++;
            String msg = "甜圆有机现场报警：定时更新云库配置数据，网络抖动异常，远程读取失败，AlarmWork.sysWork告警，第" + sum + "次";
            System.out.println("甜圆有机报警：定时更新云库配置数据，网络抖动异常，远程读取失败，AlarmWork.sysWork告警第" + sum + "次");
            sendQuantiyAlarmInfo(msg, getListMobies());
        }
        if (yunUpdateListCellSum > 15) {
            String msg = "甜圆有机现场报警：定时更新云库配置数据，网络抖动异常，经过重试后无效，配置置为本地初始化状态，已断开与云库通信";
            cronConfigSwtich = false;
            System.out.println("甜圆有机报警：定时更新云库配置数据，网络抖动异常，经过重试后无效，配置置为本地初始化状态，已断开与云库通信");
            sendQuantiyAlarmInfo(msg, getListMobies());
        }
        System.out.println("yunUpdateListCellSum" + yunUpdateListCellSum);
        System.out.println("3333333333");
    }

    /**
     * 远程同步数据work1 每10分组一组
     */
    public void sysWork1() {

    }


    /**
     * 远程同步数据work3 每天
     */
    public void sysWork3() {

    }


    /**
     * 同步至云，初始化配置
     */
    private void loadSysCell() {
        if (sysCellList.length == 0) {
            setSysCellList(readExcelService.getSysCellList());
        }
    }

    public static boolean isCronConfig() {
        return cronConfigSwtich;
    }

    public static void setCronConfig(boolean cronConfig) {
        Work.cronConfigSwtich = cronConfig;
    }

    public static String getMainWitch() {
        return mainWitch;
    }

    public static void setMainWitch(String mainWitch) {
        Work.mainWitch = mainWitch;
    }

    public static String getReadWitch() {
        return readWitch;
    }

    public static void setReadWitch(String readWitch) {
        Work.readWitch = readWitch;
    }

    public static String getWriteYunSwitch() {
        return writeYunSwitch;
    }

    public static void setWriteYunSwitch(String writeYunSwitch) {
        Work.writeYunSwitch = writeYunSwitch;
    }

    public static String getDingDingAlarmSwtich() {
        return dingDingAlarmSwtich;
    }

    public static void setDingDingAlarmSwtich(String dingDingAlarmSwtich) {
        Work.dingDingAlarmSwtich = dingDingAlarmSwtich;
    }

    public static String getDingDingAlarmMsg() {
        return dingDingAlarmMsg;
    }

    public static void setDingDingAlarmMsg(String dingDingAlarmMsg) {
        Work.dingDingAlarmMsg = dingDingAlarmMsg;
    }

    public static List<String> getListMobies() {
        return listMobies;
    }

    public static void setListMobies(List<String> listMobies) {
        Work.listMobies = listMobies;
    }

    public static int getAlarmReadException() {
        return alarmReadException;
    }

    public static void setAlarmReadException(int alarmReadException) {
        Work.alarmReadException = alarmReadException;
    }

    public static int getAlarmWriteYunException() {
        return alarmWriteYunException;
    }

    public static void setAlarmWriteYunException(int alarmWriteYunException) {
        Work.alarmWriteYunException = alarmWriteYunException;
    }

    public static int getYunUpdateListCellSum() {
        return yunUpdateListCellSum;
    }

    public static void setYunUpdateListCellSum(int yunUpdateListCellSum) {
        Work.yunUpdateListCellSum = yunUpdateListCellSum;
    }

    public ReadExcelService getReadExcelService() {
        return readExcelService;
    }

    public void setReadExcelService(ReadExcelService readExcelService) {
        this.readExcelService = readExcelService;
    }

    public static String[] getSysCellList() {
        return sysCellList;
    }

    public static void setSysCellList(String[] sysCellList) {
        Work.sysCellList = sysCellList;
    }

}
