package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户与角色关联表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yp_sys_user_role")
public class SysUserRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;
}
