# 机器人 DIY 论坛数据库设计

## 1. 文档说明

本文档用于记录当前“机器人 DIY 论坛”初版项目的数据库设计方案，包含：

- 核心业务表说明
- 各表字段设计
- 表之间的关系
- 完整 MySQL 建表 SQL
- 当前版本设计约束
- 后续可扩展方向

当前初版功能范围主要包括：

- 用户注册
- 用户登录
- 用户启用/禁用
- 论坛分类
- 发主题帖
- 回复评论
- 主题帖附件上传
- 回复附件上传
- 标签
- 帖子与标签关联

当前数据库设计默认约定：

- 数据库：`MySQL 8.0`
- 字符集：`utf8mb4`
- 存储引擎：`InnoDB`
- 用户角色直接存放在 `sys_user.role`
- 当前版本不单独设计角色表
- 用户表 `sys_user` 不做逻辑删除，只通过 `status` 控制启用/禁用
- 其余内容表保留 `is_deleted` 逻辑删除字段

---

## 2. 核心表设计概览

当前初版建议使用以下 7 张核心表：

1. `sys_user`：用户表
2. `forum_category`：论坛分类表
3. `forum_topic`：主题帖表
4. `forum_reply`：回复表
5. `forum_tag`：标签表
6. `forum_topic_tag`：帖子标签关联表
7. `forum_attachment`：附件表

---

## 3. 各表详细设计

### 3.1 `sys_user` 用户表

用于存储平台注册用户信息。

主要设计说明：

- `username` 是登录账号，必须唯一
- `email` 注册必填，必须唯一
- `password` 字段存的是加密后的密码串，不能存明文密码
- `role` 当前只使用两个值：
  - `USER`：普通用户
  - `ADMIN`：管理员
- `status` 当前用于控制账号是否可用：
  - `1`：启用
  - `0`：禁用

字段说明：

- `id`：用户主键 ID
- `username`：用户名
- `email`：邮箱
- `password`：密码（加密后）
- `role`：角色
- `status`：状态
- `avatar`：头像地址
- `create_time`：创建时间
- `update_time`：更新时间

---

### 3.2 `forum_category` 分类表

用于存储论坛板块分类，支持父分类和子分类。

设计思路：

- 使用 `parent_id` 实现父子分类结构
- 顶级分类：`parent_id = 0`
- 子分类：`parent_id = 父分类ID`
- 当前初版不再单独设计“是否允许发帖”字段
- 分类是否可用统一通过 `status` 控制

字段说明：

- `id`：分类主键 ID
- `parent_id`：父分类 ID
- `name`：分类名称
- `description`：分类描述
- `status`：状态
- `create_time`：创建时间
- `update_time`：更新时间
- `is_deleted`：逻辑删除标记

状态建议：

- `status = 1`：启用
- `status = 0`：停用

---

### 3.3 `forum_topic` 主题帖表

用于存储用户发布的主题帖，也就是论坛中的主贴。

设计说明：

- 一条主题帖只能属于一个分类
- 一条主题帖只能由一个用户创建
- 主题帖保留 `status` 字段，用于控制帖子业务状态
- `is_deleted` 只负责逻辑删除
- `view_count` 用于记录浏览量
- `reply_count` 用于记录回复数量

`status` 建议值：

- `1`：正常
- `0`：隐藏
- `2`：关闭

字段说明：

- `id`：主题帖主键 ID
- `category_id`：所属分类 ID
- `creator_id`：发帖人用户 ID
- `title`：帖子标题
- `content`：帖子正文内容，存 Markdown 原文
- `status`：主题帖状态
- `view_count`：浏览量
- `reply_count`：回复数
- `create_time`：创建时间
- `update_time`：更新时间
- `is_deleted`：逻辑删除标记

---

### 3.4 `forum_reply` 回复表

用于存储帖子下的回复，并支持基于父评论的评论回复关系。

当前版本采用简化方案：

- 保留 `parent_reply_id`
- 不保留 `root_reply_id`

字段说明：

- `id`：回复主键 ID
- `topic_id`：所属主题帖 ID
- `creator_id`：回复人用户 ID
- `parent_reply_id`：父回复 ID，顶层回复为 `0`
- `reply_to_user_id`：被回复用户 ID
- `content`：回复内容，存 Markdown 原文
- `create_time`：创建时间
- `update_time`：更新时间
- `is_deleted`：逻辑删除标记

使用规则：

1. 顶层回复

- `parent_reply_id = 0`
- `reply_to_user_id = NULL`

2. 回复某条评论

