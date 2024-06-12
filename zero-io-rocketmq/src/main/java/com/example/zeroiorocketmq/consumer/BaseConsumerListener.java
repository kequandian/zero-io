package com.example.zeroiorocketmq.consumer;

import com.example.zeroiorocketmq.util.JackJsonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RocketMQMessageListener(topic = "${rocketmq.topic}", consumerGroup = "${rocketmq.consumer.group}")
public class BaseConsumerListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {


    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onMessage(MessageExt message) {
        String topic = message.getTopic();
        String tag = message.getTags();
        byte[] body = message.getBody();
        String keys = message.getKeys();
        String msgId = message.getMsgId();
        String realTopic = message.getProperty("REAL_TOPIC");
        String originMessageId = message.getProperty("ORIGIN_MESSAGE_ID");
        // 获取重试的次数 失败一次消息中的失败次数会累加一次
        int reconsumeTimes = message.getReconsumeTimes();

        String jsonBody = JackJsonUtil.toJSONString((new String(body)));

        // 消费者幂等处理: 设计去重表,防止重复消费
        applicationContext.publishEvent(new BaseEvent(tag, jsonBody));
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 设置最大重试次数
        consumer.setMaxReconsumeTimes(3);
        // 如下，设置其它consumer相关属性
        consumer.setPullBatchSize(16);
    }
}
