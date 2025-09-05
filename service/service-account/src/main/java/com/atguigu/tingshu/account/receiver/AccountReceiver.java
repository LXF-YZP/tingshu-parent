package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陆小凤
 * @version 1.0
 * @description: TODO
 */
@Component
@Slf4j
public class AccountReceiver {

    @Autowired
    private UserAccountService userAccountService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_USER_REGISTER,durable = "true",autoDelete="false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ALBUM),
            key = MqConst.ROUTING_USER_REGISTER
    ))
    public void userRegister(Long userId, Message message, Channel channel){
        System.out.println("用户注册成功，用户id："+userId);
        try {
            if (null != userId){
                //  说明获取到了用户Id;
                //  调用服务层方法;
                userAccountService.userRegister(userId);
            }
        } catch (Exception e) {
            //  可以采用 redis进行重试;
            /*
            if(count>=3){
                落盘数据库表; 存储信息；
            }
             */
            //  如果网络抖动；可以调用内置的重试方法 channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            log.error(e.getMessage(),e);
        }
        //  手动确认消息
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