- `parent_reply_id = 被回复评论ID`
- `reply_to_user_id = 被回复用户ID`

---

### 3.5 `forum_tag` 标签表

用于存储帖子标签。

设计说明：

- 标签用于辅助分类内容，但不代替分类
- 一个帖子只能属于一个分类，但可以有多个标签
- 当前版本标签表不加 `status`
- 当前版本标签表只通过 `is_deleted` 控制删除

字段说明：

- `id`：标签主键 ID
- `name`：标签名称
- `create_time`：创建时间
- `update_time`：更新时间
- `is_deleted`：逻辑删除标记

---

### 3.6 `forum_topic_tag` 帖子标签关联表

用于建立“帖子”和“标签”的多对多关系。

字段说明：

- `id`：主键 ID
- `topic_id`：主题帖 ID
- `tag_id`：标签 ID

约束建议：

- `topic_id + tag_id` 必须唯一

---

### 3.7 `forum_attachment` 附件表

用于存储主题帖和回复上传的附件信息。

支持的附件类型包括：

- 图片
- 文档
- PDF
- Word
- 其他普通文件

设计说明：

- 一个附件只能属于一篇主题帖或一条回复
- 通过 `related_type + related_id` 进行业务关联
- `related_type` 当前建议值：
  - `TOPIC`
  - `REPLY`
- `file_type` 当前建议值：
  - `IMAGE`
  - `FILE`

字段说明：

- `id`：附件主键 ID
- `file_name`：文件名
- `file_path`：文件路径
- `file_size`：文件大小，单位字节
- `file_type`：文件类型
- `related_type`：关联类型
- `related_id`：关联业务 ID
- `uploader_id`：上传人 ID
- `create_time`：创建时间
- `update_time`：更新时间
- `is_deleted`：逻辑删除标记

说明：

- 如果 `related_type = TOPIC`，则 `related_id` 对应 `forum_topic.id`
- 如果 `related_type = REPLY`，则 `related_id` 对应 `forum_reply.id`

---

## 4. 表之间的关系

当前表关系如下：

- 一个用户 `sys_user` 可以发布多个主题帖 `forum_topic`
- 一个用户 `sys_user` 可以发布多个回复 `forum_reply`
- 一个分类 `forum_category` 可以有多个子分类
- 一个分类 `forum_category` 可以有多个主题帖
- 一个主题帖 `forum_topic` 可以有多个回复 `forum_reply`
- 一个主题帖 `forum_topic` 可以绑定多个标签 `forum_tag`
- 一个标签 `forum_tag` 可以被多个主题帖使用
- 一个主题帖 `forum_topic` 可以有多个附件 `forum_attachment`
- 一条回复 `forum_reply` 可以有多个附件 `forum_attachment`

---

## 5. 完整 MySQL 建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS robot_diy_forum
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE robot_diy_forum;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码，存加密串',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER普通用户 ADMIN管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE forum_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，顶级分类为0',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_category_parent_name (parent_id, name),
    KEY idx_forum_category_parent_id (parent_id),
    KEY idx_forum_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛分类表';

