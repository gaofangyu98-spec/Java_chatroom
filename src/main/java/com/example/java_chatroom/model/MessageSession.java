package com.example.java_chatroom.model;

import lombok.Data;

import java.util.List;
@Data
// 使用这个类表示一个会话
public class MessageSession {
    private int sessionId;
    private List<Friend> friends;
    private String lastMessage;
}
