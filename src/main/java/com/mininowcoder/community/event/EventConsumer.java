package com.mininowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mininowcoder.community.entity.DiscussPost;
import com.mininowcoder.community.entity.Event;
import com.mininowcoder.community.entity.Message;
import com.mininowcoder.community.service.DiscussPostService;
import com.mininowcoder.community.service.ElasticsearchService;
import com.mininowcoder.community.service.MessageService;
import com.mininowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/25.
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        /**
         * 在将json转换成map对象是将其中的data丢失了( Map<String, Object> data = new HashMap() )
         * 之前的event的定义是这样的
         * public class Event {
         *     private String topic; // 事件的类型-评论,点赞,关注
         *     private int userId; // 事件触发的人
         *     private int entityType; // 实体类型
         *     private int entityId; // 实体id
         *     private int entityUserId; // 实体作者
         *     private Map<String, Object> data = new HashMap<>();
         *
         *     public String getTopic() {
         *         return topic;
         *     }
         *
         *     public Event setTopic(String topic) {
         *         this.topic = topic;
         *         return this;
         *     }
         *
         *     public int getUserId() {
         *         return userId;
         *     }
         *
         *     public Event setUserId(int userId) {
         *         this.userId = userId;
         *         return this;
         *     }
         *
         *     public int getEntityType() {
         *         return entityType;
         *     }
         *
         *     public Event setEntityType(int entityType) {
         *         this.entityType = entityType;
         *         return this;
         *     }
         *
         *     public int getEntityId() {
         *         return entityId;
         *     }
         *
         *     public Event setEntityId(int entityId) {
         *         this.entityId = entityId;
         *         return this;
         *     }
         *
         *     public int getEntityUserId() {
         *         return entityUserId;
         *     }
         *
         *     public Event setEntityUserId(int entityUserId) {
         *         this.entityUserId = entityUserId;
         *         return this;
         *     }
         *
         *     public Map<String, Object> getData() {
         *         return data;
         *     }
         *
         *     public Event setData(String key, Object value) {
         *         this.data.put(key, value);
         *         return this;
         *     }
         *
         *     @Override
         *     public String toString() {
         *         return "Event{" +
         *                 "topic='" + topic + '\'' +
         *                 ", userId=" + userId +
         *                 ", entityType=" + entityType +
         *                 ", entityId=" + entityId +
         *                 ", entityUserId=" + entityUserId +
         *                 ", data=" + data +
         *                 '}';
         *     }
         * }
         */
        if(event==null){
            logger.error("消息格式错误!");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry: event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    // 消费发帖事件以及对帖子存在修改数据的事件(评论帖子)
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlerPublishMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误!");
            return;
        }
        // 查询到修改后的帖子，并更新至elasticsearch中
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }


    // 消费删帖（拉黑）事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handlerDeleteMessage(ConsumerRecord record){
        if(record==null||record.value()==null){
            logger.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event==null){
            logger.error("消息格式错误!");
            return;
        }
        elasticsearchService.deleteDiscusspost(event.getEntityId());
    }


}
