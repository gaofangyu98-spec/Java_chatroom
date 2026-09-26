# Java Chatroom

基于 Spring Boot 3 + WebSocket + MyBatis 的实时聊天室，支持单聊/群聊、在线用户管理、WebSocket 消息实时推送与 MySQL 消息历史持久化。

## 核心亮点

| 亮点 | 说明 |
|---|---|
| 双协议架构 | HTTP API 处理注册/好友/历史消息，WebSocket 处理实时推送，各干各的 |
| 在线用户管理 | ConcurrentHashMap 维护 userId → WebSocketSession 映射，单设备登录，后连踢前 |
| 握手拦截器 | HttpSessionHandshakeInterceptor 把登录态从 HttpSession 带到 WebSocketSession，复用同一份用户身份 |
| 安全下线 | 连接关闭时校验 session 同一性，避免旧连接误删新连接的映射 |
| 消息持久化 | 消息先落库再转发，离线用户下次登录拉历史消息 |
| 群聊扩展 | message_session_user 关联表，一个会话多成员，支持群聊扩展 |

---

## 目录

- [架构总览](#架构总览)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [数据库设计](#数据库设计)
- [WebSocket 消息流程](#websocket-消息流程)
- [关键设计点](#关键设计点)
- [API 接口](#api-接口)
- [WebSocket 消息格式](#websocket-消息格式)
- [快速开始](#快速开始)
- [功能特性](#功能特性)
- [安全说明](#安全说明)
- [可改进点](#可改进点)

---

## 架构总览

```
用户 A (浏览器)                   用户 B (浏览器)
    │                                  │
    │  HTTP API (登录/好友/消息历史)    │
    ├──────────────────────────────────┤
    │                                  │
    ▼                                  ▼
┌──────────────────────────────────────────┐
│  Spring Boot — 端口 8083                  │
│                                           │
│  ┌──────────┐  ┌──────────────────────┐  │
│  │ HTTP API  │  │  WebSocket 处理器    │  │
│  │ User/Friend│  │  /WebSocketMessage  │  │
│  │ Msg/Session│  │                     │  │
│  └─────┬─────┘  └──────────┬───────────┘  │
│        │                   │              │
│  ┌─────▼───────────────────▼───────────┐  │
│  │  Service + MyBatis Mapper          │  │
│  │  OnlineUserManager (在线管理)      │  │
│  └────────────────────────────────────┘  │
│                                           │
│  ┌────────────────────────────────────┐  │
│  │  MySQL (user/friend/message/       │  │
│  │         message_session)           │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

关键设计：
- HTTP API 处理非实时操作（注册/登录/好友管理/查历史消息）
- WebSocket 处理实时消息推送
- OnlineUserManager 用 ConcurrentHashMap 管理在线用户
- 消息发送者也会收到自己发出的消息，便于客户端统一展示

---

## 项目结构

```
Java_chatroom/
├── pom.xml
├── src/main/java/com/example/java_chatroom/
│   ├── JavaChatroomApplication.java       # 启动入口
│   ├── api/
│   │   ├── UserAPI.java                   # 登录/注册/用户信息
│   │   ├── FriendAPI.java                 # 添加/删除好友，好友列表
│   │   ├── MessageAPI.java                # 消息历史查询
│   │   ├── MessageSessionAPI.java          # 会话列表查询
│   │   ├── MessageRequest.java            # WebSocket 请求 DTO
│   │   ├── MessageResponse.java           # WebSocket 响应 DTO
│   │   ├── WebSocketAPI.java              # WebSocket 主消息处理
│   │   └── TestWebSocketAPI.java          # WebSocket 测试端点
│   ├── config/
│   │   └── WebSocketConfig.java           # WebSocket 注册 + 握手拦截器
│   ├── component/
│   │   └── OnlineUserManager.java         # 在线用户管理
│   └── model/
│       ├── User.java / UserMapper.java    # 用户
│       ├── Friend.java / FriendMapper.java # 好友关系
│       ├── Message.java / MessageMapper.java # 消息
│       ├── MessageSession.java / MessageSessionMapper.java # 会话
│       └── MessageSessionUserItem.java    # 会话-用户关联
├── src/main/resources/
│   ├── application.yml.example            # 配置模板
│   └── mapper/                            # MyBatis XML
└── src/main/java/db.sql                  # 数据库建表 + 演示数据
```

---

## 技术栈

| 组件 | 选型 |
|------|------|
| 框架 | Spring Boot 3.5.8 |
| ORM | MyBatis 3.0.5 |
| 数据库 | MySQL 8.0 |
| 实时通信 | WebSocket（原生 Spring WebSocket） |
| 会话管理 | HttpSession |
| Java 版本 | 17 |
| 构建工具 | Maven |

---

## 数据库设计

库名：`java_chatroom`

### user（用户表）
```sql
CREATE TABLE user (
    userId   INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) UNIQUE,
    password VARCHAR(20)
);
```

### friend（好友关系表）
```sql
CREATE TABLE friend (
    userId   INT,   -- 用户 ID
    friendId INT    -- 好友 ID
);
-- 双向好友关系：一条记录加两个方向
```

### message_session（会话表）
```sql
CREATE TABLE message_session (
    sessionId INT PRIMARY KEY AUTO_INCREMENT,
    lastTime  DATETIME  -- 最后消息时间
);
```

### message_session_user（会话-用户关联）
```sql
CREATE TABLE message_session_user (
    sessionId INT,
    userId    INT
);
-- 一对多：一个会话可以有多个用户（群聊支持）
```

### message（消息表）
```sql
CREATE TABLE message (
    messageId INT PRIMARY KEY AUTO_INCREMENT,
    fromId    INT,           -- 发送者
    sessionId INT,           -- 所属会话
    content   VARCHAR(2048), -- 消息内容
    postTime  DATETIME       -- 发送时间
);
```

### 表关系图

```
user ──┬── friend (userId ↔ friendId) 好友关系
       │
       ├── message_session_user (userId ↔ sessionId) 会话成员
       │
       └── message (fromId → userId) 消息发送者

message_session ── message_session_user (sessionId ↔ userId) 会话成员
                ── message (sessionId ↔ messageId) 会话消息
```

设计要点：会话和成员拆成两张表，一个会话挂多个成员，单聊和群聊共用同一套结构。

---

## WebSocket 消息流程

```
① 用户 A 发送消息
   │
   ▼
② WebSocketAPI.handleTextMessage()
   │
   ├── 持久化消息到 MySQL
   │
   ├── 查询消息接收者（会话所有成员）
   │
   └── OnlineUserManager 查询在线状态
       │
       ├── 在线 → 通过 WebSocket Session 推送
       └── 离线 → 忽略（下次登录拉取历史消息）
       │
       └── 发送者也收到（便于客户端展示）
```

---

## 关键设计点

### 握手拦截器：复用 HttpSession 登录态

WebSocket 连接本身不带登录态，但是用户刚在 HTTP 阶段登录过，HttpSession 里有 userId。

WebSocketConfig 里注册了一个 HttpSessionHandshakeInterceptor：握手阶段把 HttpSession 里的 userId 拿出来，塞进 WebSocketSession 的 attributes。后面 WebSocket 处理器从 WebSocketSession.attributes 里就能拿到发送者是谁，不用重新登录。

### OnlineUserManager 在线用户管理

```java
// key: userId, value: WebSocket Session
private ConcurrentHashMap<Integer, WebSocketSession> onlineUsers;
```

为什么用 ConcurrentHashMap 而不是 HashMap：
- WebSocket 连接建和断是不同线程触发的，多线程并发读写 HashMap 会有并发问题
- ConcurrentHashMap 读不加锁，性能好

单设备登录策略：
- 同一用户后连上来时，把旧 session 关掉，新 session 覆盖旧的
- 避免一个账号在两个浏览器同时在线，旧浏览器的消息丢给新连接

下线清理：
- WebSocket 连接关闭时，回调 afterConnectionClosed
- 先比对当前 map 里存的 session 是不是自己：是才 remove，不是不动
- 为什么要比对：后连的新 session 已经覆盖了旧的，如果旧连接关闭时直接 remove，会把新 session 删掉，导致新连接收不到消息

---

## API 接口

### 用户模块

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /login | 用户登录 | username, password |
| POST | /register | 用户注册 | username, password |
| GET | /userInfo | 获取当前用户信息 | HttpSession |

### 好友模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /friendList | 好友列表 |
| POST | /addFriend | 添加好友 |

### 会话模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /messageSessionList | 会话列表 |
| GET | /messageHistory | 消息历史（按 sessionId 查询） |

### WebSocket 端点

| 协议 | 路径 | 说明 |
|------|------|------|
| WS | /WebSocketMessage | WebSocket 消息推送端点 |

---

## WebSocket 消息格式

### 请求（客户端 → 服务端）

```json
{
  "type": "message",
  "sessionId": 1,
  "content": "你好"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | 消息类型，固定为 "message" |
| sessionId | int | 目标会话 ID |
| content | String | 消息文本内容 |

### 响应（服务端 → 客户端）

```json
{
  "type": "message",
  "fromId": 1,
  "fromName": "张三",
  "sessionId": 1,
  "content": "你好"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | 消息类型 |
| fromId | int | 发送者用户 ID |
| fromName | String | 发送者用户名 |
| sessionId | int | 会话 ID |
| content | String | 消息内容 |

---

## 快速开始

### 1. 数据库准备

```sql
-- 在 MySQL 中执行
source src/main/java/db.sql
```

此脚本会创建 java_chatroom 库和所有表，并插入演示数据（张三/李四/王五/赵六四个用户，含好友关系和示例消息）。

### 2. 配置数据库连接

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

编辑 application.yml，填写实际数据库密码。

### 3. 启动服务

```bash
mvn spring-boot:run
# 或
mvn package -DskipTests
java -jar target/Java_chatroom-0.0.1-SNAPSHOT.jar
```

服务默认运行在 http://localhost:8083。

### 演示数据

| 用户 | 密码 | 好友 |
|------|------|------|
| 张三 | 123456 | 李四, 王五, 赵六 |
| 李四 | 123456 | 张三 |
| 王五 | 123456 | 张三 |
| 赵六 | 123456 | 张三 |

预置会话：张三-李四的聊天记录、张三-王五的会话。

---

## 功能特性

- 用户注册/登录 — 基于 HttpSession 的会话管理，注册防重名
- 好友管理 — 添加/删除好友，好友列表查询
- 单聊/群聊 — WebSocket 实时消息推送，数据库与转发逻辑已支持群聊扩展
- 在线状态 — ConcurrentHashMap 管理在线用户映射，断线自动清理
- 消息持久化 — 所有消息写入 MySQL，支持历史消息查询
- 发送者也接收 — 消息发送者也会收到自己发出的消息，便于客户端统一展示

---

## 安全说明

- 真实配置文件 application.yml 已加入 .gitignore，不会被提交
- 请参考 application.yml.example 模板创建本地配置
- 数据库初始化脚本仅限本地开发使用
- 生产环境部署时请务必修改默认密码

---

## 可改进点

1. 当前用 HttpSession 做登录态，分布式部署会有 session 共享问题，应该换成 JWT
2. 在线状态存在单机内存里，多实例部署时一个用户只在一台机器在线，应该换成 Redis 存在线映射
3. 消息量上来后全表查历史会慢，应该加 sessionId + postTime 的联合索引
4. WebSocket 没做心跳检测，死连接要靠 TCP 超时发现，应该加应用层心跳
5. 消息没做已读未读状态，群聊里其他人的已读情况看不到
6. 没做文件/图片消息，当前只支持文本
