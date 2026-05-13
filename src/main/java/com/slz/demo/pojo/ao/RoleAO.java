package com.slz.demo.pojo.ao;

import com.slz.demo.common.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAO {

    private Long userId;

    private UserRole role;
}
