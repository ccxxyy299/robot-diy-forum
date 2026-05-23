package com.slz.demo.server.aspect;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.ao.RoleAO;
import com.slz.demo.server.annotation.RoleRequired;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 角色权限切面
 */
@Slf4j
@Aspect
@Component
public class RoleRequiredAspect {

    @Before("@annotation(roleRequired) && within(com.slz.demo.server.controller..*)")
    public void before(RoleRequired roleRequired) {
        RoleAO currentUser = UserContext.get();
        if (currentUser == null) {
            log.warn("权限校验拒绝: 未登录");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        UserRole requiredRole = roleRequired.value();
        if (currentUser.getRole().getLevel() < requiredRole.getLevel()) {
            log.warn("权限校验拒绝: userId={}, 当前角色={}, 需要角色={}",
                    currentUser.getUserId(), currentUser.getRole(), requiredRole);
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        log.debug("权限校验通过: userId={}, 角色={}", currentUser.getUserId(), currentUser.getRole());
    }
}
