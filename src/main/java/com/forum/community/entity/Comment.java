package com.forum.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private int id;//评论ID
    private int userId;//发出评论的用户ID
    private int entityType;//评论的实体类型
    private int entityId;//实体的ID
    private int targetId;//评论是回复的时候，回复的对象
    private String content;//评论内容
    private int status;//评论状态
    private Date createTime;//评论创建时间
}
