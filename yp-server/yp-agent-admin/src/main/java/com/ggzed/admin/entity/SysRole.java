package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yp_sys_role")
public class SysRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
