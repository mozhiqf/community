package com.forum.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private int id;//消息的Id
    private int fromId;//发送方
    private int toId;//接受方
    private String conversationId;//消息所属的会话Id,格式为111_222，userId小的在前面
    private String content;//message的内容
    private int status;//消息的状态 0代表未读 1代表已读 2代表已删除
    private Date createTime;//消息创建时间
}
