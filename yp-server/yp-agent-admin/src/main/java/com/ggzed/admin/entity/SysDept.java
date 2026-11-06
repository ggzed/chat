package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yp_sys_dept")
public class SysDept extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String deptName;

    private Integer sort;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
