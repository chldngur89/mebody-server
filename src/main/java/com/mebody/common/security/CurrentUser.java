package com.mebody.common.security;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import java.util.UUID;

public record CurrentUser(
    UUID id,
    UUID authUserId,
    String email,
    UserRole role,
    UserStatus status,
    UserGrade grade
) {
  public static CurrentUser from(UserProfile profile) {
    return new CurrentUser(
        profile.getId(),
        profile.getAuthUserId(),
        profile.getEmail(),
        profile.getRole(),
        profile.getStatus(),
        profile.getGrade()
    );
  }
}
