package com.forum.community.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Event {

    private String topic;//事件类型
    private int userId;//事件触发的用户
    private int entityType;//事件发生的实体类型
    private int entityId;//事件发生的实体id
    private int entityUserId;//事件归属的用户
    private Map<String, Object> data = new HashMap<>();//其他的业务数据

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
