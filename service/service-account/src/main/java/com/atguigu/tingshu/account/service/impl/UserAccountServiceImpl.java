package com.atguigu.tingshu.account.service.impl;

import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

	@Autowired
	private UserAccountMapper userAccountMapper;

	@Override
	public void userRegister(Long userId) {
		//	创建一个账户对象
		UserAccount userAccount = new UserAccount();
		//	赋值:
		userAccount.setUserId(userId);
		userAccount.setTotalAmount(new BigDecimal("100.00"));
		userAccount.setAvailableAmount(new BigDecimal("100.00"));
		userAccountMapper.insert(userAccount);
	}
}
