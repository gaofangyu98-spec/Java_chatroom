package com.example.java_chatroom.api;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TestWebSocketAPI extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 这个方法会在 websocket 连接建立成功后, 被自动调用.
        System.out.println("TestAPI 连接成功!");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 这个方法是在 websocket 收到消息的时候, 被自动调用的.
        System.out.println("TestAPI 收到消息!" + message.toString());
        // session 是个会话, 里面就记录了通信双方是谁. (session 中就持有了 websocket 的通信连接)
        session.sendMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 这个方法是在连接出现异常的时候, 被自动调用的.
        System.out.println("TestAPI 连接异常!");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 这个方法是在连接正常关闭后, 被自动调用的
        System.out.println("TestAPI 连接关闭!");
    }
}
