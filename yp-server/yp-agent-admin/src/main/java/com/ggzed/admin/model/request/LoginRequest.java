package com.ggzed.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求。
 */
@Data
public class LoginRequest implements Serializable {

    @NotBlank(message = "账号不能为空")
    @Size(max = 64)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 64)
    private String password;
}
