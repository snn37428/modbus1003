package shop.dao;

import shop.domain.CellModel;

import java.util.List;

public interface TaskMapper {

    int insert(CellModel cellModel);

    int insertList(List<CellModel> cellModelListl);

    List<CellModel> listCellModel();

    List<CellModel>  selectById(CellModel cellModel);
}
