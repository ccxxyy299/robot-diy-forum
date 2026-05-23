package com.slz.demo.server.service.impl;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.ao.RoleAO;
import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.server.service.ForumPermissionService;
import org.springframework.stereotype.Service;

/**
 * 论坛资源权限校验服务实现
 */
@Service
public class ForumPermissionServiceImpl implements ForumPermissionService {

    @Override
    public void checkCanUpdateTopic(ForumTopic topic) {
        RoleAO currentUser = requireLogin();
        if (isAdmin(currentUser) || topic.getCreatorId().equals(currentUser.getUserId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_PERMISSION);
    }

    @Override
    public void checkCanDeleteTopic(ForumTopic topic) {
        RoleAO currentUser = requireLogin();
        if (isAdmin(currentUser) || topic.getCreatorId().equals(currentUser.getUserId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_PERMISSION);
    }

    @Override
    public void checkCanDeleteReply(ForumReply reply, ForumTopic topic) {
        RoleAO currentUser = requireLogin();
        if (isAdmin(currentUser)
                || reply.getCreatorId().equals(currentUser.getUserId())
                || topic.getCreatorId().equals(currentUser.getUserId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_PERMISSION);
    }

    private RoleAO requireLogin() {
        RoleAO currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private boolean isAdmin(RoleAO currentUser) {
        return currentUser.getRole() == UserRole.ADMIN;
    }
}
