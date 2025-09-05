package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserAccountService extends IService<UserAccount> {


    /**
     * 用户注册
     * @param userId
     */
    void userRegister(Long userId);
}
