package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.TsLogin;
import com.atguigu.tingshu.common.login.TsLoginAspect;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.rabbitmq.client.Return;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private TsLoginAspect tsLoginAspect;

    //获取用户信息
    @TsLogin
    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    public Result getUserInfo(){
        Long userId = AuthContextHolder.getUserId();
        UserInfoVo userInfoVo = userInfoService.getUserInfo(userId);
        return Result.ok(userInfoVo);
    }



    // 微信登录
    @Operation(summary = "微信登录")
    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable String code) {
        log.info("微信登录开始...");
        //  调用服务层方法;
        Map<String ,Object> map = userInfoService.wxLogin(code);
        //  返回数据
        return Result.ok(map);
    }


}
