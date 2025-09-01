package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;

	@Autowired
	private AlbumInfoMapper albumInfoMapper;

	@Autowired
	private VodService vodService;
    @Autowired
    private TrackStatMapper trackStatMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId) {

		try {
			TrackInfo trackInfo = new TrackInfo();
			BeanUtils.copyProperties(trackInfoVo, trackInfo);
			trackInfo.setUserId(userId);
			AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfoVo.getAlbumId());
			trackInfo.setOrderNum(albumInfo.getIncludeTrackCount() + 1);
			//处理流媒体的数据，时长，类型这些数据页面没有传递过来，fileId查询流媒体数据，然后赋值，调佣云点播的api
			TrackMediaInfoVo trackMediaInfoVo = vodService.getMediaInfo(trackInfoVo.getMediaFileId());
			//开始赋值
			trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
			trackInfo.setMediaType(trackMediaInfoVo.getType());
			trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
			trackInfo.setMediaSize(trackMediaInfoVo.getSize());
			//保存声音
			trackInfoMapper.insert(trackInfo);
			//保存声音统计数据
			saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
			saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
			saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE);
			saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);
			//更新数据
			albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount() + 1);
			albumInfoMapper.updateById(albumInfo);
		} catch (BeansException e) {
			log.error("保存声音信息失败", e);
			throw new RuntimeException(e);
		}

	}

	private void saveTrackStat(Long trackId, String statPlay) {

		TrackStat trackStat = new TrackStat();
		trackStat.setTrackId(trackId);
		trackStat.setStatType(statPlay);
		trackStat.setStatNum(new Random().nextInt(1000000));
		trackStatMapper.insert(trackStat);
//		int i = 1/0;

	}

	@Override
	public IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> page, TrackInfoQuery trackInfoQuery) {

		return trackInfoMapper.selectUserTrackPage(page, trackInfoQuery);
	}

	@Override
	public void removeTrackInfo(Long trackId) {
		//查询当前声音对象
		TrackInfo trackInfo = this.getById(trackId);
		//删除track_info track_stat album_info表中的数据
		trackInfoMapper.deleteById(trackId);
		//track_stat
		trackStatMapper.delete(new LambdaQueryWrapper<TrackStat>().eq(TrackStat::getTrackId, trackId));
		//album_info更新include_track_count
		AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfo.getAlbumId());
		//删除一条数据
		albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount() - 1);
		//需要更新记录
		albumInfoMapper.updateById(albumInfo);
		trackInfoMapper.updateOrderNum(trackInfo.getAlbumId(), trackInfo.getOrderNum());
		vodService.deleteMediaFile(trackInfo.getMediaFileId());


	}

	@Override
	public void updateTrackInfo(Long trackId, TrackInfoVo trackInfoVo) {
		TrackInfo trackInfo = this.getById(trackId);
		//现货区原始的文件Id
		String mediaFileId = trackInfo.getMediaFileId();
		BeanUtils.copyProperties(trackInfoVo, trackInfo);
		if (!mediaFileId.equals(trackInfoVo.getMediaFileId())) {
			TrackMediaInfoVo trackMediaInfoVo = vodService.getMediaInfo(trackInfoVo.getMediaFileId());
			trackInfo.setMediaType(trackMediaInfoVo.getType());
			trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
			trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
			trackInfo.setMediaFileId(trackInfoVo.getMediaFileId());
			//删除原始文件
			vodService.deleteMediaFile(mediaFileId);
		}
		this.updateById(trackInfo);
	}


}
