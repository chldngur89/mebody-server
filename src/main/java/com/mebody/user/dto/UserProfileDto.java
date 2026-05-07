package com.mebody.user.dto;

import com.mebody.user.domain.UserGrade;
import com.mebody.user.domain.UserProfile;
import com.mebody.user.domain.UserRole;
import com.mebody.user.domain.UserStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileDto(
    UUID id,
    UUID authUserId,
    String email,
    String name,
    String nickname,
    String phone,
    UserRole role,
    UserStatus status,
    UserGrade grade,
    String bodyBtiCode,
    String bodyBtiTitle,
    String bodyBtiDescription,
    BigDecimal missionAchievementRate,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime deletedAt,
    UUID latestResultId,
    String latestBodyBtiCode,
    OffsetDateTime latestResultCompletedAt,
    Boolean latestCodeMatchesProfile
) {
  public static UserProfileDto from(UserProfile profile) {
    return from(profile, null, null, null);
  }

  public static UserProfileDto from(
      UserProfile profile,
      UUID latestResultId,
      String latestBodyBtiCode,
      OffsetDateTime latestResultCompletedAt
  ) {
    Boolean latestCodeMatchesProfile = latestBodyBtiCode == null
        ? null
        : latestBodyBtiCode.equals(profile.getBodyBtiCode());

    return new UserProfileDto(
        profile.getId(),
        profile.getAuthUserId(),
        profile.getEmail(),
        profile.getName(),
        profile.getNickname(),
        profile.getPhone(),
        profile.getRole(),
        profile.getStatus(),
        profile.getGrade(),
        profile.getBodyBtiCode(),
        profile.getBodyBtiTitle(),
        profile.getBodyBtiDescription(),
        profile.getMissionAchievementRate(),
        profile.getCreatedAt(),
        profile.getUpdatedAt(),
        profile.getDeletedAt(),
        latestResultId,
        latestBodyBtiCode,
        latestResultCompletedAt,
        latestCodeMatchesProfile
    );
  }
}
