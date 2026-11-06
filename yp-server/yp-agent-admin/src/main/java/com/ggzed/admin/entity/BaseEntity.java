package com.ggzed.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 公共审计字段：创建时间、更新时间、逻辑删除标记（0 正常 / 1 删除）。
 */
@Data
public abstract class BaseEntity implements Serializable {

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
