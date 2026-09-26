package com.example.java_chatroom.api;

import lombok.Data;

@Data
// 表示一个消息请求
public class MessageRequest {
    private String type = "message";
    private int sessionId;
    private String content;

}
