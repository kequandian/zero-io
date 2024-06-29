package com.jfeat.zeroiorocketmq.consumer;

import com.jfeat.zeroiorocketmq.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HandlerForTest {



//    @Autowired
//    private Publisher publisher;
//    @EventListener(condition = "#event.msgTag != null ")
//    public void execute(BaseEvent event) {
//        Object source = event.getSource();
//        publisher.notifySubscribers(event.getMsgTag(),event.getSource().toString());
//        log.info("事件监听类: tag: {}, msgType: {}, date: {}, data:{}", event.getMsgTag(), event.getMsgType(), event.getDate(), event.getSource());
//    }


}
