package com.atguigu.tingshu.common.login;


import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TsLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;





}
