package com.example.zeroiorocketmq.producer.service;

import org.apache.rocketmq.client.producer.SendResult;

public interface RocketMQProducer {
    SendResult sendSyncMessage(String topic, String message);

    SendResult sendSyncMessage(String topic, String tag, String message);

}
