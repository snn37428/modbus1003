package shop.pojo;

import java.util.Date;

public class Spot {
    private Integer id;

    private Integer plcId;

    private String spotName;

    private String spotDesc;

    private Integer modbusAddress;

    private String value;

    private Date created;

    private String tableIndex;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlcId() {
        return plcId;
    }

    public void setPlcId(Integer plcId) {
        this.plcId = plcId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName == null ? null : spotName.trim();
    }

    public String getSpotDesc() {
        return spotDesc;
    }

    public void setSpotDesc(String spotDesc) {
        this.spotDesc = spotDesc == null ? null : spotDesc.trim();
    }

    public Integer getModbusAddress() {
        return modbusAddress;
    }

    public void setModbusAddress(Integer modbusAddress) {
        this.modbusAddress = modbusAddress;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? null : value.trim();
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(String tableIndex) {
        this.tableIndex = tableIndex;
    }
}