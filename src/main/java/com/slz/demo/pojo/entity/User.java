package com.slz.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.slz.demo.common.enumeration.UserRole;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import com.tangzc.mpe.autotable.annotation.Table;
import com.tangzc.mpe.autotable.annotation.UniqueIndex;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
@Table(value = "sys_user", comment = "用户表")
public class User {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "用户ID")
    private Long id;

    @UniqueIndex(name = "uk_sys_user_username", comment = "用户名唯一索引")
    @Column(type = "varchar", length = 50, notNull = true, comment = "用户名")
    private String username;

    @UniqueIndex(name = "uk_sys_user_email", comment = "邮箱唯一索引")
    @Column(type = "varchar", length = 100, notNull = true, comment = "邮箱")
    private String email;

    @Column(type = "varchar", length = 50, notNull = true, comment = "昵称")
    private String nickname;

    @Column(type = "varchar", length = 255, notNull = true, comment = "密码")
    private String password;

    @Column(type = "varchar", length = 20, notNull = true, defaultValue = "'USER'", comment = "角色: USER普通用户 ADMIN管理员")
    private UserRole role;

    @Column(type = "tinyint", notNull = true, defaultValue = "1", comment = "状态: 1启用 0禁用")
    private Integer status;

    @Column(type = "varchar", length = 255, comment = "头像")
    private String avatar;

    @Column(value = "create_time", type = "datetime", notNull = true, comment = "创建时间", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;
}
