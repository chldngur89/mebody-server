package com.mebody.user.dto;

import java.math.BigDecimal;

public record MeSummaryResponse(
    UserProfileDto profile,
    String bodyBtiCode,
    String bodyBtiTitle,
    BigDecimal missionAchievementRate,
    long activeMissionCount,
    long completedMissionCount
) {
}
