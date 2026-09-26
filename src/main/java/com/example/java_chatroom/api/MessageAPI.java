package com.example.java_chatroom.api;

import com.example.java_chatroom.model.Message;
import com.example.java_chatroom.model.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Collections;
import java.util.List;

@RestController
public class MessageAPI {
    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/message")
    public Object getMessage(int sessionId) {
        List<Message> messages = messageMapper.getMessagesBySessionId(sessionId);
        // 针对查询结果, 进行逆置操作. 毕竟界面上需要的是按照时间升序排列的消息. 此处得到的是降序排列的消息.
        Collections.reverse(messages);
        return messages;
    }
}
