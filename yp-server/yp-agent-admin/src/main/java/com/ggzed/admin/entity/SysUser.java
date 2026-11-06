package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * 用户表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yp_sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 加密 */
    private String password;

    private String realName;

    private String avatar;

    private Long deptId;

    private String phone;

    /** 1 启用 / 0 停用 */
    private Integer status;

    private OffsetDateTime lastLoginTime;
}
