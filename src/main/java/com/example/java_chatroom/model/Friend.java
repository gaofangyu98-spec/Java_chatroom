package com.example.java_chatroom.model;

import lombok.Data;

@Data

// 使用一个 Friend 对象表示一个好友.
public class Friend {

    private int friendId;
    private String friendName;

}