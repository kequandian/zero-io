package com.example.zeroiorocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HandlerForTest {

    @EventListener(condition = "#event.msgTag=='" + "test")
    public void execute(BaseEvent event) {
        Object source = event.getSource();
        log.info("事件监听类: tag: {}, msgType: {}, date: {}, data:{}", event.getMsgTag(), event.getMsgType(), event.getDate(), event.getSource());
    }
}