CREATE TABLE forum_topic (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主题帖ID',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    creator_id BIGINT NOT NULL COMMENT '发帖人用户ID',
    title VARCHAR(150) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '正文内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0隐藏 2关闭',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    reply_count INT NOT NULL DEFAULT 0 COMMENT '回复数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_forum_topic_category_id (category_id),
    KEY idx_forum_topic_creator_id (creator_id),
    KEY idx_forum_topic_status (status),
    KEY idx_forum_topic_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题帖表';

CREATE TABLE forum_reply (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    topic_id BIGINT NOT NULL COMMENT '所属主题帖ID',
    creator_id BIGINT NOT NULL COMMENT '回复人用户ID',
    parent_reply_id BIGINT NOT NULL DEFAULT 0 COMMENT '父回复ID，顶层回复为0',
    reply_to_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    content TEXT NOT NULL COMMENT '回复内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_forum_reply_topic_id (topic_id),
    KEY idx_forum_reply_creator_id (creator_id),
    KEY idx_forum_reply_parent_reply_id (parent_reply_id),
    KEY idx_forum_reply_reply_to_user_id (reply_to_user_id),
    KEY idx_forum_reply_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

CREATE TABLE forum_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE forum_topic_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    topic_id BIGINT NOT NULL COMMENT '主题帖ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_topic_tag (topic_id, tag_id),
    KEY idx_forum_topic_tag_topic_id (topic_id),
    KEY idx_forum_topic_tag_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题帖标签关联表';

CREATE TABLE forum_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小，单位字节',
    file_type VARCHAR(20) NOT NULL COMMENT '文件类型: IMAGE/FILE',
    related_type VARCHAR(20) NOT NULL COMMENT '关联类型: TOPIC/REPLY',
    related_id BIGINT NOT NULL COMMENT '关联业务ID',
    uploader_id BIGINT NOT NULL COMMENT '上传人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_forum_attachment_related (related_type, related_id),
    KEY idx_forum_attachment_uploader_id (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';
```

---

## 6. 外键设计建议

当前初版建议：

- 先不在数据库层面强制加外键约束
- 关系一致性由服务层代码控制

如果后期要加强数据库约束，这些逻辑关系需要保持：

- `forum_topic.category_id -> forum_category.id`
- `forum_topic.creator_id -> sys_user.id`
- `forum_reply.topic_id -> forum_topic.id`
- `forum_reply.creator_id -> sys_user.id`
- `forum_reply.parent_reply_id -> forum_reply.id`
- `forum_reply.reply_to_user_id -> sys_user.id`
- `forum_topic_tag.topic_id -> forum_topic.id`
- `forum_topic_tag.tag_id -> forum_tag.id`
- `forum_attachment.uploader_id -> sys_user.id`

同时业务上要保证：

- 当 `related_type = TOPIC` 时，`related_id` 对应 `forum_topic.id`
- 当 `related_type = REPLY` 时，`related_id` 对应 `forum_reply.id`

---

## 7. 状态字段与删除字段说明

### 7.1 用户表 `sys_user`

- `status = 1`：启用
- `status = 0`：禁用

说明：

- 用户表当前不做逻辑删除

### 7.2 分类表 `forum_category`

- `status = 1`：启用
- `status = 0`：停用
- `is_deleted = 1`：逻辑删除

### 7.3 主题帖表 `forum_topic`

- `status = 1`：正常
- `status = 0`：隐藏
- `status = 2`：关闭
- `is_deleted = 1`：主题帖逻辑删除

### 7.4 回复表 `forum_reply`

- `is_deleted = 1`：回复逻辑删除

### 7.5 标签表 `forum_tag`

- `is_deleted = 1`：标签逻辑删除

### 7.6 附件表 `forum_attachment`

- `is_deleted = 1`：附件逻辑删除

---

## 8. 业务规则建议

当前服务层建议遵守以下规则：

- 被禁用用户不能登录
- 被禁用用户不能发帖
- 被禁用用户不能回复
- 被禁用用户不能上传附件
- 发帖时必须选择有效分类
- 只有满足以下条件的分类才允许发帖：
  - `status = 1`
  - `is_deleted = 0`
- 回复时只能回复未删除且未关闭的主题帖
- `forum_topic.status = 2` 时，不允许继续回复
- 删除主题帖时，建议同时逻辑删除其下回复和附件
- 删除回复时，建议同时逻辑删除其下附件
- 标签名称不能重复
- 同一个帖子不能重复绑定同一个标签
- 主题帖附件必须满足：
  - `related_type = TOPIC`
  - `related_id = 主题帖ID`
- 回复附件必须满足：
  - `related_type = REPLY`
  - `related_id = 回复ID`

---

## 9. 索引设计建议

### 分类相关

- 根据 `parent_id` 查询子分类
- 根据 `status` 查询启用分类

### 主题帖相关

- 根据 `category_id` 查询分类下帖子列表
- 根据 `creator_id` 查询某用户发过的帖子
- 根据 `status` 查询正常/关闭/隐藏帖子
- 根据 `create_time` 做帖子时间排序

### 回复相关

- 根据 `topic_id` 查询某主题帖下的回复
- 根据 `parent_reply_id` 查询某条评论的直接子评论
- 根据 `reply_to_user_id` 查询回复关系

### 标签相关

- 根据 `topic_id` 查询某个帖子绑定的标签
- 根据 `tag_id` 查询某个标签下的帖子

### 附件相关

- 根据 `related_type + related_id` 查询某个帖子或回复下的附件
- 根据 `uploader_id` 查询某用户上传的附件

---

## 10. 后续可扩展表

当前初版不做，但后续可以继续扩展：

- `admin_operation_log`：管理员操作日志
- `forum_category_tag`：分类和标签的限制关系
- `forum_notice`：论坛公告表
- `user_login_log`：登录日志表
- `forum_like`：点赞表
- `forum_favorite`：收藏表

当前阶段先把以下 7 张核心表跑通：

- `sys_user`
- `forum_category`
- `forum_topic`
- `forum_reply`
- `forum_tag`
- `forum_topic_tag`
- `forum_attachment`
