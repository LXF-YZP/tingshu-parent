package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

	@Autowired
	private AlbumAttributeValueMapper albumAttributeValueMapper;
	@Autowired
	private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private AlbumStatMapper albumStatMapper;

	@Autowired
	private AlbumAttributeValueService albumAttributeValueService;

	@Override
	public IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> page, AlbumInfoQuery albumInfoQuery) {

			return albumInfoMapper.selectUserAlbumPage(page, albumInfoQuery);
	}

	@Override
	public AlbumInfo getAlbumInfo(Long albumId) {

		//因为需要给专辑标签赋值，即给albumAttributeValueVoList赋值
		AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);
		if (null != albumInfo) {
			//根据专辑Id获取到标签数据
			//select * from album_attibute_value where album_id = 1630
			LambdaQueryWrapper<AlbumAttributeValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
			lambdaQueryWrapper.eq(AlbumAttributeValue ::getAlbumId, albumId);
			List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueMapper.selectList(lambdaQueryWrapper);
			albumInfo.setAlbumAttributeValueVoList(albumAttributeValues);

		}

		return albumInfo;
	}

	@Override
	public List<AlbumInfo> findUserAllAlbumList(Long userId) {

		//构建查询条件
		LambdaQueryWrapper<AlbumInfo> queryChainWrapper = new LambdaQueryWrapper<>();
		queryChainWrapper.eq(AlbumInfo::getUserId, userId);
		queryChainWrapper.orderByDesc(AlbumInfo::getId);
		Page<AlbumInfo> page = new Page<>(1, 100);
		Page<AlbumInfo> albumInfoPage = albumInfoMapper.selectPage(page, queryChainWrapper);

		return albumInfoPage.getRecords();

	}

	@Override
	public void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo) {
		AlbumInfo albumInfo = new AlbumInfo();
		BeanUtils.copyProperties(albumInfoVo, albumInfo);
		albumInfo.setId(albumInfo.getId());
		albumInfoMapper.updateById(albumInfo);
		//album_attribute_value 由于修改值的时候未出发控制器，所以不能实时的记录修改的信息，因此需要先删除再新增数据
		//删除原有标签数据，
		albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
		List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
		if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
			//循环遍历数据
			for (AlbumAttributeValueVo albumAttributeValueVo : albumAttributeValueVoList) {

				AlbumAttributeValue attributeValue = new AlbumAttributeValue();
				//属性拷贝
				BeanUtils.copyProperties(albumAttributeValueVo, attributeValue);
				attributeValue.setAlbumId(albumId);
				albumAttributeValueMapper.insert(attributeValue);
			}
		}

	}

	@Override
	public void removeAlbumInfo(Long albumId) {
		//删除album_info;
		albumInfoMapper.deleteById(albumId);
		//删除album_attibute_value
		albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
		//删除统计信息album_stat
		albumStatMapper.delete(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, albumId));
	}


	@Override
	public Boolean saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId) {

		try {
			AlbumInfo albumInfo = new AlbumInfo();
			BeanUtils.copyProperties(albumInfoVo, albumInfo);
			albumInfo.setUserId(userId);
			if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfoVo.getPayType())) {
				albumInfo.setTracksForFree(5);
			}
			albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);
			if (albumInfo != null) {
				albumInfoMapper.insert(albumInfo);
			}
			saveBatchAlbumAttributeValue(albumInfoVo, albumInfo);
			saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY);
			saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE);
			saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_BROWSE);
			saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return true;
	}

//	private void saveAlbumAttributeValue(AlbumInfoVo albumInfoVo, AlbumInfo albumInfo) {
//
//		List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
//		if (!albumAttributeValueVoList.isEmpty()) {
//			for (AlbumAttributeValueVo albumAttributeValueVo : albumAttributeValueVoList) {
//				AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
//				albumAttributeValue.setAlbumId(albumInfo.getId());
//				albumAttributeValue.setAttributeId(albumAttributeValueVo.getAttributeId());
//				albumAttributeValue.setValueId(albumAttributeValueVo.getValueId());
//				albumAttributeValueMapper.insert(albumAttributeValue);
//			}
//		}
//
//	}

	private void saveBatchAlbumAttributeValue(AlbumInfoVo albumInfoVo, AlbumInfo albumInfo) {

		List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
		List<AlbumAttributeValue> attributeValueList = new ArrayList<>();

		if (!albumAttributeValueVoList.isEmpty()) {
			for (AlbumAttributeValueVo albumAttributeValueVo : albumAttributeValueVoList) {
				AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
				albumAttributeValue.setAlbumId(albumInfo.getId());
				albumAttributeValue.setAttributeId(albumAttributeValueVo.getAttributeId());
				albumAttributeValue.setValueId(albumAttributeValueVo.getValueId());
				attributeValueList.add(albumAttributeValue);
			}
		}
		albumAttributeValueService.saveBatch(attributeValueList);

	}

	private void saveAlbumStat(Long id, String statPlay) {

		log.info("保存专辑统计信息：{}", id);
		AlbumStat albumStat = new AlbumStat();
		albumStat.setAlbumId(id);
		albumStat.setStatType(statPlay);
		albumStat.setStatNum(new Random().nextInt(1000000));
		albumStatMapper.insert(albumStat);
	}
}
