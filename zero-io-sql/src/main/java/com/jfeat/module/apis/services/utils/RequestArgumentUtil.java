package com.jfeat.module.apis.services.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数解析工具（静态方法）
 */
public final class RequestArgumentUtil {

    private RequestArgumentUtil() {}

    /**
     * 获取请求参数键值对（支持 query/form 多值，按最后一次出现覆盖）
     */
    public static Map<String, String> parseGetRequestArgument(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> attrs = new HashMap<>();
        if (paramMap == null || paramMap.isEmpty()) {
            return attrs;
        }
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                // 取最后一个值作为该键的值（与原实现一致行为）
                attrs.put(key, values[values.length - 1]);
            }
        }
        return attrs;
    }
}