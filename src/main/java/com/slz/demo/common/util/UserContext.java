package com.slz.demo.common.util;

import com.slz.demo.pojo.ao.RoleAO;

/**
 * 当前登录用户上下文，基于 ThreadLocal
 */
public class UserContext {

    private static final ThreadLocal<RoleAO> CURRENT_USER = new ThreadLocal<>();

    public static void set(RoleAO ao) {
        CURRENT_USER.set(ao);
    }

    public static RoleAO get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
