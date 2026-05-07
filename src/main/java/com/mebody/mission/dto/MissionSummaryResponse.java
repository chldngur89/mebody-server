package com.mebody.mission.dto;

import java.math.BigDecimal;
import java.util.List;

public record MissionSummaryResponse(
    BigDecimal overallAchievementRate,
    long activeMissionCount,
    long completedMissionCount,
    List<MissionProgressDto> progress
) {
}
