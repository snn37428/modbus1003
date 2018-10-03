package shop.domain;

/**
 * create snn by 2018/09/16
 */

public class CellModel {

    /**
     * id
     */
    private String id;

    /**
     * 点名
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 中文描述
     */
    private String desc;

    /**
     * 功能码
     */
    private String model;

    /**
     * modbus地址
     */
    private String modbusAddr;

    /**
     * plc读取的值
     */
    private String Value;

    /**
     * 存储周期
     */
    private String cyc;

    /**
     * 分组编码
     *
     * @return
     */
    private String groupCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModbusAddr() {
        return modbusAddr;
    }

    public void setModbusAddr(String modbusAddr) {
        this.modbusAddr = modbusAddr;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getCyc() {
        return cyc;
    }

    public void setCyc(String cyc) {
        this.cyc = cyc;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
