package com.example.zeroiorocketmq;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Publisher {
    private Map<String,RegisterService> subscribers = new HashMap<>();

    // 注册订阅者 name为topic 根据每个服务注册的topic 返回消息
    public void attach(String name,RegisterService subscriber) {
        if (name!=null && subscriber!=null){
            subscribers.put(name,subscriber);
        }
    }

    // 发布通知
    public void notifySubscribers(String name,String massage) {
        if (subscribers.containsKey(name)){
            RegisterService registerService = subscribers.get(name);
            registerService.registerService(massage);
        }
    }
}
