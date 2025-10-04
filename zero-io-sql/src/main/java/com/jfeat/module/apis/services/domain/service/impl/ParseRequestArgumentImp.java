package com.jfeat.module.apis.services.domain.service.impl;

import com.jfeat.module.apis.services.domain.service.ParseRequestArgument;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@Service
public class ParseRequestArgumentImp implements ParseRequestArgument {


    @Override
    public Map<String, String> parseGetRequestArgument(HttpServletRequest request) {
//        获取请求参数列表
        Map<String, String[]> map = request.getParameterMap();

        Set keSet = map.entrySet();

//        最终返回结果
        Map<String,String> attr = new HashMap<>();

        for(Iterator itr = keSet.iterator(); itr.hasNext();){
            Map.Entry me=(Map.Entry)itr.next();
            Object ok=me.getKey();
            Object ov=me.getValue();
            String[] value=new String[1];
            if(ov instanceof String[]){
                value=(String[])ov;
            }else {
                value[0]=ov.toString();
            }
            for(int k=0;k<value.length;k++){
                attr.put((String) ok,value[k]);
            }
        }
        return attr;
    }
}
