package com.atguigu.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private WxMaService wxMaService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RabbitService rabbitService;

	@Override
	public Map<String, Object> wxLogin(String code) {
		//	调用微信登录api;
		WxMaJscode2SessionResult wxMaJscode2SessionResult = null;
		try {
			wxMaJscode2SessionResult = wxMaService.jsCode2SessionInfo(code);
		} catch (WxErrorException e) {
			throw new RuntimeException(e);
		}
		//	openid每个人的微信都对应的一个微信标识！
		String openid = wxMaJscode2SessionResult.getOpenid();
		//	利用openid实现登录或注册功能;
		UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openid));
		//	判断对象是否为空
		if (null == userInfo) {
			//	说明要注册,本质赋值;
			userInfo = new UserInfo();
			userInfo.setWxOpenId(openid);
			userInfo.setNickname("听友:" + System.currentTimeMillis());
			userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
			//	保存数据到数据库;
			userInfoMapper.insert(userInfo);
			//  在此需要给当前注册的用户进行初始化充值;  同步：异步：mq 发送消息;
			rabbitService.sendMessage(MqConst.EXCHANGE_ALBUM,MqConst.ROUTING_USER_REGISTER,userInfo.getId());
		}
		//	说明要登录： 将数据存储到redis，与返回token;
		String token = UUID.randomUUID().toString().replaceAll("-", "");
		//	组成缓存的key;
		String userLoginKey = RedisConstant.USER_LOGIN_KEY_PREFIX + token;
		//  存储数据;
		redisTemplate.opsForValue().set(userLoginKey, userInfo, RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
		//  返回数据;
		HashMap<String, Object> map = new HashMap<>();
		map.put("token", token);
		return map;
	}

	/*
	*
	* */
	@Override
	public UserInfoVo getUserInfo(Long userId) {

		//查询用户信息
		UserInfo userInfo = this.getById(userId);
		//创建目标对象
		UserInfoVo userInfoVo = new UserInfoVo();
		BeanUtils.copyProperties(userInfoVo, userInfoVo);
		return userInfoVo;

	}
}
