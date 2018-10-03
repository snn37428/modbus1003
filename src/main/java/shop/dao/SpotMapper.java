package shop.dao;

import shop.pojo.Spot;

import java.util.List;

public interface SpotMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Spot record);

    int insertSpotInt(List<Spot> spotList);

    int insertSpotBool(List<Spot> spotList);

    int insertSelective(Spot record);

    Spot selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Spot record);

    int updateByPrimaryKey(Spot record);

}