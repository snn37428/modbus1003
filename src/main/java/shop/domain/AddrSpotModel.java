package shop.domain;

import java.util.List;

public class AddrSpotModel {

    /**
     * 配置文件id
     */
    private List<Integer> Id;

    /**
     * 地址点功能码
     */
    private String modelCode;

    /**
     * 点步长
     */
    private int addNum;

    /**
     * 起始点
     */
    private int nFrom;

    /**
     * 结束点
     */
    private String nEnd;

    /**
     * 类型 int
     */
    private String type;

    /**
     * 周期
     */
    private String cyc;

    /**
     * 点集合
     */
    private List<Integer> listSpot;

    /**
     * 点描述
     */
    private List<String> spotDesc;

    /**
     * 点描述 中文
     */
    private List<String> spotDescCN;

    /**
     * 读取的PLC数值
     * @return
     */
    private List<Float> plcValue;

    /**
     * 分组编码
     * @return
     */
    private String groupCode;

    public List<Integer> getId() {
        return Id;
    }

    public void setId(List<Integer> id) {
        Id = id;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public int getAddNum() {
        return addNum;
    }

    public void setAddNum(int addNum) {
        this.addNum = addNum;
    }

    public int getnFrom() {
        return nFrom;
    }

    public void setnFrom(int nFrom) {
        this.nFrom = nFrom;
    }

    public List<Integer> getListSpot() {
        return listSpot;
    }

    public void setListSpot(List<Integer> listSpot) {
        this.listSpot = listSpot;
    }

    public List<String> getSpotDesc() {
        return spotDesc;
    }

    public void setSpotDesc(List<String> spotDesc) {
        this.spotDesc = spotDesc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSpotDescCN() {
        return spotDescCN;
    }

    public void setSpotDescCN(List<String> spotDescCN) {
        this.spotDescCN = spotDescCN;
    }

    public String getCyc() {
        return cyc;
    }

    public void setCyc(String cyc) {
        this.cyc = cyc;
    }

    public String getnEnd() {
        return nEnd;
    }

    public void setnEnd(String nEnd) {
        this.nEnd = nEnd;
    }

    public List<Float> getPlcValue() {
        return plcValue;
    }

    public void setPlcValue(List<Float> plcValue) {
        this.plcValue = plcValue;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
