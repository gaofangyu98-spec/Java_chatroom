create database if not exists java_chatroom charset utf8;

use java_chatroom;

drop table if exists user;
create table user (
    userId int primary key auto_increment,
    username varchar(20) unique,
    password varchar(20)
);

insert into user values(1, '张三', '123456');
insert into user values(2, '李四', '123456');
insert into user values(3, '王五', '123456');
insert into user values(4, '赵六', '123456');


drop table if exists friend;
create table friend (
    userId int,
    friendId int
);

insert into friend values(1, 2);
insert into friend values(2, 1);
insert into friend values(1, 3);
insert into friend values(3, 1);
insert into friend values(1, 4);
insert into friend values(4, 1);

drop table if exists message_session;
create table message_session (
    sessionId int primary key auto_increment,

    lastTime datetime
);

insert into message_session values(1, '2000-05-01 00:00:00');
insert into message_session values(2, '2000-06-01 00:00:00');


drop table if exists message_session_user;
create table message_session_user (
    sessionId int,
    userId int
);


insert into message_session_user values(1, 1), (1, 2);

insert into message_session_user values(2, 1), (2, 3);



drop table if exists message;
create table message (
    messageId int primary key auto_increment,
    fromId int,
    sessionId int,
    content varchar(2048),
    postTime datetime
);


insert into message values (1, 1, 1, '今晚吃啥', '2000-05-01 17:00:00');
insert into message values (2, 2, 1, '随便', '2000-05-01 17:01:00');
insert into message values (3, 1, 1, '那吃面?', '2000-05-01 17:02:00');
insert into message values (4, 2, 1, '不想吃', '2000-05-01 17:03:00');
insert into message values (5, 1, 1, '那你想吃啥', '2000-05-01 17:04:00');
insert into message values (6, 2, 1, '随便', '2000-05-01 17:05:00');
insert into message values (11, 1, 1, '那吃米饭炒菜?', '2000-05-01 17:06:00');
insert into message values (8, 2, 1, '不想吃', '2000-05-01 17:07:00');
insert into message values (9, 1, 1, '那你想吃啥?', '2000-05-01 17:08:00');
insert into message values (10, 2, 1, '随便', '2000-05-01 17:09:00');

insert into message values(7, 1, 2, '晚上一起约?', '2000-05-02 12:00:00');

