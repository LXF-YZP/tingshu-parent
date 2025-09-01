package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {


    Boolean saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);

    IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> page, AlbumInfoQuery albumInfoQuery);

    AlbumInfo getAlbumInfo(Long albumId);

    List<AlbumInfo> findUserAllAlbumList(Long userId);

    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);

    void removeAlbumInfo(Long albumId);

}
