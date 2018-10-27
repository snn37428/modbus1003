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
import org.springframework.util.StringUtils;
import shop.dao.TaskMapper;
import shop.domain.AlarmModel;
import shop.domain.CellModel;
import shop.yun.dao.TaskYunMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private static boolean cronReadYUNConfigSwitch = true;

    /**
     * 云库报警配置更新
     */
    private static List<AlarmModel> yunUpdateListCellModel = new ArrayList<AlarmModel>();

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
    private static String dingDingAlarmSwtich = "1";

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
    private static int sysTOYunError = 0;

    /**
     * 写云异常总次数
     */
    private static int sysTOYUNException = 1;

    /**
     * 本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云、本地取数据到云
     */
    public void sysTOYun() {
        loadSysCell();
        // 错误计数器
        int errorSum = 0;
        try {
            // 错开modbus部分写数据库时间、错来五分钟防止锁库
            Thread.sleep(50);
        } catch (InterruptedException e) {
            System.out.println("Work.sysTOYun，InterruptedException" + e);
        }
        List<CellModel> listCellModel = new ArrayList<CellModel>();
        if (sysCellList.length == 0) {
            System.out.println("Work.sysTOYun，同步至云，初始化配置为空！");
        }
        // 根据配置点读取本地数据
        try {
            System.out.println("(((((((((((((原始配置" + JSONObject.toJSONString(sysCellList));
            for (int i = 0; i < sysCellList.length; i++) {
                CellModel cellModel = new CellModel();
                cellModel.setId(sysCellList[i]);
                Thread.sleep(30);
                List<CellModel> rs = taskMapper.selectById(cellModel);
                if (CollectionUtils.isEmpty(rs)) {
                    System.out.println("Work.sysTOYun，本地数据库读出来的数据少于，配置点的个数。缺失configId： " + (sysCellList[i]));
                    continue;
                }
                listCellModel.add(rs.get(0));
                Thread.sleep(30);
            }
        } catch (Exception e) {
            System.out.println("Work.sysTOYun, 读取数据异常！！！" + e);
            errorSum++;
        }
        if (CollectionUtils.isEmpty(listCellModel)) {
            errorSum++;
        }
        // 写云库
        try {
            int rs = taskYunMapper.insertList(listCellModel);
            if (rs <= 0) {
                errorSum++;
            }
            System.out.println("Work.sysTOYun, 写云数据库成功");
        } catch (Exception e) {
            System.out.println("Work.sysTOYun, 写云数据库异常" + e);
            errorSum++;
        }
        // 钉钉报警
        if ("1".equals(mainWitch) && errorSum > 0) {
            String msg = "【甜圆有机】【现场报警】：同步数据到云失败,异常总次数：" + sysTOYUNException + " 次" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
            System.out.println(msg);
            sysTOYUNException++;
            sysTOYunError++;
            sendQuantiyAlarmInfo(msg, getListMobies());
        }
    }

    /**
     * 同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程、同步远程配置数据主进程
     */
    private static int yunUpdateListCellSum = 0;

    /**
     * 告警次数
     */
    int sum = 0;

    public void sysWorkConfig() {

        if (!cronReadYUNConfigSwitch) {
            System.out.println("定时更新云库配置数据,网络重试失败，已经切换到本地配置方案");
            return;
        }
        try {
            yunUpdateListCellModel = taskYunMapper.selectMainSwitch();
            if (yunUpdateListCellModel == null) {
                yunUpdateListCellSum++;
            }
            if (yunUpdateListCellModel != null && yunUpdateListCellSum > 3) {
                yunUpdateListCellSum = 0;
                sum = 0;
                String msg = "【甜圆有机】【现场报警】：定时更新云库配置数据，网络异常，恢复正常。";
                sendQuantiyAlarmInfo(msg, getListMobies());
                System.out.println(msg);
            }
        } catch (Exception e) {
            System.out.println("Work.sysWork2, 更新云库配置表失败");
            yunUpdateListCellSum++;
        }
        if (yunUpdateListCellSum > 3 && yunUpdateListCellSum <= 6) {
            sum++;
            String msg = "【甜圆有机】【现场报警】：定时更新云库配置数据，网络异常，远程读取失败，AlarmWork告警，第" + sum + "次。";
            System.out.println(msg);
            sendQuantiyAlarmInfo(msg, getListMobies());
        }
        // 大于10次 就不读取数据配置
        if (yunUpdateListCellSum > 10) {
            String msg = "【甜圆有机】【现场报警】：定时更新云库配置数据，网络异常，经过重试后无效，配置置为本地初始化状态，已断开与云库通信。";
            cronReadYUNConfigSwitch = false;
            sum = 0;
            System.out.println(msg);
            sendQuantiyAlarmInfo(msg, getListMobies());
        }
//        System.out.println("定时更新云库配置数据,成功");
    }

    /**
     * 同步内存中的主开关、同步内存中的主开关、同步内存中的主开关、同步内存中的主开关、同步内存中的主开关、同步内存中的主开关、同步内存中的主开关、同步内存中的主开关
     */
    public void sysWork1() {
        try {
            if (CollectionUtils.isEmpty(yunUpdateListCellModel)) {
                System.out.println("更新云配置失败，原因为读取数据库配置为空");
                return;
            }
            if (yunUpdateListCellModel.get(0) == null) {
                System.out.println("更新云配置失败，原因为读取数据库配置，第一条数据为空");
                return;
            }
            if (StringUtils.isEmpty(yunUpdateListCellModel.get(0).getManSwitch())) {
                System.out.println("更新云配置失败，原因为读取数据库配置，第一条数据，报警主开关为空");
                return;
            }
            // 钉钉推送主开关状态
            if (!mainWitch.equals(yunUpdateListCellModel.get(0).getManSwitch())) {
                String msg = "【甜圆有机】【推送提醒】：主开关推送，状态置为：" + yunUpdateListCellModel.get(0).getManSwitch();
                sendQuantiyAlarmInfo(msg, getListMobies());
            }
            // 更新主开关配置
            setMainWitch(yunUpdateListCellModel.get(0).getManSwitch());
            // 主开关  默认为1，打开主开关，0 为关闭
            System.out.println("更新云库配置表，主开关---------------------" + JSONObject.toJSONString(yunUpdateListCellModel.get(0).getManSwitch()));
        } catch (Exception e) {
            System.out.println("更新云库配置表，异常" + e);
        }
        sysWork99();
    }

    /**
     * 同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点、同步内存中的采集点
     */
    public void sysWork99() {

        try {
            if (CollectionUtils.isEmpty(yunUpdateListCellModel)) {
                System.out.println("更新云配置到本地失败，原因为读取数据库配置为空");
                return;
            }
            if (yunUpdateListCellModel.get(0) == null) {
                System.out.println("更新云配置到本地失败，原因为读取数据库配置，第一条数据为空");
                return;
            }
            if (StringUtils.isEmpty(yunUpdateListCellModel.get(0).getListCells())) {
                System.out.println("更新云配置到本地失败，原因为读取数据库配置，第一条数据，采集点列表为空");
                return;
            }
            String sptList = yunUpdateListCellModel.get(0).getManSwitch();
            String[] sysCell = sptList.toString().split("\\,");
            System.out.println(")))))))))))))))" + JSONObject.toJSONString(sysCell));
            if (sysCell.length != 0) {
                sysCellList = sysCell;
                System.out.println("更新云配置到本地，采集点更新成功");
            }
        } catch (Exception e) {
            System.out.println("更新云配置到本地，更新采集点异常" + e);
        }

    }

    /**
     * 同步云库数据失败后，回滚配置。
     */
    public void errorGoBack() {
        // 回滚采集点
        setSysCellList(readExcelService.getSysCellList());
        // 回滚主开关
        setMainWitch("0");

    }


    /**
     * 同步至云，初始化配置
     */
    private void loadSysCell() {
        if (sysCellList.length == 0) {
            setSysCellList(readExcelService.getSysCellList());
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
            System.out.println("钉钉报警HttpClint服务异常");
        }
    }

    public static boolean isCronConfig() {
        return cronReadYUNConfigSwitch;
    }

    public static void setCronConfig(boolean cronConfig) {
        Work.cronReadYUNConfigSwitch = cronConfig;
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
