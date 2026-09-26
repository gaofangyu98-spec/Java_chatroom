# Java Chatroom

基于 **Spring Boot 3 + WebSocket + MyBatis** 的实时聊天室，支持单聊/群聊、在线用户管理、消息实时推送与历史记录。

## 项目结构

```
Java_chatroom/
├── src/main/java/com/example/java_chatroom/
│   ├── api/                    # 控制器层
│   │   ├── UserAPI.java        # 用户注册/登录/信息查询
│   │   ├── FriendAPI.java      # 好友管理
│   │   ├── MessageAPI.java     # 消息历史
│   │   ├── MessageSessionAPI.java  # 会话管理
│   │   └── WebSocketAPI.java   # WebSocket 消息转发
│   ├── config/
│   │   └── WebSocketConfig.java    # WebSocket 注册与拦截器配置
│   ├── component/
│   │   └── OnlineUserManager.java  # 在线用户管理（ConcurrentHashMap）
│   ├── model/                  # 数据模型 + Mapper
│   └── db.sql                  # 数据库建表与测试数据
├── src/main/resources/
│   ├── application.yml.example # 应用配置模板
│   └── mapper/                 # MyBatis XML 映射
├── pom.xml
└── README.md
```

## 技术栈

| 组件 | 选型 |
|------|------|
| 框架 | Spring Boot 3.5.8 |
| ORM | MyBatis 3.0.5 |
| 数据库 | MySQL 8.0 |
| 实时通信 | WebSocket（原生 Spring WebSocket）|
| 构建工具 | Maven |
| Java 版本 | 17 |

## 功能特性

- **用户注册/登录** — 基于 HttpSession 的会话管理，注册防重名
- **好友管理** — 添加/删除好友，好友列表查询
- **单聊/群聊** — WebSocket 实时消息推送，后端支持群聊扩展（数据库与转发逻辑已支持）
- **在线状态** — ConcurrentHashMap 管理在线用户映射，断线自动清理
- **消息历史** — 消息持久化到 MySQL，支持历史消息查询
- **消息转发** — 消息发送者也会收到自己发出的消息（便于客户端展示）

## 快速开始

### 1. 数据库准备

```sql
-- 在 MySQL 中执行
source src/main/java/db.sql
```

### 2. 配置数据库连接

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
# 编辑 application.yml，填写实际数据库密码
```

### 3. 启动服务

```bash
mvn spring-boot:run
# 或
mvn package -DskipTests
java -jar target/Java_chatroom-0.0.1-SNAPSHOT.jar
```

服务默认运行在 `http://localhost:8083`。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 用户登录 |
| POST | `/register` | 用户注册 |
| GET  | `/userInfo` | 获取当前用户信息 |
| GET  | `/friendList` | 好友列表 |
| POST | `/addFriend` | 添加好友 |
| GET  | `/messageSessionList` | 会话列表 |
| GET  | `/messageHistory` | 消息历史 |
| WS  | `/WebSocketMessage` | WebSocket 消息推送 |

## WebSocket 消息格式

**请求：**
```json
{"type": "message", "sessionId": 1, "content": "你好"}
```

**响应：**
```json
{"type": "message", "fromId": 1, "fromName": "张三", "sessionId": 1, "content": "你好"}
```

## 安全说明

- 真实配置文件 `application.yml` 已加入 `.gitignore`，不会被提交
- 请参考 `application.yml.example` 模板创建本地配置
- 数据库初始化脚本 `db.sql` 包含演示数据，仅限本地开发使用
- 生产环境部署时请**务必修改默认密码**
