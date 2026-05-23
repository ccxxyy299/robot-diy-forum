package com.slz.demo.server.service;

import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.entity.ForumTopic;

/**
 * 论坛资源权限校验服务
 */
public interface ForumPermissionService {

    /**
     * 校验当前用户是否可以修改主题帖
     */
    void checkCanUpdateTopic(ForumTopic topic);

    /**
     * 校验当前用户是否可以删除主题帖
     */
    void checkCanDeleteTopic(ForumTopic topic);

    /**
     * 校验当前用户是否可以删除回复
     */
    void checkCanDeleteReply(ForumReply reply, ForumTopic topic);
}
