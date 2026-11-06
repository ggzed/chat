package com.ggzed.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * 字段自动填充：创建/更新时间、逻辑删除标记。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", OffsetDateTime.class, OffsetDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", OffsetDateTime.class, OffsetDateTime.now());
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", OffsetDateTime.class, OffsetDateTime.now());
    }
}
