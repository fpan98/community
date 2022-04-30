package com.mininowcoder.community;

import com.alibaba.fastjson.JSONObject;
import com.mininowcoder.community.entity.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/26.
 */


public class JsonObjectTests {

    public static void main(String[] args){
        String json = "{\"data\":{\"postId\":280},\"entityId\":280,\"entityType\":1,\"entityUserId\":149,\"topic\":\"comment\",\"userId\":152}";
        Event event = JSONObject.parseObject(json, Event.class);
        System.out.println(event);
    }
}
