package com.shiliu.dragon.utils.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMap = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String toJson(Object object){
        if (object == null){
            return null;
        }
        try {
            String json = objectMap.writeValueAsString(object);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Object to json error",e);
        }
        return null;
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        if (objectMap == null) {
            objectMap = new ObjectMapper();
        }
        if(content == null){
            return null;
        }
        try {
            //字符串转Json对象
            return objectMap.readValue(content, valueType);
        } catch (Exception e) {
            logger.error("Json to object error",e);
        }
        return null;
    }
}
