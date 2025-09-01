package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {

//分页查询用户声音列表
    IPage<TrackListVo> selectUserTrackPage(Page<TrackListVo> page, @Param("vo") TrackInfoQuery trackInfoQuery);

    //修改专辑排序
    @Update("update track_info set order_num = order_num -1 where album_id = #{albumId} and order_num > #{orderNum} and is_deleted = 0")
    void updateOrderNum(Long albumId, Integer orderNum);

}
