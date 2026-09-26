package com.example.java_chatroom.model;

import lombok.Data;

@Data
// 使用这个类的对象来表示 message_session_user 表里的一个记录
public class MessageSessionUserItem {
    private int sessionId;
    private int userId;

}