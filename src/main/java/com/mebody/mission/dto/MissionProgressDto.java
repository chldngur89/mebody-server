package com.mebody.mission.dto;

import com.mebody.mission.domain.UserMissionProgress;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MissionProgressDto(
    UUID id,
    UUID missionId,
    int currentCount,
    int targetCount,
    BigDecimal achievementRate,
    OffsetDateTime completedAt
) {
  public static MissionProgressDto from(UserMissionProgress progress) {
    return new MissionProgressDto(
        progress.getId(),
        progress.getMissionId(),
        progress.getCurrentCount(),
        progress.getTargetCount(),
        progress.getAchievementRate(),
        progress.getCompletedAt()
    );
  }
}
