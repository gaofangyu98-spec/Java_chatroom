package com.example.java_chatroom.api;

import lombok.Data;

@Data
// 表示一个响应
public class MessageResponse {
    private String type = "message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;
}
