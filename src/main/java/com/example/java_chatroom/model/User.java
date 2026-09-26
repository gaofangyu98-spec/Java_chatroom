package com.example.java_chatroom.model;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String username = "";
    private String password = "";
}
