package com.example.zeroiorocketmq.controller;

import com.example.zeroiorocketmq.producer.service.RocketMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rockemq/test")
public class TestController {

    @Autowired
    RocketMQProducer rocketMQProducer;

    @PostMapping("/send")
    public void sendMq(@RequestParam("tag") String tag,@RequestParam("msg")String msg){
        SendResult sendResult = rocketMQProducer.sendSyncMessage("springboot-mq", tag, msg);
        System.out.println(sendResult.getSendStatus());
    }

}
