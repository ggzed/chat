package com.ggzed.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录响应：令牌 + 用户信息 + 角色编码列表。
 */
@Data
@AllArgsConstructor
public class LoginResponse implements Serializable {

    /** 演示令牌（UUID）；生产应替换为 JWT */
    private String token;

    private Long userId;

    private String username;

    private String realName;

    private Long deptId;

    /** 角色编码：sales / admin / ops */
    private List<String> roles;
}
