package com.zhangxd.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Producer {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(String queueKey, Object object) {
        // convertAndSend 将Java对象转换为消息发送至匹配key的交换机中Exchange
        amqpTemplate.convertAndSend(queueKey, object);
    }
}
