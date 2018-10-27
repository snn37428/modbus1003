package shop.yun.dao;

import shop.domain.CellModel;

import java.util.List;

public interface TaskYunMapper {

    int insert(CellModel cellModel);

    int insertList(List<CellModel> cellModelListl);

    List<CellModel> selectMainSwitch();
}
