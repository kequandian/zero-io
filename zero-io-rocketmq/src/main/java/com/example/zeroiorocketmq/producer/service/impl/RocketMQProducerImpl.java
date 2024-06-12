package com.example.zeroiorocketmq.producer.service.impl;

import com.example.zeroiorocketmq.producer.service.RocketMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class RocketMQProducerImpl implements RocketMQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    public SendResult sendSyncMessage(String topic, String message) {
        SendResult sendResult = rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(message).build());

        return sendResult;
    }
    public SendResult sendSyncMessage(String topic, String tag, String message) {
        // 使用 'topic:tag' 格式发送消息
        String destination = topic + ":" + tag;
        SendResult sendResult = sendSyncMessage(destination,message);

        return sendResult;
    }
}
