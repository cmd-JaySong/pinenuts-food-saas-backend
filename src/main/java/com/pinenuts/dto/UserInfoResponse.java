package com.pinenuts.dto;

import com.pinenuts.entity.SysPermission;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private Set<String> permissions;
    private List<SysPermission> menus;  // 菜单树
}
