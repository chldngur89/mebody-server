package com.mebody.admin.dto;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;

public record AdminUserSearchRequest(
    String search,
    UserRole role,
    UserStatus status,
    UserGrade grade,
    boolean includeDeleted,
    int page,
    int size
) {
}
