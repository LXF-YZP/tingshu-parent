package com.atguigu.tingshu.common.login;


import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class TsLoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(tsLogin)")
    public Object doBasicProfiling(ProceedingJoinPoint joinPoint, TsLogin tsLogin) throws Throwable {

        //1.先判断当前用户是否登录，登录成功之后，将token放入请求头中
        //判断请求头中是否有token就可以
        //2.如果有token，需要查询缓存中是否真正存在用户信息
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) attributes;
        assert servletRequestAttributes != null;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String token = request.getHeader("token");
        //判断注解属性
        if (tsLogin.required()) {
            //判断token是否为空
            if (StringUtils.isEmpty(token)) {
                //说明没登录过，；LOGIN_AUTH(208, 未登录)
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
            //判断缓存中是否真正有用户信息
            String userLoginKey = RedisConstant.USER_LOGIN_KEY_PREFIX + token;
            //获取缓存综合功能的数据
            UserInfo userInfo = (UserInfo)this.redisTemplate.opsForValue().get(userLoginKey);
            //如果用户信息 为空
            if (null == userInfo) {
                //说明没有登录过，LOGIN_AUTH(208,未登录)
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //判断当前token不为空
        if (!StringUtils.isEmpty(token)){
            //有token, 那么缓存中可能存在
            String userLoginKey = RedisConstant.USER_LOGIN_KEY_PREFIX + token;
            //获取缓存中的数据
            UserInfo userInfo =(UserInfo) this.redisTemplate.opsForValue().get(userLoginKey);
            if (null != userInfo){
                //将用户Id存储到本地
                AuthContextHolder.setUserId(userInfo.getId());
            }
        }

        //声明一个对象
        Object proceed;
        try {
            //执行方法
            proceed = joinPoint.proceed();
        } finally {
            //防止内存泄漏
            AuthContextHolder.removeUserId();
        }
        return proceed;
    }

}