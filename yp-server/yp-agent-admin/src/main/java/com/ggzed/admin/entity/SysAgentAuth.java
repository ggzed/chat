package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 数据权限授权表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yp_sys_agent_auth")
public class SysAgentAuth extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 授权目标 ID（用户 ID 或部门 ID） */
    private Long targetId;

    /** 目标类型：1 用户 / 2 部门 */
    private Integer targetType;

    private Long agentId;
}
