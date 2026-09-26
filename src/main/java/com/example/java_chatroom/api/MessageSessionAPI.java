package com.example.java_chatroom.api;

import com.example.java_chatroom.model.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class MessageSessionAPI {
    @Resource
    private MessageSessionMapper messageSessionMapper;

    @Resource
    private MessageMapper messageMapper;

    @GetMapping("/sessionList")
    @ResponseBody
    public Object getMessageSessionList(HttpServletRequest req) {
        List<MessageSession> messageSessionList = new ArrayList<>();
        // 1. 获取到当前用户的 userId (从 spring 的 session 中获取)
        HttpSession session = req.getSession(false);
        if (session == null) {
            System.out.println("[getMessageSessionList] session == null");
            return messageSessionList;
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            System.out.println("[getMessageSessionList] user == null");
            return messageSessionList;
        }
        // 2. 根据 userId 查询数据库, 查出来有哪些会话id
        List<Integer> sessionIdList = messageSessionMapper.getSessionIdsByUserId(user.getUserId());
        for (int sessionId : sessionIdList) {
            MessageSession messageSession = new MessageSession();
            messageSession.setSessionId(sessionId);
            // 3. 遍历会话id, 查询出每个会话里涉及到的好友都有谁
            List<Friend> friends = messageSessionMapper.getFriendsBySessionId(sessionId, user.getUserId());
            messageSession.setFriends(friends);
            // 4. 遍历会话id, 查询出每个会话的最后一条消息
            //    有可能出现按照会话 id 查不到消息的情况 (新创建的会话可能还没来得及发消息呢
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);
            if (lastMessage == null) {
                messageSession.setLastMessage("");
            } else {
                messageSession.setLastMessage(lastMessage);
            }
            messageSessionList.add(messageSession);
        }
        // 最终目标就是构造出一个 MessageSession 对象数组
        return messageSessionList;
    }

    @PostMapping("/session")
    @ResponseBody
    @Transactional
    public Object addMessageSession(int toUserId, @SessionAttribute("user") User user) {
        HashMap<String, Integer> resp = new HashMap<>();
        // 进行数据库的插入操作
        // 1. 先给 message_session 表里插入记录. 使用这个参数的目的主要是为了能够获取到会话的 sessionId
        //    换而言之, MessageSession 里的 friends 和 lastMessage 属性此处都用不上.
        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);
        // 2. 给 message_session_user 表插入记录.
        MessageSessionUserItem item1 = new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUserId());
        messageSessionMapper.addMessageSessionUser(item1);
        // 3. 给 message_session_user 表插入记录.
        MessageSessionUserItem item2 = new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);

        System.out.println("[addMessageSession] 新增会话成功! sessionId=" + messageSession.getSessionId()
                + " userId1=" + user.getUserId() + " userId2=" + toUserId);

        resp.put("sessionId", messageSession.getSessionId());
        // 返回的对象是一个普通对象也可以, 或者是一个 Map 也可以, jackson 都能进行处理.
        return resp;
    }
}
