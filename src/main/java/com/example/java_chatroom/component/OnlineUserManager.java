package com.example.java_chatroom.component;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// 通过这个类来记录当前用户在线的状态. (维护了 userId 和 WebSocketSession 之间的映射)
@Component
public class OnlineUserManager {
    // 此处这个 哈希表 要考虑到线程安全问题.
    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 1) 用户上线, 给这个哈希表中插入键值对
    public void online(int userId, WebSocketSession session) {
        if (sessions.get(userId) != null) {
            // 此时说明用户已经在线了, 就登录失败, 不会记录这个映射关系.
            // 不记录这个映射关系, 后续就收不到任何消息 (毕竟, 咱们此处是通过映射关系来实现消息转发的)
            System.out.println("[" + userId + "] 已经被登录了, 登录失败!");
            return;
        }
        sessions.put(userId, session);
        System.out.println("[" + userId + "] 上线!");
    }

    // 2) 用户下线, 针对这个哈希表进行删除元素
    public void offline(int userId, WebSocketSession session) {
        WebSocketSession existSession = sessions.get(userId);
        if (existSession == session) {
            // 如果这俩 session 是同一个, 才真正进行下线操作. 否则就啥都不干
            sessions.remove((userId));
            System.out.println("[" + userId + "] 下线!");
        }
    }

    // 3) 根据 userId 获取到 WebSocketSession
    public WebSocketSession getSession(int userId) {
        return sessions.get(userId);
    }
}
