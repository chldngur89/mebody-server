package com.mebody.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardSummary(
    long totalUsers,
    long activeUsers,
    long suspendedUsers,
    long sellerUsers,
    long adminUsers,
    long todaySignups,
    BigDecimal averageMissionAchievementRate
) {
}
